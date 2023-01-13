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

    public Register() {
    button1.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String address = addressTextField.getText();
            String port = portTextField.getText();
            String username = usernameTextField.getText();
            String age = ageTextField.getText();
            String gender = genderComboBox.getSelectedItem().toString();
            String address1 = addressTextField1.getText();
            String password1 = "", password2 = "";
            try {
                password1 = (new BigInteger(1, MessageDigest.getInstance("md5").digest(passwordField1.getText().getBytes()))).toString(16);
                password2 = (new BigInteger(1, MessageDigest.getInstance("md5").digest(passwordField2.getText().getBytes()))).toString(16);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            int ageNumber, portNumber;
            try {
                ageNumber = Integer.parseInt(age);
                if (ageNumber < 0) {
                    throw new Exception();
                }
                portNumber = Integer.parseInt(port);
            }
            catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "Invalid Input. Check your information.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password1.equals(password2)) {
                JOptionPane.showMessageDialog(null,
                        "The password you have inputted the first time is not the same as the second's.",
                        "Password Mismatch",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
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
                if (scanner.nextLine().equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(null,
                            "Successfully registered. You can return to the login page and login.",
                            "Successfully Registered",
                            JOptionPane.INFORMATION_MESSAGE);
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
                else {
                    JOptionPane.showMessageDialog(null,
                            "Failed to register. Username is already occupied.",
                            "Failed to register",
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
