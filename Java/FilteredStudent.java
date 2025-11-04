/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: course project-2
 */


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Reflection/subclass of the student class
 */
public class FilteredStudent extends Student {  //Inherit from parent class.
    private final Field[] filteredColumns;   //List of columns that needs to be filtered
    //The values of each column,
    // since we don't know the exact datatype we use the parent type Object.
    private final Object[] values;

    //Constructor parameter to inject list of filed and values
    public FilteredStudent(Student base, Field[] filteredColumns, Object[] values) {
        super(base);
        this.filteredColumns = filteredColumns;
        this.values = values;
    }


    /**
     * Method that compares this filterStudent object to the given studentObject using the comparision column
     *
     * @param matchedField - Sort based on this column
     * @param student      - to be compared class
     * @return integer specifying comparision result
     */
    @Override
    public int compare(Student student, Field matchedField) throws IllegalAccessException {
        FilteredStudent otherFilteredStudent = (FilteredStudent) student;
        for (int i = 0; i < filteredColumns.length; i++) {
            if (filteredColumns[i].equals(matchedField)) {  //Find the comparision column
                //Will be type implicitly type case cast to the corresponding Non-Primitive/Reference datatype
                // i.e. Object of type int will be converted to Integer
                Object v1 = values[i];
                Object v2 = otherFilteredStudent.values[i];
                if (v1 instanceof Comparable && v2 instanceof Comparable) { //All Non-primitive datatypes are instance of Comparable interface
                    return ((Comparable) v1).compareTo(v2);  //Compare the two column values
                } else {
                    throw new IllegalArgumentException("Cannot not comparable this column");  // if not instance of comparable
                }
            }
        }
        return super.compare(student, matchedField);  //Column not found in select check parent class
    }

    /**
     * Converts the filterStudent object to a string
     *
     * @return Comma separated String value
     */
    @Override
    public String toString() {
        return Arrays.stream(values).map(e -> {
            if (e instanceof Boolean b)  //If element is of type Boolean, convert to "yes", "no"
                return b ? "yes" : "no";
            return String.valueOf(e);  //get the element as string
        }).collect(Collectors.joining(",")); //separate them using ","
    }

    /**
     * This constructor is used to create a subclass of student
     * when the filtered columns and its corresponding values are available
     * @param selectedColumns List of required columns
     * @param values Corresponding values
     */
    public FilteredStudent(Field[] selectedColumns, Object[] values) {
        this.filteredColumns = selectedColumns;
        this.values = values;
        //Assign the values to the correct column name.
        for (int i = 0; i < filteredColumns.length; i++) {
            Field field = filteredColumns[i];
            Object value = values[i];
            try {
                field.setAccessible(true);
                field.set(this, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


}
