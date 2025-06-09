package at.jku.dke.task_app.jdbc.evaluation;

/**
 * This class provides a template string for executing the JDBC code of the students
 */
public class Template {
    /**
     * The Java code template as a multi-line string. Includes the templateMethod()
     */
    private String template = """
        package at.jku.dke.task_app.jdbc;

        import java.io.PrintWriter;
        import java.io.StringWriter;
        import java.sql.*;
        import org.h2.jdbcx.JdbcDataSource;

        public class Template {
            public String templateMethod() {
                StringWriter sw = new StringWriter();
                PrintWriter Out = new PrintWriter(sw, true);
                /*<Variables>*/

                try {
                    JdbcDataSource ds = new JdbcDataSource();
                    ds.setURL("/*<DB_URL>*/");
                    ds.setUser("sa");
                    ds.setPassword("");
                    /*<StudentInput> */

                } catch (Exception e) {
                    e.printStackTrace(Out);
                }

                return sw.toString();
            }
        }
        """;

    /**
     * Default constructor.
     */
    public Template() {}

    /**
     * Returns the Java template string used for compiling and executing student code.
     *
     * @return The Java source code template as a {@code String}.
     */
    public String getTemplate() {
        return template;
    }
}
