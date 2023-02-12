import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
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
    private void message(String userText, String message) {
        JLabel userLabel = new JLabel(userText);
        JLabel messageLabel = new JLabel(message);
        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        userLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
        // Add to panel
        client.chatPanel.add(userLabel);
        client.chatPanel.add(messageLabel);
        client.chatPanel.revalidate();
    }
    private void image(String userText, String imageBase64) {
        byte[] bytes = Base64.getDecoder().decode(imageBase64);
        ImageIcon image = new ImageIcon(bytes);
        int width = image.getIconWidth();
        int height = image.getIconHeight();
        if (200.0 / (double)width * (double)height > 150) {
            height = (int)(200.0 / (double)width * (double)height);
            width = 200;
        }
        else {
            width = (int)(150.0 / (double)height * (double)height);
            height = 150;
        }
        image.setImage(image.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        JLabel imageLabel = new JLabel(image);
        imageLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFrame jFrame = new JFrame("Image Viewer");
                ImageViewer imageViewer = new ImageViewer();
                JPanel jPanel = imageViewer.jPanel;
                JScrollPane jScrollPane = imageViewer.jScrollPane;
                jScrollPane.getViewport().add(new JLabel(new ImageIcon(bytes)));
                jFrame.setPreferredSize(new Dimension(800, 600));
                jFrame.setSize(new Dimension(800, 600));
                jFrame.setContentPane(jPanel);
                jFrame.setLocationRelativeTo(null);
                jFrame.setAlwaysOnTop(true);
                jFrame.pack();
                jFrame.setVisible(true);
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
        JLabel userLabel = new JLabel(userText);
        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        userLabel.setForeground(Color.RED);
        client.chatPanel.add(userLabel);
        client.chatPanel.add(imageLabel);
        client.chatPanel.revalidate();
    }
    private void audio(String userText, String audioBase64) {
        byte[] bytes = Base64.getDecoder().decode(audioBase64);
        JLabel userLabel = new JLabel(userText);
        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        userLabel.setForeground(Color.RED);
        JButton soundButton = new JButton("Play Sound");
        soundButton.setFont(new Font("Dialog", Font.PLAIN, 20));
        try {
            Clip audioClip = AudioSystem.getClip();
            soundButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (soundButton.getText().equals("Play Sound")) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
                            if (!audioClip.isOpen()) {
                                audioClip.open(audioInputStream);
                            }
                            new Thread(() -> audioClip.start()).start();
                            soundButton.setText("Stop Sound");
                        } else {
                            if (audioClip.isRunning()) {
                                audioClip.stop();
                            }
                            audioClip.flush();
                            soundButton.setText("Play Sound");
                        }
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        client.chatPanel.add(userLabel);
        client.chatPanel.add(soundButton);
        client.chatPanel.revalidate();
    }
    @Override
    public void run() {
        try {
            File file = new File(username + "_log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                Scanner fileScanner = new Scanner(file);
                // Present the content of the file
                while (fileScanner.hasNextLine()) {
                    String command = fileScanner.nextLine();
                    switch (command) {
                        case "MESSAGE" -> {
                            String userText = fileScanner.nextLine();
                            String message = fileScanner.nextLine();
                            message(userText, message);
                        }
                        case "IMAGE" -> {
                            String userText = fileScanner.nextLine();
                            String imageBase64 = fileScanner.nextLine();
                            image(userText, imageBase64);
                        }
                        case "AUDIO" -> {
                            String userText = fileScanner.nextLine();
                            String audioBase64 = fileScanner.nextLine();
                            audio(userText, audioBase64);
                        }
                    }
                }
                fileScanner.close();
            }
            Scanner scanner = new Scanner(socket.getInputStream());
            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter filePrinter = new PrintWriter(fileWriter);
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                switch (command) {
                    case "MESSAGE" -> {
                        String user = scanner.nextLine();
                        long time = Long.parseLong(scanner.nextLine());
                        String message = scanner.nextLine();
                        String userText = "User: " + user + " Time: " + (new Date(time)).toString();
                        message(userText, message);

                        // Add to file
                        filePrinter.println("MESSAGE");
                        filePrinter.println(userText);
                        filePrinter.println(message);
                        filePrinter.flush();
                    }
                    case "IMAGE" -> {
                        String user = scanner.nextLine();
                        long time = Long.parseLong(scanner.nextLine());
                        String imageBase64 = scanner.nextLine();
                        String userText = "User: " + user + " Time: " + (new Date(time)).toString();
                        image(userText, imageBase64);

                        // Add to file
                        filePrinter.println("IMAGE");
                        filePrinter.println(userText);
                        filePrinter.println(imageBase64);
                        filePrinter.flush();
                    }
                    case "AUDIO" -> {
                        String user = scanner.nextLine();
                        long time = Long.parseLong(scanner.nextLine());
                        String audioBase64 = scanner.nextLine();
                        String userText = "User: " + user + " Time: " + (new Date(time)).toString();
                        audio(userText, audioBase64);

                        // Add to file
                        filePrinter.println("AUDIO");
                        filePrinter.println(userText);
                        filePrinter.println(audioBase64);
                        filePrinter.flush();
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
            try {
                PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.println("LOGOUT");
                printWriter.flush();
                socket.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
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
    private JButton audioButton;
    private JButton emojiButton;
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
        Main.jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                    printWriter.println("LOGOUT");
                    printWriter.flush();
                    socket.close();
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
                System.exit(0);
            }
        });
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
                if (textField1.getText().equals("")) {
                    return;
                }
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
                    if (textField1.getText().equals("")) {
                        return;
                    }
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
                jFileChooser.removeChoosableFileFilter(jFileChooser.getAcceptAllFileFilter());
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
                if (file != null) {
                    try {
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        String imageBase64 = Base64.getEncoder().encodeToString(bytes);
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                        printWriter.println("IMAGE");
                        printWriter.println(imageBase64);
                        printWriter.flush();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
        audioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.removeChoosableFileFilter(jFileChooser.getAcceptAllFileFilter());
                jFileChooser.addChoosableFileFilter(new FileFilter() {
                    String[] extensions = {"wav", "aifc", "aiff", "au", "snd"};
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
                        return "Audio File (*.wav, *.aifc, *.aiff, *.au, *.snd)";
                    }
                });
                jFileChooser.showDialog(new JLabel(), "Choose");
                File file = jFileChooser.getSelectedFile();
                if (file != null) {
                    try {
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        String audioBase64 = Base64.getEncoder().encodeToString(bytes);
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                        printWriter.println("AUDIO");
                        printWriter.println(audioBase64);
                        printWriter.flush();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
        emojiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame jFrame = new JFrame("Emoji Selector");
                DefaultListModel<String> emojiList = new DefaultListModel<String>();
                for (char i = '\uDE00'; i < '\uDE70'; i++) {
                    String emoji = "\uD83D" + i;
                    emojiList.addElement(emoji);
                }
                EmojiSelector emojiSelector = new EmojiSelector();
                emojiSelector.list.setModel(emojiList);
                jFrame.setContentPane(emojiSelector.jPanel);
                jFrame.setSize(new Dimension(300, 600));
                jFrame.setPreferredSize(new Dimension(300, 600));
                jFrame.setLocationRelativeTo(null);
                jFrame.setAlwaysOnTop(true);
                jFrame.pack();
                jFrame.setVisible(true);
                (new Thread(() -> {
                    while (emojiSelector.emoji == -1 && jFrame.isVisible()) {
                        // Do nothing
                        try {
                            Thread.sleep(1);
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                    if (emojiSelector.emoji != -1) {
                        textField1.setText(textField1.getText() + emojiList.getElementAt(EmojiSelector.emoji));
                    }
                    emojiSelector.emoji = -1;
                    jFrame.setVisible(false);
                })).start();
            }
        });
    }
}
