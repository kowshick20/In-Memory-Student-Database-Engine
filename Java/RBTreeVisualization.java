/*
  @author: Kowshick Srinivasan
 * @version: 1.0
 * @Assignment: course project-2
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Class to create the visualization of the Red Black tree using Java GUI programming (swing)
 */
public class RBTreeVisualization extends JPanel {
    private static final int NODE_GAP = 130;   //The initial gap between two nodes
    private final RBTreeNode root;//root node of the RB tree
    private final int width = 1920;  //define the initial width
    private final int height = 1080; //define the initial height
    public RBTreeVisualization(RBTreeNode root) {
        this.root = root;
        setBackground(Color.WHITE);  //Background color of the image is always white
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (root != null)
            drawTreeNode(g, root, getWidth() / 2, 50, getWidth() / 4 );  //If root node is present draw the node, by reducing the horizontal width be 2
    }

    /**
     * Recursive function to draw the node of the tree
     *
     * @param graphics : Canvas used to draw the tree
     * @param node:    Current tree node
     * @param x        : x-axis position of the node center
     * @param y        : y-axis position of the node center
     * @param offSet   : The horizontal space between parent-child to avoid overlap
     */
    private void drawTreeNode(Graphics graphics, RBTreeNode node, int x, int y, int offSet) {
        FontMetrics fm = graphics.getFontMetrics();
        String nodeKey = String.valueOf(node.key);

        int textWidth = fm.stringWidth(nodeKey);
        int nodeWidth = Math.max(30, textWidth + 10); //size of the node is dynamically adjusted according to the text size (Node key)
        int nodeHeight = 30;  //Constant node height

        if (node.left != null)
            drawTreeEdge(graphics, x, y, x - offSet, y + NODE_GAP);  //draw connector to left child

        if (node.right != null)
            drawTreeEdge(graphics, x, y, x + offSet, y + NODE_GAP); //draw connector to right child

        graphics.setColor(node.isRed ? Color.RED : Color.BLACK);  //Node color is set based on the R/B condition
        graphics.fillOval(x - nodeWidth / 2, y - nodeHeight / 2, nodeWidth, nodeHeight);  //draw the node
        graphics.setColor(Color.WHITE);  //Text color is white
        graphics.drawString(nodeKey, x - textWidth / 2, y + fm.getAscent() / 3);  //write the text and center it in the node

        if (node.left != null)
            drawTreeNode(graphics, node.left, x - offSet, y + NODE_GAP, offSet / 2);  //Draw left child

        if (node.right != null)
            drawTreeNode(graphics, node.right, x + offSet, y + NODE_GAP, offSet / 2); //draw right child

    }

    /**
     * Method draw the connector lines between two nodes (if any). i.e. Edge
     *
     * @param graphics - drawing canvas
     * @param x        - x coordinate of the parent node
     * @param y        - y of the parent
     * @param xChild   - x coordinate of the child node
     * @param yChild   - y coordinate of the child node
     */
    private void drawTreeEdge(Graphics graphics, int x, int y, int xChild, int yChild) {
        graphics.setColor(Color.GRAY);  //Line is in gray
        graphics.drawLine(x, y, xChild, yChild);  //draw the connector line
    }


    /**
     * Method to save the canvas as a png file
     *
     * @param filename - Image file name to save
     * @param width    -  width of the canvas
     * @param height   - height of the canvas
     */
    private void saveSnapshot(String filename, int width, int height) {
        this.setSize(width, height);  //Define the height and width of the canvas
        this.doLayout(); // force layout

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);  //Create a buffered image to save the canvas physically
        Graphics2D graphics = image.createGraphics(); //create canvas
        //Rendering tool, to make the lines smoother
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.paint(graphics);  // paint image in the created canvas
        graphics.dispose(); //discard the canvas for future use

        //Save the png file physically
        try {
            File file = new File(MemoryDatabase.BASE_PATH, filename);
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param root     accepts the current root node of the tree
     * @param filename filename to save the tree image
     */
    public static void showTree(RBTreeNode root, String filename) {
        RBTreeVisualization panel = new RBTreeVisualization(root);   //init this class
        //Draw and save the tree
        panel.saveSnapshot("RB_Tree" + filename + ".png",
                panel.width + 2500,  //width image
                panel.height); // height of the image
    }

}