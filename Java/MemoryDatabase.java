import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/*
  @author: Kowshick Srinivasan, Qingyun PU
 * @version: 1.0
 * @Assignment: course project-2
 */

/**
 * MemoryDatabase.java
 * <p>
 * Features
 * - Load CSV into an in-memory linked list
 * - Compresses the whole database for disk storage using huffman-code
 * - Build a Red-Black tree using index column for easy search of the student records
 * <p>
 * Usage
 * javac *.java
 * java MemoryDatabase
 * # REPL examples:
 * quit
 **/
public class MemoryDatabase {

    LinkedList list;  //Object of the linked list

    public static final String BASE_PATH = "D:\\Algorithms\\CourseProject-2\\Data"; // target directory

    private static final String FILENAME = "student-data.csv";  //input file name

    private static final String OUTFILE = "student-op-data" + getTimeStamp() + ".csv"; //output file name



    private final Map<String, HuffmanCodec> codec = new HashMap<>(); //huffman encoded student-data set

    private final Map<String, List<String>> encodedDatabase = new HashMap<>();  //Hold the encode student dataset

    //Constructor based dependency injection
    public MemoryDatabase(LinkedList list) {
        this.list = list;

    }

    /**
     * Return the current timestamp in specified format
     *
     * @return timestamp
     */
    private static String getTimeStamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy_HHmmss");
        return now.format(formatter);
    }


    /**
     * Method to filter out only the required columns from the encoded in-memory database
     *
     * @param selectedColumns - Required columns
     * @return Linked list of filtered students
     */
    private LinkedList.Node findStudent(Field[] selectedColumns) {

        LinkedList filteredStudents = new LinkedList(); //Create a new LinkedList for filtered students
        LinkedList.Node current = null; //current node pointer
        int n = encodedDatabase.values().iterator().next().size();  //total columns in student table

        //each columns
        for (int row = 0; row < n; row++) {
            //Array to hold the values of the objects
            Object[] values = new Object[selectedColumns.length];

            //required columns
            for (int col = 0; col < selectedColumns.length; col++) {
                Field cols = selectedColumns[col];
                String column = cols.getName();  //names of the column to be selected
                HuffmanCodec code = codec.get(column); //get the huffman coding tree

                List<String> encodedValuesList = encodedDatabase.get(column); //get the encoded column
                String encodedRow = encodedValuesList.get(row); //locate the value which is required

                Object decoded = code.decode(encodedRow, cols.getType());  //decode them
                values[col] = decoded;
            }
            FilteredStudent filteredStudent = new FilteredStudent(selectedColumns, values);  //create Filtered Student
            current = filteredStudents.insert(current, filteredStudent, null); //create the linked list

        }
        return filteredStudents.head;
    }

    /**
     * Method to verify the correctness of user query against the SQL grammar and
     * retrieve the select query parameters
     *
     * @param userInput - User input select query
     * @return -  object of select query parameters
     */
    private SelectParameters retrieveParameters(String userInput) {
        //Select query grammar, the user input needs to satisfy this grammar to pass
        String sqlGrammar = "select\\s+(.*?)\\s+" +   // group 1 = columns list
                "from\\s+(\\w+)\\s+where\\s+((?:\\w+\\s+equal\\s+\\w+)(?:\\s+and\\s+\\w+\\s+equal\\s+\\w+)*)\\s+" + // group 2 = table name group 3= where col group 4= comp value
                "order\\s+by\\s+(\\w+)\\s+(ASC|DSC)\\s+" +  // group 5 = sort column, group 6 = order
                "with\\s+(bubble_sort|insertion_sort|merge_sort|quick_sort)\\.?"; // group 7 = sort algorithm

        //Compile the SQL grammar i.e. convert Regex string to Grammar
        Pattern pattern = Pattern.compile(sqlGrammar);
        //Match the User input against out grammar
        Matcher m = pattern.matcher(userInput);
        SelectParameters parameters;
        //If user input matches
        if (m.matches()) {
            //Retrieve the parameters from the user input
            parameters = new SelectParameters(Arrays.stream(m.group(1).split(",\\s?")).  //There can be multiple COMMA separated select columns
                    map(MemoryDatabase::convertStringToField).toArray(Field[]::new),   //Convert String to Object.Field
                    m.group(2),    //Retrieve Table name
                    convertStringToField(m.group(4)),  //sort column
                    m.group(5),  //sort order
                    m.group(6)  //Get the sort algorithm to be used
            );


            String whereClause = m.group(3);//Get the where clause
            Pattern condPattern = Pattern.compile("(\\w+)\\s+equal\\s+(\\w+)");
            Matcher matcher = condPattern.matcher(whereClause);
            Map<Field, String> whereMap = new HashMap<>();
            while (matcher.find()) {
                Field colName = convertStringToField(matcher.group(1).trim());
                String comparatorVal = matcher.group(2).trim();
                whereMap.put(colName, comparatorVal);
            }
            parameters.setWhereConditions(whereMap);

        } else
            throw new IllegalArgumentException("Syntax not matching");  //If user-input not matched

        return parameters;
    }

    /**
     * Method to convert user input column string to java object field parameter,
     * it is promised the user string will be same as column name in student class (in memory database table),
     * unless invalid
     *
     * @param columnString - Raw string obtained from user-input
     * @return Object.field equivalent of the string
     */
    private static Field convertStringToField(String columnString) {
        Field matchedField = null;
        for (Field f : Student.class.getDeclaredFields())
            if (f.getName().equalsIgnoreCase(columnString)) {
                matchedField = f;
                break;
            }
        assert matchedField != null;
        return matchedField;
    }


    /**
     * Method used bubble sorting algorithm to sort the filtered student list
     *
     * @param head             - First element of filtered students object
     * @param comparatorColumn -column used to compare the list object
     * @param sortMethod       - To be sorting in Ascending or descending order
     */
    private LinkedList.Node bubbleSort(LinkedList.Node head, Field comparatorColumn, String sortMethod) throws IllegalAccessException {
        boolean flag; //Flag variable
        LinkedList.Node current;  //current node pointer
        LinkedList.Node sortedNode = null; //Before this node all the nodes are sorted

        do {  //uses exit condition loop
            flag = false;
            current = head;

            while (current.next != sortedNode) { //Keep track of the sorted elements
                if (compareTo(current.student, current.next.student, comparatorColumn, sortMethod) > 0) {
                    //Swap two elements
                    Student temp = current.student;
                    current.student = current.next.student;
                    current.next.student = temp;
                    flag = true;
                }

                current = current.next; //move pointer to the next
            }
            sortedNode = current;
            //last element is sorted
        } while (flag); //while every element is swapped, and list is in order

        return head;
    }

    /* ---------- Quick Sort (linked-list recursive partition) ---------- */
    LinkedList.Node quickSort(LinkedList.Node head, Field colName, String asc) throws IllegalAccessException {
        if (head == null) return null;
        head = quickSortRec(head, colName, asc);
        return head;
    }

    private LinkedList.Node quickSortRec(LinkedList.Node start, Field colName, String asc) throws IllegalAccessException {
        if (start == null || start.next == null) return start;
        LinkedList.Node cur = start.next;
        start.next = null;
        LinkedList.Node lessH = null, lessT = null, geH = null, geT = null;

        while (cur != null) {
            LinkedList.Node nxt = cur.next;
            cur.next = null;
            if (compareTo(cur.student, start.student, colName, asc) < 0) {
                if (lessH == null) {
                    lessH = lessT = cur;
                } else {
                    lessT.next = cur;
                    lessT = cur;
                }
            } else {
                if (geH == null) {
                    geH = geT = cur;
                } else {
                    geT.next = cur;
                    geT = cur;
                }
            }
            cur = nxt;
        }

        lessH = quickSortRec(lessH, colName, asc);
        geH = quickSortRec(geH, colName, asc);

        LinkedList.Node headNew = (lessH != null) ? lessH : start;
        if (lessH != null) {
            LinkedList.Node t = lessH;
            while (t.next != null) t = t.next;
            t.next = start;
        }
        start.next = geH;
        return headNew;
    }


    private LinkedList.Node insertionSort(LinkedList.Node head, Field comparatorColumn, String sortMethod) throws IllegalAccessException {

        LinkedList.Node sortedNode = head;  //Keep track of the last sort node, Initially head node is sorted
        LinkedList.Node current = head.next;  //Loop from the second node

        while (current != null) {    //Until end of list
            //current node is less than given Node in sorted list
            // if(current.student.compareTo(sortedNode.student) >=0){
            if (compareTo(current.student, sortedNode.student, comparatorColumn, sortMethod) >= 0) {
                //insert the current node before the given sorted node
                sortedNode = current;
                current = current.next;
            } else {
                LinkedList.Node insert = current;
                current = current.next;
                sortedNode.next = current;

                if (compareTo(insert.student, head.student, comparatorColumn, sortMethod) <= 0) { // If the current node is smaller than the head node
                    //replace head node with current node
                    insert.next = head;
                    head = insert;
                } else {
                    LinkedList.Node prev = head;
                    //Find the next biggest node
                    while (prev.next != null &&
                            compareTo(prev.next.student, insert.student, comparatorColumn, sortMethod) < 0)
                        prev = prev.next;

                    //insert before that node
                    insert.next = prev.next;
                    prev.next = insert;
                }
            }
        }
        return head;  //Complete status

    }

    private int compareTo(Student student, Student student1,
                          Field comparatorColumn, String asc) throws IllegalAccessException {
        int c = student.compare(student1, comparatorColumn);
        return Objects.equals(asc, "ASC") ? c : -c;
    }

    /**
     * Method to use merge sort algorithm to sort the filtered student list
     *
     * @param head-            head of filtered students object
     * @param comparatorColumn - column used to compare the list object
     * @param sortMethod       -To be sorting in Ascending or descending order
     */
    private LinkedList.Node mergeSort(LinkedList.Node head, Field comparatorColumn, String sortMethod) throws IllegalAccessException {
        if (head == null || head.next == null) return head; //base case
        //Find the middle element
        LinkedList.Node middle = findMiddle(head);
        LinkedList.Node nextOfMiddle = middle.next;
        middle.next = null;

        //create left sub list from 0th element to middle-1 element
        LinkedList.Node left = mergeSort(head, comparatorColumn, sortMethod);
        //create left sub list from middle element to last element
        LinkedList.Node right = mergeSort(nextOfMiddle, comparatorColumn, sortMethod);        //recursively split the sub list until the base condition


        //merge the lists to sort the elements
        return merge(left, right, comparatorColumn, sortMethod);
    }

    /**
     * Method to find the middle node of the given Linked List
     *
     * @param head - begin node of the linked list
     * @return the middle node
     */
    private LinkedList.Node findMiddle(LinkedList.Node head) {
        if (head == null) return null;
        LinkedList.Node fast = head;
        LinkedList.Node slow = head;
        while (fast.next != null && fast.next.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }


    /**
     * @param left             Left sub list
     * @param right            Right sub List
     * @param comparatorColumn column used to compare
     * @param sortMethod       sorting method Ascending/descending
     * @return sorted head
     */
    private LinkedList.Node merge(LinkedList.Node left, LinkedList.Node right,
                                  Field comparatorColumn, String sortMethod) throws IllegalAccessException {
        if (left == null) return right;
        if (right == null) return left;
        if (compareTo(left.student, right.student, comparatorColumn, sortMethod) < 0) { // left is smaller than the right node
            //put left to the list
            left.next = merge(left.next, right, comparatorColumn, sortMethod);
            return left;
        } else {   //left is greater or equal
            //put right to the list
            right.next = merge(left, right.next, comparatorColumn, sortMethod);
            return right;
        }
    }

    /**
     * Method recursively read each student details from the CSV file and adds it to the end of the linked list
     *
     * @param br      : The BufferedReader object
     * @param current : Current Node/pointer of the linked list
     * @return : Completion status of the read operation
     */
    private int read(BufferedReader br, LinkedList.Node current) throws IOException {
        String l = br.readLine();  //Read each line
        if (l == null) return 0;  //When reached EOF return
        String[] line = l.split(",");  //Split the row using "," as the separator
        Student student = convertLineToStudent(line);  //Convert the String array to the student class
        return read(br, list.insert(current, student, null));   //Recursively call the function with the buffered reader object and updated last Node/Pointer
    }


    /**
     * Accepts the String array and convert it to student object
     *
     * @param line Original String array formed after comma separation of the line
     * @return Student object
     */
    private Student convertLineToStudent(String[] line) {
        return new Student(line[0], line[1].charAt(0), Integer.parseInt(line[2]), line[3].charAt(0), line[4], line[5].charAt(0),
                Integer.parseInt(line[6]), Integer.parseInt(line[7]), line[8], line[9], line[10], line[11],
                Integer.parseInt(line[12]), Integer.parseInt(line[13]), Integer.parseInt(line[14]), line[15].equals("yes"), line[16].equals("yes"), line[17].equals("yes"), line[18].equals("yes"), line[19].equals("yes"),
                line[20].equals("yes"), line[21].equals("yes"), line[22].equals("yes"), Integer.parseInt(line[23]), Integer.parseInt(line[24]), Integer.parseInt(line[25]), Integer.parseInt(line[26]),
                Integer.parseInt(line[27]), Integer.parseInt(line[28]), Integer.parseInt(line[29]), line[30].equals("yes"));

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        MemoryDatabase database = new MemoryDatabase(
                new LinkedList()  //Initialize the list object
        );
        File inputFile = new File(BASE_PATH, FILENAME);
        File outputFile = new File(BASE_PATH, OUTFILE);
        //Since we used try with resources, we won't be needing a finally block
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile)); //Buffered reader to enable to the Java methods to read from the given file
             PrintWriter printWriter = new PrintWriter(new FileWriter(outputFile))) //Buffered writer to enable the Java methods to write on the given file
        {
            br.readLine();  //Skip the Header of the csv
            database.read(br, database.list.head);   //Read the student details from the file and add it to the linked list
            database.createHuffman();  //identify the huffman encoding pattern.

            database.encodeDatabase();  //encode the whole student database, can store physically

            System.out.println("Write the select query");
            System.out.println("SQL> ");

            //Get the select query from the user
            String userInput = scanner.nextLine();

            //Check an retrieve parameters
            SelectParameters parameters = database.retrieveParameters(userInput);
            //filter only required column
            LinkedList.Node filteredStudentList = database.findStudent(parameters.getColumns());

            //Draw a Red-Black tree to ease out the searching process, the tree index is based on the where clause
            filteredStudentList = database.createRBTree(filteredStudentList, parameters.getWhereConditions());

            //Sort the filtered Object using the selected algorithm
            filteredStudentList = switch (parameters.getSortAlgorithm()) {
                case "bubble_sort" ->
                        database.bubbleSort(filteredStudentList, parameters.getSortColumn(), parameters.getSortMethod());
                case "insertion_sort" ->
                    // Insertion Sort
                        database.insertionSort(filteredStudentList, parameters.getSortColumn(), parameters.getSortMethod());
                case "merge_sort" ->
                        database.mergeSort(filteredStudentList, parameters.getSortColumn(), parameters.getSortMethod());
                default ->
                    //quick Sort
                        database.quickSort(filteredStudentList, parameters.getSortColumn(), parameters.getSortMethod());
            };
            //Get the columns header in a comma separated string
            String head = Arrays.stream(parameters.getColumns())
                    .map(Field::getName).
                    collect(Collectors.joining(","));


            printWriter.println(head);  //print the header row of the csv
            //Print rest of the rows
            database.print(filteredStudentList, printWriter);
            System.out.println("Refer the created log files for Huffman and Red-black trees");
            System.out.println("Output save to " + OUTFILE);
        } catch (Exception e) { //Global catch block to handle all the exception thrown by the program
            e.printStackTrace(System.out);  //print the exception stack trace in console
        }
    }

    /**
     * Generate the huffman tree, for each column of the in-memory database
     */
    private void createHuffman() {
        Map<String, Map<String, Integer>> columnFrequencies = new HashMap<>();
        //loop through entire linked list and count frequencies of
        //elements column-wise
        LinkedList.Node currentNode = list.head;
        while (currentNode != null) {
            Student student = currentNode.student;

            //Generic way to iterate through each column of student class
            for (Field field : Student.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object val = field.get(student);
                    String colName = field.getName();
                    String token = String.valueOf(val);

                    columnFrequencies.putIfAbsent(colName, new HashMap<>());
                    Map<String, Integer> freqMap = columnFrequencies.get(colName);

                    freqMap.put(token, freqMap.getOrDefault(token, 0) + 1);


                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            currentNode = currentNode.next;
        }


        //Create the huffman tree for each column and store the reference
        for (Map.Entry<String, Map<String, Integer>> entry : columnFrequencies.entrySet())
            codec.put(entry.getKey(), HuffmanCodec.fromFrequencies(entry.getValue(), true));

    }


    /**
     * Method is used to encode the entire in-memory database using huffman code,
     * This is advantageous during saving the table as it reduces the size of the data
     */
    private void encodeDatabase() {
        LinkedList.Node curr = list.head;
        while (curr != null) {
            Student currentStudent = curr.student;

            //Generic way to iterate through each column of student class
            for (Field field : Student.class.getDeclaredFields()) {
                field.setAccessible(true);
                String colName = field.getName();
                HuffmanCodec code = codec.get(colName);
                if (code == null) continue;
                try {
                    Object val = field.get(currentStudent);
                    if (val == null) continue;

                    String token = String.valueOf(val);  //get the col value in string
                    //encode each value using the corresponding huffman code
                    String bitString = code.encode(token);
                    //hold the entire database
                    //We can also store this physically
                    encodedDatabase.computeIfAbsent(colName, k -> new ArrayList<>()).add(bitString);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
            curr = curr.next;
        }

    }

    /**
     * Method to create the red back tree for the given in-memory database
     */
    private LinkedList.Node createRBTree(LinkedList.Node filteredStudentList, Map<Field, String> whereCondition) throws IllegalAccessException {
        List<HuffmanCodec> codecs = new ArrayList<>();
        List<Field> columns = whereCondition.keySet().stream().toList();//where condition columns
        List<String> comparatorValues = whereCondition.values().stream().toList(); //comparator value

        for (Field column : columns) {
            codecs.add(codec.get(column.getName()));
        }

        RedBlackTree tree = new RedBlackTree(codecs);
        LinkedList.Node current = filteredStudentList;
        //Loop until the end of the linked-list
        while (current != null) {

            tree.add(current, current.student.convertFieldToColumn(columns)); //build RB tree based on the where condition columns
            //making it easier for search operation
            current = current.next;
        }
        LinkedList filteredStudent = new LinkedList();

        tree.writeToFile("Final Red-Black Tree");
        //print the constructed tree
        tree.writeTreeToFile();

        //walk thew tree to find the tree node with the given index
        List<FilteredStudent> filteredStudents = tree.search(comparatorValues);
        //Create a new LinkedList for filtered students
        for (FilteredStudent fs : filteredStudents) {
            current = filteredStudent.insert(current, fs, null);
        }
        return filteredStudent.head;

    }

    /**
     * Recursively prints the student data to the csv file
     *
     * @param current     : Accepts the current NOde/pointer of the Linked List
     * @param printWriter : Accepts the Printed writer object
     * @return : return the completion status of the printing action
     */
    private int print(LinkedList.Node current, PrintWriter printWriter) {
        String row = current.student.toString(); //Converts the student object to comma separated string
        printWriter.println(row);  //Prints the string to the csv file
        if (current.next == null)  //When reached end node return completion status
            return 0;
        return print(current.next, printWriter); //recursively call the print method with updated last node pointer
    }


}

