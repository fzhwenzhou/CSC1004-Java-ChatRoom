import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Entry {
    private JButton serverModeButton;
    public JPanel panel1;
    private JButton clientModeButton;
public Entry() {
    clientModeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // This button directs you to the login page.
            Main.jFrame.setVisible(false);
            Main.jFrame = new JFrame("Login");
            Main.jFrame.setPreferredSize(new Dimension(600, 450));
            Main.jFrame.setSize(new Dimension(600, 450));
            Main.jFrame.setContentPane((new Login()).panel1);
            Main.jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Main.jFrame.setLocationRelativeTo(null);
            Main.jFrame.pack();
            Main.jFrame.setVisible(true);
        }
    });
    serverModeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // This button directs you to the server page.
            Main.jFrame.setVisible(false);
            Main.jFrame = new JFrame("Server");
            Main.jFrame.setPreferredSize(new Dimension(600, 450));
            Main.jFrame.setSize(new Dimension(600, 450));
            Main.jFrame.setContentPane((new Server()).panel1);
            Main.jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Main.jFrame.setLocationRelativeTo(null);
            Main.jFrame.pack();
            Main.jFrame.setVisible(true);
        }
    });
}
}
