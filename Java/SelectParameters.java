/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: course project-1
 */


import java.lang.reflect.Field;
import java.util.Map;

//Record to store the SQL select parameters
public class SelectParameters {
    private final Field[] columns;
    private final String table;
    //                               Field whereColumn,
//                               String comparatorValue,
//                               Field whereColumn2,
//                               String comparatorValue2,
    private Map<Field, String> whereConditions;
    private final Field sortColumn;
    private final String sortMethod;
    private final String sortAlgorithm;

    public SelectParameters(Field[] columns, String table, Field sortColumn, String sortMethod, String sortAlgorithm) {
        this.columns = columns;
        this.table = table;
        this.sortColumn = sortColumn;
        this.sortMethod = sortMethod;
        this.sortAlgorithm = sortAlgorithm;
    }

    public void setWhereConditions(Map<Field, String> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public Field[] getColumns() {
        return columns;
    }

    public String getTable() {
        return table;
    }

    public Map<Field, String> getWhereConditions() {
        return whereConditions;
    }

    public Field getSortColumn() {
        return sortColumn;
    }

    public String getSortMethod() {
        return sortMethod;
    }

    public String getSortAlgorithm() {
        return sortAlgorithm;
    }
}
