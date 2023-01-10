import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class ThreadClient extends Thread {
    String username;
    Socket socket;
    Client client;
    ThreadClient(String username, Socket socket, Client client) {
        this.username = username;
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                switch (command) {
                    case "MESSAGE" -> {
                        String user = scanner.nextLine();
                        String message = scanner.nextLine();
                        JLabel userLabel = new JLabel("User: " + user);
                        JLabel messageLabel = new JLabel(message);
                        userLabel.setForeground(Color.RED);
                        // Add to list
                        JOptionPane.showMessageDialog(null,
                                "User: " + user + "\nMessage:" + message,
                                "A message",
                                JOptionPane.INFORMATION_MESSAGE); // For debug only
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
public class Client {
    private String username;
    private Socket socket;
    public JPanel panel1;
    private JButton logoutButton;
    private JButton sendButton;
    public JList list1;
    private JLabel label;
    public JScrollPane scrollPane1;
    private JTextField textField1;
    private JList list2;

    public Client(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
        this.label.setText("Hello, " + username);
        (new ThreadClient(username, socket, this)).start();
    logoutButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println("LOGOUT");
                printWriter.flush();
                socket.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
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
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    printWriter.println("MESSAGE");
                    printWriter.println(textField1.getText());
                    textField1.setText("");
                    printWriter.flush();
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        textField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                        printWriter.println("MESSAGE");
                        printWriter.println(textField1.getText());
                        textField1.setText("");
                        printWriter.flush();
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
    }
}
