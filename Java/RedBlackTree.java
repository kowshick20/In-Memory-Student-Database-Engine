/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: course project-2
 */


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for the creation/manipulation of the red black tree
 */
public class RedBlackTree {
    private final List<HuffmanCodec> codec;  //Knows huffman tree values
    private RBTreeNode root;  //keeps track of the root node of the tree

    public RedBlackTree(List<HuffmanCodec> codec) {
        this.codec = codec;
    }


    /**
     * Accepts the recently inserted tree node and balances the entire tree
     * created until this point accordingly
     *
     * @param current RB Tree Node
     */
    private void balanceTree(RBTreeNode current) {
        RBTreeNode uncleNode;
        //Until there is red-red node conflict
        while (current.parent.isRed) {
            if (current.parent == current.parent.parent.right) { //parent is the right child of grandparent
                uncleNode = current.parent.parent.left;  //determine the Uncle node, which is the left child of the grandparent

                //If uncle is present, and it is a red node then recolor as follows
                if (uncleNode != null && uncleNode.isRed) {
                    uncleNode.isRed = false;  //Make uncle node to be black
                    current.parent.isRed = false; //Make the parent node to be black
                    current.parent.parent.isRed = true; //Make the grandparent node to be red
                    //Balance up to grandparent level,
                    // now move up to the grandparent level and check the ancestor nodes
                    current = current.parent.parent;

                } else { //either uncle node is absent or it is black, do rotations and recolor

                    if (current == current.parent.left) {  //it is a left child
                        current = current.parent;
                        rightRotate(current); //do a right rotation,and make the grandparent-parent-child subtree left biased
                    }
                    //if is right child then  the grandparent-parent-child subtree is left biased
                    current.parent.isRed = false;   //make the parent node to black
                    current.parent.parent.isRed = true;  //make grandparent node to be red
                    leftRotate(current.parent.parent);  //do a whole left rotation, making the parent the grandparent, grandparent left child and child stays as right child
                }
            } else {  //if parent is a left child of the grandparent, this functionality is basically the mirror image of the above
                uncleNode = current.parent.parent.right;//determine the Uncle node, which is the right child of the grandparent

                //If uncle is present, and it is a red node then recolor as follows
                if (uncleNode != null && uncleNode.isRed) {
                    uncleNode.isRed = false; //Make uncle node to be black
                    current.parent.isRed = false; //Make the parent node to be black
                    current.parent.parent.isRed = true; //Make the grandparent node to be red
                    //Balance up to grandparent level,
                    // now move up to the grandparent level and check the ancestor nodes
                    current = current.parent.parent;
                } else { //either uncle node is absent or it is black, do rotations and recolor
                    if (current == current.parent.right) { //it is a right child
                        current = current.parent;
                        leftRotate(current); //do a left rotation,and make the grandparent-parent-child subtree right biased
                    }
                    //if is left child then  the grandparent-parent-child subtree is right biased
                    current.parent.isRed = false; //make parent node to black
                    current.parent.parent.isRed = true;  //make grandparent node to red
                    rightRotate(current.parent.parent); //do a whole left rotation, making the parent the grandparent, grandparent left child and child stays as right child
                }
            }
            //There won't be any parents for root, so break the cycle
            if (current == root) {
                break; //this acts as an exit condition
            }
        }
        root.isRed = false;   //Root is always Black irrespective of any cases
    }

    /**
     * method left rotates the nodes through the given anchor node
     *
     * @param current Anchor node
     */
    private void leftRotate(RBTreeNode current) {
        RBTreeNode placeholder = current.right;
        current.right = placeholder.left;
        if (placeholder.left != null) {
            placeholder.left.parent = current;
        }
        placeholder.parent = current.parent;
        if (current.parent == null) {
            this.root = placeholder;
        } else if (current == current.parent.left) {
            current.parent.left = placeholder;
        } else {
            current.parent.right = placeholder;
        }
        placeholder.left = current;
        current.parent = placeholder;
    }

    /**
     * method right rotates the nodes through the given anchor node
     *
     * @param anchor Anchor node
     */
    private void rightRotate(RBTreeNode anchor) {
        RBTreeNode placeholder = anchor.left;  //hold left child of anchor node
        anchor.left = placeholder.right; //swap left and right child of the anchor
        if (placeholder.right != null) {  //right child is present
            placeholder.right.parent = anchor;
        }
        placeholder.parent = anchor.parent;
        if (anchor.parent == null) {
            this.root = placeholder;
        } else if (anchor == anchor.parent.right) {
            anchor.parent.right = placeholder;
        } else {
            anchor.parent.left = placeholder;
        }
        placeholder.right = anchor;
        anchor.parent = placeholder;
    }

    public void add(LinkedList.Node studentRecord, List<String> key) {
        String bits = null;
        for (int i = 0; i < key.size(); i++)
            bits += codec.get(i).encode(key.get(i));
        int k = rbKeyFromBits(bits);
        insert(k, studentRecord);
    }

    /**
     * @param index         index of the RB tree
     * @param studentRecord reference to the student record
     */
    public void insert(int index, LinkedList.Node studentRecord) {

        RBTreeNode rbTreeNode = new RBTreeNode(index, studentRecord); //create a new Red-Black tree node

        RBTreeNode parent = null; //keep track of the parent node
        RBTreeNode current = this.root;  //Keep track of the current node

        //loop to find the appropriate parent node to add the new node as child
        while (current != null) {
            parent = current; //slow pointer keep tracks of current's parent
            if (rbTreeNode.key < current.key) { //new index is less than current node
                current = current.left; //move to the left
            } else if (rbTreeNode.key > current.key) { //new index is greater than the current node
                current = current.right; //move to the right
            } else { //new index is equal to the current node
                current.studentRef.add(studentRecord); //make the student point to the same red black node.
                //This reduces duplicate tree nodes/tree size and thus reducing the search space
                break;
            }
        }

        rbTreeNode.parent = parent;
        if (parent == null) {  //no root node
            this.root = rbTreeNode; //make the new node as the root node
        } else if (rbTreeNode.key < parent.key) { //if index of new node is less than parent's
            parent.left = rbTreeNode; //make the new node as left child
        } else if (rbTreeNode.key > parent.key) { //if index of new node is greater than parent's
            parent.right = rbTreeNode; //make the new node as right child
        }

        if (rbTreeNode.parent == null) {
            rbTreeNode.isRed = false;  //root node is always black
            return; //no need to balance
        }

        if (rbTreeNode.parent.parent == null) { //no grandparent?
            return; //no need to balance
        }

        balanceTree(rbTreeNode); //balance the tree
    }


    /**
     * Exposed method to print the RB tree
     */
    public void printTree() {
        printTree(this.root, "", true); //calls the actual method that prints the RB tree
    }

    /**
     * Recursive method that print the Red-Black tree
     *
     * @param current node to be printed
     * @param level   tree level/indentation level
     * @param last    is last node? A parent has two child left and right only in BST (RB => BST)
     */
    private void printTree(RBTreeNode current, String level, boolean last) {
        if (current != null) {  //current node is not null
            System.out.print(level);
            if (last) {
                System.out.print("R----");
                level += "   ";
            } else {
                System.out.print("L----");
                level += "|  ";
            }

            String nodeColor = current.isRed ? "RED" : "BLACK";  //determine the color
            System.out.println(current.key + "(" + nodeColor + ")" + current.studentRef.getFirst().student.toString());
            printTree(current.left, level, false); //recursively call the same function moving to the left child
            printTree(current.right, level, true); //recursively call the same function moving to the right child
        }
    }

    /**
     * This method is used to traverse the RB-tree and search for the given element,
     * Traveling is same as traversing a BST
     * @param keySearch Keys used to be searched in the RB tree
     * @return the list of student references
     */
    public List<FilteredStudent> search(List<String> keySearch) {
        int key = findTreeNodeId(keySearch);
        List<FilteredStudent> filteredStudents = new ArrayList<>();
        RBTreeNode current = this.root;  //Keep track of the current node
        //loop to find the appropriate parent node to add the new node as child
        while (current != null) {
            if (key < current.key) { //new index is less than current node
                current = current.left; //move to the left
            } else if (key > current.key) { //new index is greater than the current node
                current = current.right; //move to the right
            } else { //new index is equal to the current node
                current.studentRef.forEach(e -> filteredStudents.add((FilteredStudent) e.student));
                break;
            }
        }
        return filteredStudents;
    }

    private int findTreeNodeId(List<String> key) {
        String bits = null;
        for (int i = 0; i < key.size(); i++)
            bits += codec.get(i).encode(key.get(i));

        return rbKeyFromBits(bits);
    }

    /**
     * Convert Huffman bit-string into an int key for the RB-tree.
     */
    private static int rbKeyFromBits(String bits) {
        // Keep leading zeros by adding a head '1'
        String withHead = "1" + bits;
        try {
            BigInteger bi = new BigInteger(withHead, 2);
            if (bi.bitLength() <= 31) {
                return bi.intValue();
            } else {
                // Very rare for our dataset; still deterministic within the same run/codebook
                return withHead.hashCode();
            }
        } catch (Exception e) {
            return withHead.hashCode();
        }
    }


}