package at.jku.dke.task_app.jdbc.services;

import at.jku.dke.etutor.task_app.dto.ModifyTaskGroupDto;
import at.jku.dke.etutor.task_app.dto.TaskGroupModificationResponseDto;
import at.jku.dke.etutor.task_app.services.BaseTaskGroupService;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskGroupRepository;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskGroupDto;

import at.jku.dke.task_app.jdbc.evaluation.TableDump;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class provides methods for managing {@link JDBCTaskGroup}s.
 */
@Service
public class JDBCTaskGroupService extends BaseTaskGroupService<JDBCTaskGroup, ModifyJDBCTaskGroupDto> {

    private final MessageSource messageSource;

    /**
     * Creates a new instance of class {@link JDBCTaskGroupService}.
     *
     * @param repository    The task group repository.
     * @param messageSource The message source.
     */
    public JDBCTaskGroupService(JDBCTaskGroupRepository repository, MessageSource messageSource) {
        super(repository);
        this.messageSource = messageSource;
    }

    @Override
    protected JDBCTaskGroup createTaskGroup(long id, ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> modifyTaskGroupDto) {
        if (!modifyTaskGroupDto.taskGroupType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task group type.");

        var data = modifyTaskGroupDto.additionalData();
        return new JDBCTaskGroup(
            data.createStatements(),
            data.insertStatementsDiagnose(),
            data.insertStatementsSubmission()
        );
    }

    @Override
    protected void updateTaskGroup(JDBCTaskGroup taskGroup, ModifyTaskGroupDto<ModifyJDBCTaskGroupDto> modifyTaskGroupDto) {
        if (!modifyTaskGroupDto.taskGroupType().equals("jdbc"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid task group type.");

        var data = modifyTaskGroupDto.additionalData();
        taskGroup.setCreateStatements(data.createStatements());
        taskGroup.setInsertStatementsDiagnose(data.insertStatementsDiagnose());
        taskGroup.setInsertStatementsSubmission(data.insertStatementsSubmission());
    }


    @Override
    protected TaskGroupModificationResponseDto mapToReturnData(JDBCTaskGroup taskGroup, boolean create) {
        String createSql = taskGroup.getCreateStatements();
        if (createSql != null && !createSql.isBlank()) {
            // German
            List<TableDump> dumpsDe = loadSchemaTableDumps(createSql, Locale.GERMAN);
            String htmlDe = renderTableDumps(dumpsDe, Locale.GERMAN);
            // English
            List<TableDump> dumpsEn = loadSchemaTableDumps(createSql, Locale.ENGLISH);
            String htmlEn = renderTableDumps(dumpsEn, Locale.ENGLISH);

            String descDe = messageSource.getMessage(
                "defaultTaskGroupDescription",
                new Object[]{ createSql },
                Locale.GERMAN
            ) + htmlDe;
            String descEn = messageSource.getMessage(
                "defaultTaskGroupDescription",
                new Object[]{ createSql },
                Locale.ENGLISH
            ) + htmlEn;
            return new TaskGroupModificationResponseDto(descDe, descEn);
        }

        String descDe = messageSource.getMessage(
            "defaultTaskGroupDescription",
            null,
            Locale.GERMAN
        );
        String descEn = messageSource.getMessage(
            "defaultTaskGroupDescription",
            null,
            Locale.ENGLISH
        );
        return new TaskGroupModificationResponseDto(descDe, descEn);
    }

    /**
     * Loads the schema (tables and columns) from the DDL statements and returns a list of table dumps.
     */
    protected List<TableDump> loadSchemaTableDumps(String createStatements, Locale locale) {
        List<TableDump> dumps = new ArrayList<>();
        String url = "jdbc:h2:mem:schema_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;MODE=Oracle";
        try (Connection con = DriverManager.getConnection(url, "sa", "");

             Statement stmt = con.createStatement()) {
                stmt.execute("DROP ALL OBJECTS");
                for (String sql : createStatements.split(";")) {
                    if (!sql.isBlank()) stmt.execute(sql.trim());
                }
                DatabaseMetaData meta = con.getMetaData();
                ResultSet rsTables = meta.getTables(null, "PUBLIC", "%", new String[]{"TABLE"});
                List<String> tableNames = new ArrayList<>();
                while (rsTables.next()) {
                    tableNames.add(rsTables.getString("TABLE_NAME"));
                }
                for (String table : tableNames) {
                    List<String> header = List.of(
                        messageSource.getMessage("table.header.name", null, locale),
                        messageSource.getMessage("table.header.type", null, locale),
                        messageSource.getMessage("table.header.extra", null, locale)
                    );
                    List<List<String>> rows = new ArrayList<>();
                    ResultSet rsPks = meta.getPrimaryKeys(null, "PUBLIC", table);
                    Set<String> pkCols = new HashSet<>();
                    while (rsPks.next()) {
                        pkCols.add(rsPks.getString("COLUMN_NAME"));
                    }
                    rsPks.close();
                    ResultSet rsCols = meta.getColumns(null, "PUBLIC", table, "%");
                    while (rsCols.next()) {
                        String colName = rsCols.getString("COLUMN_NAME");
                        String typeName = rsCols.getString("TYPE_NAME");
                        String extra = pkCols.contains(colName)
                            ? messageSource.getMessage("table.extra.pk", null, locale)
                            : "";
                        rows.add(List.of(colName, typeName, extra));
                    }
                    rsCols.close();
                    dumps.add(new TableDump(table, header, rows));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dumps;
    }

    /**
     * Renders a list of {@link TableDump}s as HTML tables.
     */
    protected String renderTableDumps(List<TableDump> dumps, Locale locale) {
        StringBuilder html = new StringBuilder();
        if (dumps == null || dumps.isEmpty()) {
            return html.append("<p>No schema tables found</p>").toString();
        }
        String tableLabel = messageSource.getMessage("table.label", null, locale);
        for (TableDump dump : dumps) {
            html.append("<br/><b>")
                .append(tableLabel)
                .append(" ")
                .append(dump.tableName())
                .append("</b><br/>");
            html.append("<table border='1' style='border-collapse: collapse;'>");
            html.append("<tr>");
            for (String col : dump.columns()) {
                html.append("<th>").append(col).append("</th>");
            }
            html.append("</tr>");
            for (List<String> row : dump.rows()) {
                html.append("<tr>");
                for (String cell : row) {
                    html.append("<td style='padding:4px;'>").append(cell).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
        }
        return html.toString();
    }
}
