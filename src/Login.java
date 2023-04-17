import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Login {
    private JButton button1;
    public JPanel panel1;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JPasswordField passwordField1;
    private JButton registerButton;
    private void login() {
        // Get the information from the fields
        String address = textField1.getText();
        String port = textField2.getText();
        String username = textField3.getText();
        String password = "";
        try {
            // Password encryption with MD5
            password = (new BigInteger(1, MessageDigest.getInstance("md5").digest(passwordField1.getText().getBytes()))).toString(16);
        }
        catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
        try {
            // Create socket, scanner and print writer.
            Socket socket = new Socket(address, Integer.parseInt(port));
            Scanner scanner = new Scanner(socket.getInputStream());
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(username + ":" + password);
            printWriter.flush();
            // Get the message from server after sending username and password
            String message = scanner.nextLine();
            boolean invalid = false;
            final String[] invalidCharacters = new String[]{"'", "\"", "%", ":"};
            for (String s : invalidCharacters) {
                if (username.contains(s) || address.contains(s)) {
                    invalid = true;
                    break;
                }
            }
            if (invalid) {
                JOptionPane.showMessageDialog(null,
                        "Invalid Input. \"', \", %, :\" cannot appear in the fields.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Permission granted. Go to client form.
            if (message.equals("GRANTED")) {
                Client client = new Client(username, socket);
                Main.jFrame.setVisible(false);
                Main.jFrame.setTitle("Client");
                Main.jFrame.setPreferredSize(new Dimension(800, 600));
                Main.jFrame.setSize(new Dimension(800, 600));
                Main.jFrame.setContentPane(client.panel1);
                Main.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                Main.jFrame.setLocationRelativeTo(null);
                Main.jFrame.pack();
                Main.jFrame.setVisible(true);
            }
            // User already logged in.
            else if (message.equals("LOGGEDIN")) {
                JOptionPane.showMessageDialog(null,
                        "Login failed. User already logged in.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
            // Username or password mismatch
            else {
                JOptionPane.showMessageDialog(null,
                        "Login failed. Check your username and password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        // Error when connecting to server
        catch (Exception exception) {
            JOptionPane.showMessageDialog(null,
                    "Error while connecting to server. Check your address and port.",
                    "Connection Refused",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Login() {
    button1.addActionListener(new ActionListener() {
        // "Login" button was clicked
        @Override
        public void actionPerformed(ActionEvent e) {
            login();
        }
    });
        registerButton.addActionListener(new ActionListener() {
            // "Register" button was clicked
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.jFrame.setVisible(false);
                Main.jFrame = new JFrame("Register");
                Main.jFrame.setPreferredSize(new Dimension(800, 600));
                Main.jFrame.setSize(new Dimension(800, 600));
                Main.jFrame.setContentPane((new Register()).panel1);
                Main.jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                Main.jFrame.setLocationRelativeTo(null);
                Main.jFrame.pack();
                Main.jFrame.setVisible(true);
            }
        });
        // When you pressed "enter/return" in the password field, it will try to log in.
        passwordField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
    }
}
