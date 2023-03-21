import javax.swing.*;
import java.awt.*;

public class Main {
    // Entry point of the program
    public static JFrame jFrame = new JFrame("Entry");
    public static void main(String[] args) {
        jFrame.setPreferredSize(new Dimension(600, 450));
        jFrame.setSize(new Dimension(600, 450));
        jFrame.setContentPane((new Entry()).panel1);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null); // Center the window
        jFrame.pack();
        jFrame.setVisible(true); // Open the first form
    }
}