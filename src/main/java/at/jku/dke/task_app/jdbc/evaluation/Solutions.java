package at.jku.dke.task_app.jdbc.evaluation;

public class Solutions {

    public static String dbSchema = "CREATE TABLE books (book_id INT PRIMARY KEY, status VARCHAR(20));" +
            "CREATE TABLE loans (user_id INT, book_id INT, loan_date DATE);" +
            "INSERT INTO books (book_id, status) VALUES (1, 'available');" +
            "INSERT INTO books (book_id, status) VALUES (2, 'available');";

    // Correct Input
    public static String studentInput = "try (Connection con = ds.getConnection()) {\n" +
            "    con.setAutoCommit(false);\n" +
            "    int book = 1;\n" +
            "    int user = 99;\n" +
            "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
            "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
            "        selectStmt.setInt(1, book);\n" +
            "        ResultSet rs = selectStmt.executeQuery();\n" +
            "        if (!rs.next()) {\n" +
            "            Out.println(\"The book is not available!\");\n" +
            "        } else {\n" +
            "            Out.println(\"Book is available!\");\n" +
            "            String insert = \"INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, SYSDATE)\";\n"
            +
            "            String update = \"UPDATE books SET status = 'borrowed' WHERE book_id = ?\";\n" +
            "            try (\n" +
            "                PreparedStatement insertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement secondInsertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement updateStmt = con.prepareStatement(update)\n" +
            "            ) {\n" +
            "                // zweiter Eintrag zuerst\n" +
            "                secondInsertStmt.setInt(1, 100);\n" +
            "                secondInsertStmt.setInt(2, 2);\n" +
            "                secondInsertStmt.executeUpdate();\n" +
            "\n" +
            "                // erster Eintrag danach\n" +
            "                insertStmt.setInt(1, user);\n" +
            "                insertStmt.setInt(2, book);\n" +
            "                insertStmt.executeUpdate();\n" +
            "\n" +
            "                updateStmt.setInt(1, book);\n" +
            "                updateStmt.executeUpdate();\n" +
            "                con.commit();\n" +
            "                Out.println(\"The book loan was successfully recorded!\");\n" +
            "            } catch (SQLException ex) {\n" +
            "                Out.println(\"INSERT/UPDATE ERROR: \" + ex.getMessage());\n" +
            "                con.rollback();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "} catch (SQLException ex) {\n" +
            "    Out.println(\"DB ERROR: \" + ex.getMessage());\n" +
            "}";

    // Autocommit is not disabled
    public static String studentInputAutocommitNotDisabled = "try (Connection con = ds.getConnection()) {\n" +
            "    //con.setAutoCommit(false);\n" +
            "    int book = 1;\n" +
            "    int user = 99;\n" +
            "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
            "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
            "        selectStmt.setInt(1, book);\n" +
            "        ResultSet rs = selectStmt.executeQuery();\n" +
            "        if (!rs.next()) {\n" +
            "            Out.println(\"The book is not available!\");\n" +
            "        } else {\n" +
            "            Out.println(\"Book is available!\");\n" +
            "            String insert = \"INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, SYSDATE)\";\n"
            +
            "            String update = \"UPDATE books SET status = 'borrowed' WHERE book_id = ?\";\n" +
            "            try (\n" +
            "                PreparedStatement insertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement secondInsertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement updateStmt = con.prepareStatement(update)\n" +
            "            ) {\n" +
            "                // zweiter Eintrag zuerst\n" +
            "                secondInsertStmt.setInt(1, 100);\n" +
            "                secondInsertStmt.setInt(2, 2);\n" +
            "                secondInsertStmt.executeUpdate();\n" +
            "\n" +
            "                // erster Eintrag danach\n" +
            "                insertStmt.setInt(1, user);\n" +
            "                insertStmt.setInt(2, book);\n" +
            "                insertStmt.executeUpdate();\n" +
            "\n" +
            "                updateStmt.setInt(1, book);\n" +
            "                updateStmt.executeUpdate();\n" +
            "                con.commit();\n" +
            "                Out.println(\"The book loan was successfully recorded!\");\n" +
            "            } catch (SQLException ex) {\n" +
            "                Out.println(\"INSERT/UPDATE ERROR: \" + ex.getMessage());\n" +
            "                con.rollback();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "} catch (SQLException ex) {\n" +
            "    Out.println(\"DB ERROR: \" + ex.getMessage());\n" +
            "}";

    // No Exception Handling for Connection con = ds.getConnection()
    public static String studentInputNoExceptionHandling = "    Connection con = ds.getConnection();\n" +
            "    con.setAutoCommit(false);\n" +
            "    int book = 1;\n" +
            "    int user = 99;\n" +
            "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
            "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
            "        selectStmt.setInt(1, book);\n" +
            "        ResultSet rs = selectStmt.executeQuery();\n" +
            "        if (!rs.next()) {\n" +
            "            Out.println(\"The book is not available!\");\n" +
            "        } else {\n" +
            "            Out.println(\"Book is available!\");\n" +
            "            String insert = \"INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, SYSDATE)\";\n"
            +
            "            String update = \"UPDATE books SET status = 'borrowed' WHERE book_id = ?\";\n" +
            "            try (\n" +
            "                PreparedStatement insertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement secondInsertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement updateStmt = con.prepareStatement(update)\n" +
            "            ) {\n" +
            "                // zweiter Eintrag zuerst\n" +
            "                secondInsertStmt.setInt(1, 100);\n" +
            "                secondInsertStmt.setInt(2, 2);\n" +
            "                secondInsertStmt.executeUpdate();\n" +
            "\n" +
            "                // erster Eintrag danach\n" +
            "                insertStmt.setInt(1, user);\n" +
            "                insertStmt.setInt(2, book);\n" +
            "                insertStmt.executeUpdate();\n" +
            "\n" +
            "                updateStmt.setInt(1, book);\n" +
            "                updateStmt.executeUpdate();\n" +
            "                con.commit();\n" +
            "                Out.println(\"The book loan was successfully recorded!\");\n" +
            "            } catch (SQLException ex) {\n" +
            "                Out.println(\"INSERT/UPDATE ERROR: \" + ex.getMessage());\n" +
            "                con.rollback();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n";

    // Missing ; after ds.getConnection()
    public static String studentInputWrongSyntax = "    Connection con = ds.getConnection();\n" +
            "    con.setAutoCommit(false);\n" +
            "    int book = 1;\n" +
            "    int user = 99;\n" +
            "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
            "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
            "        selectStmt.setInt(1, book);\n" +
            "        ResultSet rs = selectStmt.executeQuery()\n" +
            "        if (!rs.next()) {\n" +
            "            Out.println(\"The book is not available!\");\n" +
            "        } else {\n" +
            "            Out.println(\"Book is available!\");\n" +
            "            String insert = \"INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, SYSDATE)\";\n"
            +
            "            String update = \"UPDATE books SET status = 'borrowed' WHERE book_id = ?\";\n" +
            "            try (\n" +
            "                PreparedStatement insertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement secondInsertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement updateStmt = con.prepareStatement(update)\n" +
            "            ) {\n" +
            "                // zweiter Eintrag zuerst\n" +
            "                secondInsertStmt.setInt(1, 100);\n" +
            "                secondInsertStmt.setInt(2, 2);\n" +
            "                secondInsertStmt.executeUpdate();\n" +
            "\n" +
            "                // erster Eintrag danach\n" +
            "                insertStmt.setInt(1, user);\n" +
            "                insertStmt.setInt(2, book);\n" +
            "                insertStmt.executeUpdate();\n" +
            "\n" +
            "                updateStmt.setInt(1, book);\n" +
            "                updateStmt.executeUpdate();\n" +
            "                con.commit();\n" +
            "                Out.println(\"The book loan was successfully recorded!\");\n" +
            "            } catch (SQLException ex) {\n" +
            "                Out.println(\"INSERT/UPDATE ERROR: \" + ex.getMessage());\n" +
            "                con.rollback();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n";

    public static String studentInputTooManyDbRows = "try (Connection con = ds.getConnection()) {\n" +
        "    con.setAutoCommit(false);\n" +
        "    int book = 1;\n" +
        "    int user = 99;\n" +
        "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
        "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
        "        selectStmt.setInt(1, book);\n" +
        "        ResultSet rs = selectStmt.executeQuery();\n" +
        "        if (!rs.next()) {\n" +
        "            Out.println(\"The book is not available!\");\n" +
        "        } else {\n" +
        "            Out.println(\"Book is available!\");\n" +
        "            String insert = \"INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, SYSDATE)\";\n" +
        "            try (\n" +
        "                PreparedStatement insertStmt1 = con.prepareStatement(insert);\n" +
        "                PreparedStatement insertStmt2 = con.prepareStatement(insert);\n" +
        "                PreparedStatement insertStmt3 = con.prepareStatement(insert);\n" +
        "                PreparedStatement updateStmt = con.prepareStatement(\"UPDATE books SET status = 'borrowed' WHERE book_id = ?\")\n" +
        "            ) {\n" +
        "                insertStmt1.setInt(1, user);\n" +
        "                insertStmt1.setInt(2, book);\n" +
        "                insertStmt1.executeUpdate();\n" +
        "\n" +
        "                insertStmt2.setInt(1, 100);\n" +
        "                insertStmt2.setInt(2, 2);\n" +
        "                insertStmt2.executeUpdate();\n" +
        "\n" +
        "                insertStmt3.setInt(1, 101);\n" +
        "                insertStmt3.setInt(2, 1);\n" +
        "                insertStmt3.executeUpdate(); // too much\n" +
        "\n" +
        "                updateStmt.setInt(1, book);\n" +
        "                updateStmt.executeUpdate();\n" +
        "                con.commit();\n" +
        "                Out.println(\"Loan recorded with extra entry!\");\n" +
        "            } catch (SQLException ex) {\n" +
        "                Out.println(\"INSERT/UPDATE ERROR: \" + ex.getMessage());\n" +
        "                con.rollback();\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "} catch (SQLException ex) {\n" +
        "    Out.println(\"DB ERROR: \" + ex.getMessage());\n" +
        "}";

    public static String studentInputTooFewDbRows = "try (Connection con = ds.getConnection()) {\n" +
        "    con.setAutoCommit(false);\n" +
        "    int book = 1;\n" +
        "    int user = 99;\n" +
        "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
        "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
        "        selectStmt.setInt(1, book);\n" +
        "        ResultSet rs = selectStmt.executeQuery();\n" +
        "        if (!rs.next()) {\n" +
        "            Out.println(\"The book is not available!\");\n" +
        "        } else {\n" +
        "            Out.println(\"Book is available!\");\n" +
        "            String update = \"UPDATE books SET status = 'borrowed' WHERE book_id = ?\";\n" +
        "            try (\n" +
        "                PreparedStatement updateStmt = con.prepareStatement(update)\n" +
        "            ) {\n" +
        "                // MISSING: Insert into loans table\n" +
        "                updateStmt.setInt(1, book);\n" +
        "                updateStmt.executeUpdate();\n" +
        "                con.commit();\n" +
        "                Out.println(\"Only book updated, loan not recorded!\");\n" +
        "            } catch (SQLException ex) {\n" +
        "                Out.println(\"UPDATE ERROR: \" + ex.getMessage());\n" +
        "                con.rollback();\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "} catch (SQLException ex) {\n" +
        "    Out.println(\"DB ERROR: \" + ex.getMessage());\n" +
        "}";



    public static String taskSolution = "try (Connection con = ds.getConnection()) {\n" +
            "    con.setAutoCommit(false);\n" +
            "    int book = 1;\n" +
            "    int user = 99;\n" +
            "    String select = \"SELECT * FROM books WHERE book_id = ? AND status = 'available'\";\n" +
            "    try (PreparedStatement selectStmt = con.prepareStatement(select)) {\n" +
            "        selectStmt.setInt(1, book);\n" +
            "        ResultSet rs = selectStmt.executeQuery();\n" +
            "        if (!rs.next()) {\n" +
            "            Out.println(\"The book is not available!\");\n" +
            "        } else {\n" +
            "            Out.println(\"Book is available!\");\n" +
            "            String insert = \"INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, SYSDATE)\";\n"
            +
            "            String update = \"UPDATE books SET status = 'borrowed' WHERE book_id = ?\";\n" +
            "            try (\n" +
            "                PreparedStatement insertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement secondInsertStmt = con.prepareStatement(insert);\n" +
            "                PreparedStatement updateStmt = con.prepareStatement(update)\n" +
            "            ) {\n" +
            "                // erster Eintrag\n" +
            "                insertStmt.setInt(1, user);\n" +
            "                insertStmt.setInt(2, book);\n" +
            "                insertStmt.executeUpdate();\n" +
            "\n" +
            "                // zweiter Eintrag\n" +
            "                secondInsertStmt.setInt(1, 100);\n" +
            "                secondInsertStmt.setInt(2, 2);\n" +
            "                secondInsertStmt.executeUpdate();\n" +
            "\n" +
            "                updateStmt.setInt(1, book);\n" +
            "                updateStmt.executeUpdate();\n" +
            "                con.commit();\n" +
            "                Out.println(\"The book loan was successfully recorded!\");\n" +
            "            } catch (SQLException ex) {\n" +
            "                Out.println(\"INSERT/UPDATE ERROR: \" + ex.getMessage());\n" +
            "                con.rollback();\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "} catch (SQLException ex) {\n" +
            "    Out.println(\"DB ERROR: \" + ex.getMessage());\n" +
            "}";
}
