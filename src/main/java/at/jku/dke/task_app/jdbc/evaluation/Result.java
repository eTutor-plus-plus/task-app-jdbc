package at.jku.dke.task_app.jdbc.evaluation;

public class Result {
    private Boolean syntaxResult;
    private String syntaxMessage;
    private String syntaxError;
    private Boolean autoCommitResult;
    private String autoCommitMessage;
    private Boolean outputComparisionResult;
    private String outputComparisionMessage;
    private Boolean databaseResult;
    private String databaseMessage;
    private Boolean exceptionResult;
    private String exceptionMessage;
    private String studentQueryResult;


    public Result() {
    }

    public Result(Boolean syntaxResult, String syntaxMessage, String syntaxError,
                  Boolean autoCommitResult, String autoCommitMessage,
                  Boolean outputComparisionResult, String outputComparisionMessage,
                  Boolean databaseResult, String databaseMessage,
                  Boolean exceptionResult, String exceptionMessage) {
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

    public Boolean getOutputComparisionResult() {
        return outputComparisionResult;
    }

    public void setOutputComparisionResult(Boolean outputComparisionResult) {
        this.outputComparisionResult = outputComparisionResult;
    }

    public String getOutputComparisionMessage() {
        return outputComparisionMessage;
    }

    public void setOutputComparisionMessage(String outputComparisionMessage) {
        this.outputComparisionMessage = outputComparisionMessage;
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

    public String getStudentQueryResult() {
        return studentQueryResult;
    }
    
    public void setStudentQueryResult(String studentQueryResult) {
        this.studentQueryResult = studentQueryResult;
    }

    @Override
    public String toString() {
        return "Result :\n\n" +
                // "syntaxResult=" + syntaxResult + "\n" +
                "  Sytnax: " + syntaxMessage + "\n" +
                // ", autoCommitResult=" + autoCommitResult + "\n" +
                "  Autocommit: " + autoCommitMessage + "\n" +
                // ", outputComparisionResult=" + outputComparisionResult + "\n" +
                "  Output: " + outputComparisionMessage + "\n" +
                // ", databaseResult=" + databaseResult + "\n" +
                "  Database content: " + databaseMessage + "\n" +
                // ", exceptionResult=" + exceptionResult + "\n" +
                "  Exceptionhandling: " + exceptionMessage + "\n" +
                "  Student Query Result:\n" + studentQueryResult + "\n" 
                ;
    }
    
}
