/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: Hw2
 */

/**
 * Class to define the structure of the linked list
 */
public class LinkedList {
    Node head; //Contains  object of type Node

    public static class Node {  //static inner class to define the structure of node

        Student student; //Contains object of student

        Node next;  //Contains an object of itself

        Node(Student student) {  //Constructor to initialise
            this.student = student;
            next = null;  //the next node is null by default
        }
    }

    /**
     * @param prevItem The current last Node/pointer
     * @param student  The student object that needs to be inserted
     * @param nextNode The following Node which needs to be pointed by the inserted node
     * @return new last node
     */
    public Node insert(Node prevItem, Student student, Node nextNode) {
        Node newStudent = new Node(student);  //Create the new student node
        newStudent.next = nextNode;
        if (prevItem == null)
            this.head = newStudent;  //If linked list is empty, make the new student as the first/head node
        else
            prevItem.next = newStudent; //Connect the previous node next to the new student node, extending the linked list chain
        return newStudent;
    }


}
