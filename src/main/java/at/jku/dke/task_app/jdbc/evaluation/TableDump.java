package at.jku.dke.task_app.jdbc.evaluation;

import java.util.List;

/**
 * Repräsentiert den Zustand einer einzelnen Tabelle innerhalb einer Datenbank
 *
 * @param tableName Der Name der Tabelle (z.B. {"users"}).
 * @param columns Eine Liste der Spaltennamen in der Reihenfolge ihres Auftretens.
 * @param rows Eine Liste von Zeileninhalten; jede Zeile ist eine Liste von Strings,
 *             wobei jede Zelle dem entsprechenden Spaltennamen entspricht.
 */
public record TableDump(String tableName, List<String> columns, List<List<String>> rows) {}
