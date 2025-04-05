package at.jku.dke.task_app.jdbc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import org.h2.jdbcx.JdbcDataSource;

/**
 * A  template class used to inject and execute student-submitted JDBC code.
 *
 * The template sets up a PrintWriter to capture output and initializes a
 * JDBC connection to a configurable H2 in-memory database. Student code is
 * inserted into the marked section at runtime.
 */
public class Template {
    public String templateMethod() {
        StringWriter sw = new StringWriter();
        PrintWriter Out = new PrintWriter(sw, true);

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
