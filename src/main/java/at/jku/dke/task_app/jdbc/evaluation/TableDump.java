package at.jku.dke.task_app.jdbc.evaluation;

import java.util.List;

/**
 * A record that represents a database table's content, including the table's name,
 * its column names, and the rows of data.
 *
 * @param tableName The name of the table.
 * @param columns The list of column names in the table.
 * @param rows The list of rows, where each row is represented as a list of column values.
 */
public record TableDump(String tableName, List<String> columns, List<List<String>> rows) {}
