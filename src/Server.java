import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

class ThreadServer extends Thread {
    ThreadServer(Socket socket, String username, HashMap<Socket, String> users) {

    }
    @Override
    public void run() {

    }

}
public class Server {
    private static boolean userPassInDatabase(String username, String password) {
        // Query from DB
        return false;
    }
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JButton startServerButton;
    public JPanel panel1;

    public Server() {
    startServerButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (startServerButton.getText().equals("Start Server")) {
                String address = textArea1.getText();
                String port = textArea2.getText();
                (new Thread(() -> {
                    try {
                        HashMap<Socket, String> users = new HashMap<Socket, String>();
                        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port), 255, InetAddress.getByName(address));
                        while (true) {
                            Socket socket = serverSocket.accept();
                            Scanner scanner = new Scanner(socket.getInputStream());
                            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
                            String[] loginMessage = scanner.nextLine().split(":");
                            if (userPassInDatabase(loginMessage[0], loginMessage[1])) {
                                users.put(socket, loginMessage[0]);
                                (new ThreadServer(socket, loginMessage[0], users)).start();
                                printWriter.println("GRANTED");
                                printWriter.flush();
                            }
                            else {
                                printWriter.println("DENIED");
                                printWriter.flush();
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
