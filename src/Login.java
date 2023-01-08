import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Login {
    private JButton button1;
    public JPanel panel1;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextArea textArea3;
    private JPasswordField passwordField1;

    public Login() {
    button1.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String address = textArea1.getText();
            String port = textArea2.getText();
            String username = textArea3.getText();
            String password = "";
            try {
                password = new BigInteger(1, MessageDigest.getInstance("md5").digest(passwordField1.getText().getBytes())).toString(16);
            }
            catch (NoSuchAlgorithmException exception) {
                // Actually nothing to handle here.
            }
            try {
                Socket socket = new Socket(address, Integer.parseInt(port));
                Scanner scanner = new Scanner(socket.getInputStream());
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println(username + ":" + password);
                printWriter.flush();
                if (scanner.nextLine().equals("GRANTED")) {
                    Client.username = username;
                    Client.socket = socket;
                    Main.jFrame.setVisible(false);
                    Main.jFrame = new JFrame("Client");
                    Main.jFrame.setPreferredSize(new Dimension(800, 600));
                    Main.jFrame.setSize(new Dimension(800, 600));
                    Main.jFrame.setContentPane((new Client()).panel1);
                    Main.jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    Main.jFrame.setLocationRelativeTo(null);
                    Main.jFrame.pack();
                    Main.jFrame.setVisible(true);
                }
                else {
                    JOptionPane.showMessageDialog(null,
                            "Login failed. Check your username and password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Error while connecting to server. Check your address and port.",
                        "Connection Refused",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    });
}
}
