import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

class ThreadServer extends Thread {
    Socket socket;
    String username;
    HashMap<Socket, String> users;
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
            users.forEach((user, username) -> {
                printWriter.println("ADDUSER");
                printWriter.println(username);
                printWriter.flush();
            });
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                switch (command) {
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
                    case "MESSAGE", "IMAGE" -> {
                        String message = scanner.nextLine();
                        users.forEach((user, username) -> {
                            try {
                                PrintWriter printEach = new PrintWriter(user.getOutputStream());
                                printEach.println(command);
                                printEach.println(this.username);
                                printEach.println(message);
                                printEach.flush();
                            }
                            catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        });
                    }
                }
            }
        }
        catch (Exception e) {
            try {
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
    private static boolean register(String username, int age, String gender, String address, String password) {
        try {
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
    startServerButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (startServerButton.getText().equals("Start Server")) {
                String address = textField1.getText();
                String port = textField2.getText();
                (new Thread(() -> {
                    try {
                        HashMap<Socket, String> users = new HashMap<Socket, String>();
                        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port), 255, InetAddress.getByName(address));
                        while (true) {
                            Socket socket = serverSocket.accept();
                            Scanner scanner = new Scanner(socket.getInputStream());
                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                            String message = scanner.nextLine();
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
                                    if (users.containsValue(loginMessage[0])) {
                                        printWriter.println("LOGGEDIN");
                                        printWriter.flush();
                                        continue;
                                    }
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
                                    (new ThreadServer(socket, loginMessage[0], users)).start();
                                    printWriter.println("GRANTED");
                                    printWriter.flush();
                                } else {
                                    printWriter.println("DENIED");
                                    printWriter.flush();
                                }
                            }
                        }
                    }
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
