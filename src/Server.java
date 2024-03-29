import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
class ChatServer extends Thread {
    Socket socket;
    ChatServer(Socket socket) {
        this.socket = socket;
        // Add this socket to the user socket list
        Server.chatClients.add(socket);
    }
    @Override
    public void run() {
        try {
            PrintWriter audioPrinter = new PrintWriter(socket.getOutputStream());
            Scanner audioScanner = new Scanner(socket.getInputStream());
            while (audioScanner.hasNextLine()) {
                String audioBase64 = audioScanner.nextLine();
                // If the user clicks "Stop Chat" in the client
                if (audioBase64.equals("STOP CHATTING")) {
                    break;
                }
                // Broadcast the audio message to all the users
                else {
                    for (Socket userSocket : Server.chatClients) {
                        // Not broadcasting to itself
                        if (!userSocket.equals(socket)) {
                        // For testing only
                        // if (true) {
                            (new Thread(() -> {
                                try {
                                    // Print Base64
                                    PrintWriter printEach = new PrintWriter(userSocket.getOutputStream());
                                    printEach.println(audioBase64);
                                    printEach.flush();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })).start();
                        }
                    }
                }
            }
            socket.close();
            Server.chatClients.remove(socket);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class ThreadServer extends Thread {
    Socket socket;
    String username;
    HashMap<Socket, String> users;
    // Initialize thread server
    ThreadServer(Socket socket, String username, HashMap<Socket, String> users) {
        this.socket = socket;
        this.username = username;
        this.users = users;
    }
    @Override
    public void run() {
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            Scanner scanner = new Scanner(socket.getInputStream());
            Thread.sleep(100);
            // Add user to everyone's user list
            users.forEach((user, username) -> {
                printWriter.println("ADDUSER");
                printWriter.println(username);
                printWriter.flush();
            });
            while (scanner.hasNextLine()) {
                // Get command
                String command = scanner.nextLine();
                switch (command) {
                    // User log out or close the window
                    case "LOGOUT" -> {
                        users.remove(socket);
                        socket.close();
                        users.forEach((user, username) -> {
                            try {
                                PrintWriter printEach = new PrintWriter(user.getOutputStream());
                                printEach.println("DELUSER");
                                printEach.println(this.username);
                                printEach.flush();
                            }
                            catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });
                        return;
                    }
                    // Receive message, image or audio
                    // Broadcast it to others
                    case "MESSAGE", "IMAGE", "AUDIO" -> {
                        String message = scanner.nextLine();
                        users.forEach((user, username) -> {
                            try {
                                PrintWriter printEach = new PrintWriter(user.getOutputStream());
                                printEach.println(command);
                                printEach.println(this.username);
                                printEach.println((new Date()).getTime());
                                printEach.println(message);
                                printEach.flush();
                                // Add to database
                            }
                            catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });
                    }
                    // Create a chat server to enable them to chat
                    case "CHAT" -> {
                        (new ChatServer(Server.serverSocket.accept())).start();
                    }
                }
            }
        }
        catch (Exception e) {
            try {
                e.printStackTrace();
                // If disconnected unexpectedly
                users.remove(socket);
                socket.close();
                users.forEach((user, username) -> {
                    try {
                        PrintWriter printEach = new PrintWriter(user.getOutputStream());
                        printEach.println("DELUSER");
                        printEach.println(this.username);
                        printEach.flush();
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
                return;
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
            e.printStackTrace();
        }
    }

}
public class Server {
    public static ServerSocket serverSocket;
    public static ArrayList<Socket> chatClients = new ArrayList<Socket>();
    // Register
    private static boolean register(String username, int age, String gender, String address, String password) {
        try {
            // Send register query to database
            SQLiteDatabase database = new SQLiteDatabase((new File("")).getCanonicalPath() + "/sqlite.db");
            if (!database.tableExists()) {
                database.createTable();
            }
            return database.registerQuery(username, age, gender, address, password);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private static boolean userPassInDatabase(String username, String password) {
        try {
            SQLiteDatabase database = new SQLiteDatabase((new File("")).getCanonicalPath() + "/sqlite.db");
            // Query from DB
            if (!database.tableExists()) {
                return false;
            }
            return database.loginQuery(username, password);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private JTextField textField1;
    private JTextField textField2;
    private JButton startServerButton;
    public JPanel panel1;

    public Server() {
        // Start a new server
        try {
            serverSocket = new ServerSocket(65535); // For voice chat
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    startServerButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (startServerButton.getText().equals("Start Server")) {
                String address = textField1.getText();
                String port = textField2.getText();
                // Continuously listen for clients to join in
                (new Thread(() -> {
                    try {
                        HashMap<Socket, String> users = new HashMap<Socket, String>();
                        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port), 255, InetAddress.getByName(address));
                        while (true) {
                            Socket socket = serverSocket.accept();
                            Scanner scanner = new Scanner(socket.getInputStream());
                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                            String message = scanner.nextLine();
                            // Perform register
                            if (message.equals("REGISTER")) {
                                if (register(scanner.nextLine(), Integer.parseInt(scanner.nextLine()), scanner.nextLine(), scanner.nextLine(), scanner.nextLine())) {
                                    printWriter.println("SUCCESS");
                                    printWriter.flush();
                                }
                                else {
                                    printWriter.println("FAILED");
                                    printWriter.flush();
                                }
                                // Go to register
                            }
                            else {
                                String[] loginMessage = message.split(":");
                                if (userPassInDatabase(loginMessage[0], loginMessage[1])) {
                                    // Already logged in
                                    if (users.containsValue(loginMessage[0])) {
                                        printWriter.println("LOGGEDIN");
                                        printWriter.flush();
                                        continue;
                                    }
                                    // Add to user list
                                    users.forEach((user, username) -> {
                                        try {
                                            PrintWriter printEach = new PrintWriter(user.getOutputStream());
                                            printEach.println("ADDUSER");
                                            printEach.println(loginMessage[0]);
                                            printEach.flush();
                                        }
                                        catch (Exception exception) {
                                            exception.printStackTrace();
                                        }
                                    });
                                    users.put(socket, loginMessage[0]);
                                    ThreadServer threadServer = new ThreadServer(socket, loginMessage[0], users);
                                    threadServer.start();
                                    printWriter.println("GRANTED");
                                    printWriter.flush();
                                } else {
                                    printWriter.println("DENIED");
                                    printWriter.flush();
                                }
                            }
                        }
                    }
                    // Handle some exceptions
                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(null,
                                "Failed to start the server. Check if address or port is valid.",
                                "Failed to start server",
                                JOptionPane.ERROR_MESSAGE);
                    }
                })).start();
                startServerButton.setText("Server Running...");
            }
        }
    });
}
}
