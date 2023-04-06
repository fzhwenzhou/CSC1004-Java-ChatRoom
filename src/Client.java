import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.ForkJoinWorkerThread;

class AudioRecorderThread extends Thread {
    public static byte[] audio;
    static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    TargetDataLine targetDataLine;

    AudioRecorderThread() {
        AudioFormat audioFormat = new AudioFormat(8000f, 16, 1, true, false);
        // Record audio
        try {
            targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
            targetDataLine.open(audioFormat);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        targetDataLine.start();
        // Create audio line
        AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);
        File file = null;
        try {
            // Save the audio to a temp file
            file = new File(Math.random() + ".wav");
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // The record has been stopped
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte bytes[] = new byte[1024];
                // Continuously read the bytes from byte output stream to file
                while (fileInputStream.read(bytes) != -1) {
                    byteArrayOutputStream.write(bytes);
                }
                fileInputStream.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            // Delete the temp file
            file.delete();
            audio = byteArrayOutputStream.toByteArray();
        }
    }

    public void stopRecording() {
        // Stop target line
        targetDataLine.stop();
        targetDataLine.close();
        try {
            this.join();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
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
        // Receive one message
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
        // Receive one image
        byte[] bytes = Base64.getDecoder().decode(imageBase64);
        ImageIcon image = new ImageIcon(bytes);
        int width = image.getIconWidth();
        int height = image.getIconHeight();
        // Adjust the size of the thumbnail
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
        // When clicking on the thumbnail, it opens the image viewer
        imageLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFrame jFrame = new JFrame("Image Viewer");
                ImageViewer imageViewer = new ImageViewer();
                JPanel jPanel = imageViewer.jPanel;
                JScrollPane jScrollPane = imageViewer.jScrollPane;
                // Full sized image
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
        // Add the image thumbnail to the list
        JLabel userLabel = new JLabel(userText);
        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        userLabel.setForeground(Color.RED);
        client.chatPanel.add(userLabel);
        client.chatPanel.add(imageLabel);
        client.chatPanel.revalidate();
    }
    private void audio(String userText, String audioBase64) {
        // Receive one audio
        byte[] bytes = Base64.getDecoder().decode(audioBase64);
        JLabel userLabel = new JLabel(userText);
        userLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        userLabel.setForeground(Color.RED);
        // Add one button
        JButton soundButton = new JButton("Play Sound");
        soundButton.setFont(new Font("Dialog", Font.PLAIN, 20));
        try {
            // Play the sound when click on the button
            soundButton.addActionListener(new ActionListener() {
                Clip audioClip;
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        // If the sound has not been played
                        if (soundButton.getText().equals("Play Sound")) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(byteArrayInputStream);
                            audioClip = AudioSystem.getClip();
                            // Handle some unexpected situation
                            if (!audioClip.isOpen()) {
                                audioClip.open(audioInputStream);
                            }
                            new Thread(() -> audioClip.start()).start();
                            soundButton.setText("Stop Sound");
                            // If it is playing
                        }
                        else {
                            if (audioClip.isRunning()) {
                                audioClip.stop();
                            }
                            audioClip.flush();
                            audioClip.close();
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
        // Add button to the panel
        client.chatPanel.add(userLabel);
        client.chatPanel.add(soundButton);
        client.chatPanel.revalidate();
    }
    @Override
    public void run() {
        try {
            // Read the message history
            File file = new File(username + "_log.txt");
            // No file
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
                    // If it is a message
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
                        // If it is an image
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
                        // If it is an audio
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
                        // Add one user to the list
                        String user = scanner.nextLine();
                        client.defaultListModel.addElement(user);
                        client.list1.setModel(client.defaultListModel);
                    }
                    case "DELUSER" -> {
                        // Delete one user to the list
                        String user = scanner.nextLine();
                        client.defaultListModel.removeElement(user);
                        client.list1.setModel(client.defaultListModel);
                    }
                }
            }
        }
        catch (Exception e) {
            // Something unexpected happens. Log out.
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
    private JButton voiceChatButton;
    private JButton speakButton;
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
                    // Log out when close the window
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
        // Log out when clicking the "Logout" button
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
            // Return to the login frame
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
            // When the "Send" button was hit
            @Override
            public void actionPerformed(ActionEvent e) {
                // Nothing inside
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
        // If "enter/return" was pressed
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
        // Image button
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.removeChoosableFileFilter(jFileChooser.getAcceptAllFileFilter());
                // Filters for file chooser
                jFileChooser.addChoosableFileFilter(new FileFilter() {
                    // Available extensions
                    String[] extensions = {"jpg", "png", "jpeg", "gif", "bmp"};
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
                        return "Image File (*.jpg, *.png, *.jpeg, *.gif, *.bmp)";
                    }
                });
                jFileChooser.showOpenDialog(new JLabel());
                File file = jFileChooser.getSelectedFile();
                // Read the file
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
        // Audio button
        audioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.removeChoosableFileFilter(jFileChooser.getAcceptAllFileFilter());
                // Filters for file chooser
                jFileChooser.addChoosableFileFilter(new FileFilter() {
                    // Available file extensions
                    String[] extensions = {"wav", "aifc", "aiff", "au", "snd", "pcm"};
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
                        return "Audio File (*.wav, *.aifc, *.aiff, *.au, *.snd, *.pcm)";
                    }
                });
                jFileChooser.showOpenDialog(new JLabel());
                File file = jFileChooser.getSelectedFile();
                // Read the file
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
                // List to store emoji
                DefaultListModel<String> emojiList = new DefaultListModel<String>();
                // Store some emojis (Unicode) to the list
                for (char i = '\uDE00'; i < '\uDE50'; i++) {
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
                // Create a thread to listen if the emoji is selected
                (new Thread(() -> {
                    while (emojiSelector.emoji == -1 && jFrame.isVisible()) {
                        // Do nothing
                        try {
                            // Prevent stuck on Windows
                            Thread.sleep(1);
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                    // If the window is not closed directly
                    if (emojiSelector.emoji != -1) {
                        textField1.setText(textField1.getText() + emojiList.getElementAt(EmojiSelector.emoji));
                    }
                    emojiSelector.emoji = -1;
                    jFrame.setVisible(false);
                })).start();
            }
        });
        voiceChatButton.addActionListener(new ActionListener() {
            // Initialize
            static boolean isChatting = false;
            // Voice chat
            @Override
            public void actionPerformed(ActionEvent e) {
                if (voiceChatButton.getText().equals("Voice Chat")) {
                    voiceChatButton.setText("Stop Chat");
                    isChatting = true;
                    (new Thread(() -> {
                        try {
                            // Start Chatting
                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                            printWriter.println("CHAT");
                            printWriter.flush();
                            Thread.sleep(100);
                            Socket chatSocket = new Socket(socket.getInetAddress(), 65535);
                            PrintWriter audioPrinter = new PrintWriter(chatSocket.getOutputStream());
                            Scanner audioScanner = new Scanner(chatSocket.getInputStream());
                            // Get data lines
                            AudioFormat audioFormat = new AudioFormat(8000f, 16, 1, true, false);
                            TargetDataLine targetDataLine = AudioSystem.getTargetDataLine(audioFormat);
                            targetDataLine.open(audioFormat);
                            targetDataLine.start();
                            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
                            sourceDataLine.open(audioFormat);
                            sourceDataLine.start();
                            (new Thread(() -> {
                                try {
                                    while (isChatting) {
                                        // For testing only
                                        // System.err.println("Received one audio");
                                        // Continuously listen
                                        String audioBase64 = audioScanner.nextLine();
                                        byte[] bytes = Base64.getDecoder().decode(audioBase64);
                                        sourceDataLine.write(bytes, 0, bytes.length);
                                    }
                                }
                                catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                            })).start();
                            byte[] bytes = new byte[1024];
                            // For testing only
                            // System.err.println("Start Sending Audio");
                            // Continuously send
                            while (isChatting) {
                                targetDataLine.read(bytes, 0, bytes.length);
                                String audioBase64 = Base64.getEncoder().encodeToString(bytes);
                                audioPrinter.println(audioBase64);
                                audioPrinter.flush();
                            }
                            // Stop all the lines
                            sourceDataLine.stop();
                            targetDataLine.stop();
                            targetDataLine.close();
                            sourceDataLine.close();
                            audioPrinter.println("STOP CHATTING");
                            audioPrinter.flush();
                            audioScanner.close();
                            audioPrinter.close();
                            chatSocket.close();
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    })).start();
                }
                else {
                    // Stop chatting
                    voiceChatButton.setText("Voice Chat");
                    isChatting = false;
                }
            }
        });
        speakButton.addActionListener(new ActionListener() {
            static AudioRecorderThread audioThread;
            @Override
            public void actionPerformed(ActionEvent e) {
                // Start speaking
                if (speakButton.getText().equals("Speak")) {
                    audioThread = new AudioRecorderThread();
                    audioThread.start();
                    speakButton.setText("Stop");
                }
                // Stop speaking
                else {
                    audioThread.stopRecording();
                    byte[] output = audioThread.audio;
                    String audioBase64 = Base64.getEncoder().encodeToString(output);
                    // Write the audio to the server
                    try {
                        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                        printWriter.println("AUDIO");
                        printWriter.println(audioBase64);
                        printWriter.flush();
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    speakButton.setText("Speak");
                }
            }
        });
    }
}
