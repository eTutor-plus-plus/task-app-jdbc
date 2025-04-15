package at.jku.dke.task_app.jdbc.evaluation;

import java.util.List;

public record TableDump(String tableName, List<String> columns, List<List<String>> rows) {}
