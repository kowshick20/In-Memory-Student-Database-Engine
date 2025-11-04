/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: course project-2
 */

import java.util.ArrayList;
import java.util.List;

public class RBTreeNode {
    int key;   //Student Absences
    List<LinkedList.Node> studentRef;  //refer to the actual linked list
    RBTreeNode left, right, parent; //pointers to left right and parent nodes
    boolean isRed; //is the node red?

    RBTreeNode(int absences, LinkedList.Node studentRecord) {
        this.key = absences;  //Nodes are indexed by absences
        this.studentRef = new ArrayList<>();  //Student records to be referenced by the tree node
        studentRef.add(studentRecord); //add a reference pointer
        this.isRed = true;   //All new nodes are added in red.
    }

}
