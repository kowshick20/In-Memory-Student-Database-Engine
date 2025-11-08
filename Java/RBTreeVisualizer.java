import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class generates a visualization for Red-black tree, generating a GraphViz dot file
 * and the dot can be converted into a png file.
 */
public class RBTreeVisualizer {

    /**
     * Generates the completed tree and save in the .dot format, from the root node
     * @param root : root node of the red-black tree
     * @param filename : filename to save the dot file
     */
    public static void saveAsDot(RBTreeNode root, String filename) {
        StringBuilder tree = new StringBuilder();

        //dot headers
        tree.append("digraph RBTree {\n");
        tree.append("  node [shape=circle, style=filled, fontname=\"Arial\"];\n");
        tree.append("  edge [color=gray];\n");

        //function to traverse each node and create dot equivalent
        generateDot(root, tree);

        tree.append("}\n"); //close the dot
        File file = new File(MemoryDatabase.BASE_PATH,filename);
        //write the file physically
        try (FileWriter fw = new FileWriter(file+ ".dot")) {
            fw.write(tree.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param node Current node
     * @param tree dot string builder containing the tree
     */
    // Recursively add nodes and edges
    private static void generateDot(RBTreeNode node, StringBuilder tree) {
        if (node == null) return;  //exit case

        // set node color based on Red-black tree node
        String color = node.isRed ? "red" : "black";
        String fontColor = "white";  //Write the text in white

        //Add dot equivalent of the current node
        // is drawn as a circle (Red/Black) and text written in white
        tree.append(String.format("  \"%s\" [label=\"%s\", fillcolor=\"%s\", fontcolor=\"%s\"];\n",
                node.key, node.key, color, fontColor));

        //move left
        if (node.left != null) {
            //draw edge to the left
            tree.append(String.format("  \"%s\" -> \"%s\";\n", node.key, node.left.key));
            //Recursive call to draw the left child
            generateDot(node.left, tree);
        } else {
            //Add a null node if left is absent
            String nullNode = node.key + "_L";
            tree.append(String.format("  \"%s\" [style=invis];\n", nullNode));
            tree.append(String.format("  \"%s\" -> \"%s\" [style=invis];\n", node.key, nullNode));
        }

        //move right
        if (node.right != null) {
            //draw edge to the right
            tree.append(String.format("  \"%s\" -> \"%s\";\n", node.key, node.right.key));
            //recursive call
            generateDot(node.right, tree);
        } else {
            //Add a null node
            String nullNode = node.key + "_R";
            tree.append(String.format("  \"%s\" [style=invis];\n", nullNode));
            tree.append(String.format("  \"%s\" -> \"%s\" [style=invis];\n", node.key, nullNode));
        }
    }

}
