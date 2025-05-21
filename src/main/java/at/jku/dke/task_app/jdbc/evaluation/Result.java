package at.jku.dke.task_app.jdbc.evaluation;

import java.util.List;


/**
 * This class contains the evaluation results of a student's JDBC submission.
 * It contains results and feedback messages for various evaluation criteria
 */
public class Result {
    // --- Syntax Check ---
    /** True if syntax is valid, false otherwise. */
    private Boolean syntaxResult;
    /** Message explaining the result of the syntax check. */
    private String syntaxMessage = "";
    /** Error details if syntax is invalid. */
    private String syntaxError = "";

    // --- Autocommit Check ---
    /** True if autocommit is correctly disabled. */
    private Boolean autoCommitResult;
    /** Feedback message for autocommit check. */
    private String autoCommitMessage = "";

    // --- Output Comparison ---
    /** True if console output matches the expected result. */
    private Boolean outputComparisionResult;
    /** Feedback message for output comparison. */
    private String outputComparisionMessage = "";

    // --- Database Comparison ---
    /** True if the resulting database state is correct. */
    private Boolean databaseResult;
    /** Feedback message for database content evaluation. */
    private String databaseMessage = "";


    // --- Exception Handling ---
    /** True if exception handling is correct. */
    private Boolean exceptionResult;
    /** Feedback message for exception handling. */
    private String exceptionMessage = "";

    // --- Detailed Results ---
    /** Final query result of the student's code (as structured table dumps). */
    private List<TableDump> studentQueryResult;
    /** Tuples that are missing in the student's result compared to the expected one. */
    private List<List<String>> missingTuples;
    /** Tuples that are present in the student's result but shouldn't be. */
    private List<List<String>> superfluousTuples;

    /** The console output of the student's code. */
    private String studentOutput = "";
    /** Lines that are missing in the output. */
    private List<String> missingOutputs;
    /** Lines that are superfluous in the output. */
    private List<String> superfluousOutputs;
    /** Database content before the execution of the students' code */
    private List<TableDump> databaseBefore;

    /**
     * Default constructor.
     */
    public Result() {
    }

    /**
     * Constructor initializing all fields relevant to evaluation, excluding tuples and table data.
     *
     * @param syntaxResult              True if syntax is correct
     * @param syntaxMessage             Syntax feedback message
     * @param syntaxError               Syntax error details (if any)
     * @param autoCommitResult          Result of autocommit check
     * @param autoCommitMessage         Feedback on autocommit
     * @param outputComparisionResult   Result of output comparison
     * @param outputComparisionMessage  Feedback on output comparison
     * @param databaseResult            Result of database state comparison
     * @param databaseMessage           Feedback on database content
     * @param exceptionResult           Result of exception handling
     * @param exceptionMessage          Feedback on exception handling
     * @param studentOutput             Console output
     * @param superfluousOutputs        Output lines that were too much
     * @param missingOutputs            Output lines that were missing
     * @param studentQueryResult        Result of student's database query as table dumps
     * @param missingTuples             Missing tuples in student's query result
     * @param superfluousTuples         Extra tuples in student's query result
     * @param databaseBefore            Database content before the execution of the students' code
     */
    public Result(Boolean syntaxResult, String syntaxMessage, String syntaxError,
                  Boolean autoCommitResult, String autoCommitMessage,
                  Boolean outputComparisionResult, String outputComparisionMessage,
                  Boolean databaseResult, String databaseMessage,
                  Boolean exceptionResult, String exceptionMessage,
                  String studentOutput, List<String> superfluousOutputs, List<String> missingOutputs,
                  List<TableDump> studentQueryResult, List<List<String>> missingTuples, List<List<String>> superfluousTuples, List<TableDump> databaseBefore) {
        this.syntaxResult = syntaxResult;
        this.syntaxMessage = syntaxMessage;
        this.syntaxError = syntaxError;
        this.autoCommitResult = autoCommitResult;
        this.autoCommitMessage = autoCommitMessage;
        this.outputComparisionResult = outputComparisionResult;
        this.outputComparisionMessage = outputComparisionMessage;
        this.databaseResult = databaseResult;
        this.databaseMessage = databaseMessage;
        this.exceptionResult = exceptionResult;
        this.exceptionMessage = exceptionMessage;
        this.studentOutput = studentOutput;
        this.missingOutputs = missingOutputs;
        this.superfluousOutputs = superfluousOutputs;
        this.studentQueryResult = studentQueryResult;
        this.missingTuples = missingTuples;
        this.superfluousTuples = superfluousTuples;
        this.databaseBefore = databaseBefore;
    }

    public Boolean getSyntaxResult() {
        return syntaxResult;
    }

    public void setSyntaxResult(Boolean syntaxResult) {
        this.syntaxResult = syntaxResult;
    }

    public String getSyntaxMessage() {
        return syntaxMessage;
    }

    public void setSyntaxMessage(String syntaxMessage) {
        this.syntaxMessage = syntaxMessage;
    }

    public String getSyntaxError() {
        return syntaxError;
    }

    public void setSyntaxError(String syntaxError) {
        this.syntaxError = syntaxError;
    }

    public Boolean getAutoCommitResult() {
        return autoCommitResult;
    }

    public void setAutoCommitResult(Boolean autoCommitResult) {
        this.autoCommitResult = autoCommitResult;
    }

    public String getAutoCommitMessage() {
        return autoCommitMessage;
    }

    public void setAutoCommitMessage(String autoCommitMessage) {
        this.autoCommitMessage = autoCommitMessage;
    }

    public Boolean getOutputComparisonResult() {
        return outputComparisionResult;
    }

    public void setOutputComparisonResult(Boolean outputComparisionResult) {
        this.outputComparisionResult = outputComparisionResult;
    }

    public String getOutputComparisonMessage() {
        return outputComparisionMessage;
    }

    public void setOutputComparisonMessage(String outputComparisonMessage) {
        this.outputComparisionMessage = outputComparisonMessage;
    }

    public Boolean getDatabaseResult() {
        return databaseResult;
    }

    public void setDatabaseResult(Boolean databaseResult) {
        this.databaseResult = databaseResult;
    }

    public String getDatabaseMessage() {
        return databaseMessage;
    }

    public void setDatabaseMessage(String databaseMessage) {
        this.databaseMessage = databaseMessage;
    }

    public Boolean getExceptionResult() {
        return exceptionResult;
    }

    public void setExceptionResult(Boolean exceptionResult) {
        this.exceptionResult = exceptionResult;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public List<TableDump> getStudentQueryResult() {
        return studentQueryResult;
    }

    public void setStudentQueryResult(List<TableDump> studentQueryResult) {
        this.studentQueryResult = studentQueryResult;
    }

    public List<List<String>> getMissingTuples() {
        return missingTuples;
    }

    public void setMissingTuples(List<List<String>> missingTuples) {
        this.missingTuples = missingTuples;
    }

    public List<List<String>> getSuperfluousTuples() {
        return superfluousTuples;
    }

    public void setSuperfluousTuples(List<List<String>> superfluousTuples) {
        this.superfluousTuples = superfluousTuples;
    }

    public String getStudentOutput() {
        return studentOutput;
    }

    public void setStudentOutput(String studentOutput) {this.studentOutput = studentOutput;}

    public List<String> getMissingOutputs() {
        return missingOutputs;
    }

    public void setMissingOutputs(List<String> missingOutputs) {
        this.missingOutputs = missingOutputs;
    }

    public List<String> getSuperfluousOutputs() {
        return superfluousOutputs;
    }

    public void setSuperfluousOutputs(List<String> superfluousOutputs) {
        this.superfluousOutputs = superfluousOutputs;
    }

    public void setDatabaseBefore(List<TableDump> databaseBefore) {this.databaseBefore = databaseBefore;}

    public List<TableDump> getDatabaseBefore() {return databaseBefore;}
    /**
     * Returns a string representation of the result
     *
     * @return String containing a summary of the evaluation results
     */
    @Override
    public String toString() {
        return "Result :\n\n" +
                "  Sytnax: " + syntaxMessage + "\n" +
                "  Autocommit: " + autoCommitMessage + "\n" +
                "  Output: " + outputComparisionMessage + "\n" +
                "  Database content: " + databaseMessage + "\n" +
                "  Exceptionhandling: " + exceptionMessage + "\n" +
                "  Student Query Result:\n" + studentQueryResult + "\n"
                ;
    }

}
