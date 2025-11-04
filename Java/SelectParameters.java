/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: course project-1
 */


import java.lang.reflect.Field;
import java.util.Map;

//Class to store the SQL select parameters
public class SelectParameters {
    private final Field[] columns;  //Hold the required columns for filtering
    private final String table;  //Hold the table name
    private Map<Field, String> whereConditions;  //hold the where conditions and their corresponding comparision value
    private final Field sortColumn;  //Column to be sorted based on
    private final String sortMethod;  //ASC/DSC
    private final String sortAlgorithm;  //method used to sort

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
