import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
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
                        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
                        userLabel.setForeground(Color.RED);
                        messageLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
                        // Add to panel
                        client.chatPanel.add(userLabel);
                        client.chatPanel.add(messageLabel);
                        client.chatPanel.revalidate();
                    }
                    case "IMAGE" -> {
                        String user = scanner.nextLine();
                        String imageBase64 = scanner.nextLine();
                        byte[] bytes = Base64.getDecoder().decode(imageBase64);
                        JLabel imageLabel = new JLabel(new ImageIcon(bytes));
                        JLabel userLabel = new JLabel("User: " + user);
                        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
                        userLabel.setForeground(Color.RED);
                        client.chatPanel.add(userLabel);
                        client.chatPanel.add(imageLabel);
                        client.chatPanel.revalidate();
                    }
                    case "ADDUSER" -> {
                        String user = scanner.nextLine();
                        client.defaultListModel.addElement(user);
                        client.list1.setModel(client.defaultListModel);
                    }
                    case "DELUSER" -> {
                        String user = scanner.nextLine();
                        client.defaultListModel.removeElement(user);
                        client.list1.setModel(client.defaultListModel);
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
    public DefaultListModel<String> defaultListModel = new DefaultListModel<String>();
    private String username;
    private Socket socket;
    public JPanel panel1;
    private JButton logoutButton;
    private JButton sendButton;
    private JLabel label;
    public JScrollPane scrollPane2;
    private JTextField textField1;
    private JScrollPane scrollPane1;
    public JList list1;
    private JButton imageButton;
    public JPanel chatPanel = new JPanel();

    public Client(String username, Socket socket) {
        this.username = username;
        this.socket = socket;
        this.label.setText("Hello, " + username);
        this.chatPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.chatPanel.setLayout(new BoxLayout(this.chatPanel, BoxLayout.Y_AXIS));
        this.list1.setModel(defaultListModel);
        this.scrollPane1.getViewport().add(this.list1);
        this.scrollPane2.getViewport().add(this.chatPanel);
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
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.addChoosableFileFilter(new FileFilter() {
                    String[] extensions = {"jpg", "png", "jpeg", "gif"};
                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        String filename = f.getName();
                        int dot = filename.lastIndexOf(".");
                        if (dot > 0 && dot < filename.length() - 1) {
                            String extension = filename.substring(dot + 1).toLowerCase();
                            if (Arrays.asList(extensions).contains(extension)) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "Image File (*.jpg, *.png, *.jpeg, *.gif)";
                    }
                });
                jFileChooser.showDialog(new JLabel(), "Choose");
                File file = jFileChooser.getSelectedFile();
                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    String imageBase64 = Base64.getEncoder().encodeToString(bytes);
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    printWriter.println("IMAGE");
                    printWriter.println(imageBase64);
                    printWriter.flush();
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }
}
