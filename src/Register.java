import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Scanner;

public class Register {
    private JButton button1;
    private JTextField addressTextField;
    private JTextField portTextField;
    private JTextField usernameTextField;
    private JPasswordField passwordField1;
    public JPanel panel1;
    private JTextField ageTextField;
    private JTextField addressTextField1;
    private JPasswordField passwordField2;
    private JComboBox<String> genderComboBox;
    private JButton returnToLoginButton;

    public Register() {
    button1.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get all the information in the field
            String address = addressTextField.getText();
            String port = portTextField.getText();
            String username = usernameTextField.getText();
            String age = ageTextField.getText();
            String gender = genderComboBox.getSelectedItem().toString();
            String address1 = addressTextField1.getText();
            String password1 = "", password2 = "";
            if (passwordField1.getText().length() < 8) {
                JOptionPane.showMessageDialog(null,
                        "Password is too short. Use at least 8 characters.",
                        "Short Password",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // Get encrypted password
                password1 = (new BigInteger(1, MessageDigest.getInstance("md5").digest(passwordField1.getText().getBytes()))).toString(16);
                password2 = (new BigInteger(1, MessageDigest.getInstance("md5").digest(passwordField2.getText().getBytes()))).toString(16);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            int ageNumber, portNumber;
            try {
                // Check if age is valid
                ageNumber = Integer.parseInt(age);
                if (ageNumber < 0) {
                    throw new Exception();
                }
                // Check if port is valid
                portNumber = Integer.parseInt(port);
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Invalid Input. Check your information.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean invalid = false;
            final String[] invalidCharacters = new String[]{"'", "\"", "%", ":"};
            for (String s : invalidCharacters) {
                if (username.contains(s) || address1.contains(s)) {
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
            // Check if password is match
            if (!password1.equals(password2)) {
                JOptionPane.showMessageDialog(null,
                        "The password you have inputted the first time is not the same as the second's.",
                        "Password Mismatch",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                // Send register information to the server
                Socket socket = new Socket(address, portNumber);
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                Scanner scanner = new Scanner(socket.getInputStream());
                printWriter.println("REGISTER");
                printWriter.println(username);
                printWriter.println(ageNumber);
                printWriter.println(gender);
                printWriter.println(address1);
                printWriter.println(password1);
                printWriter.flush();
                // Server returned success
                if (scanner.nextLine().equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(null,
                            "Successfully registered. You can return to the login page and login.",
                            "Successfully Registered",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                // Server returned failed
                else {
                    JOptionPane.showMessageDialog(null,
                            "Failed to register. Username is already occupied.",
                            "Failed to register",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            // Connection loss
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Error while connecting to server. Check your address and port.",
                        "Connection Refused",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    });
    // Return to the login form
        returnToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
    }
}
