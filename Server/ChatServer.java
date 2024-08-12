package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private String username;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                username = in.readLine();
                String clientJoin = "System: " + username + " has joined the chat";
                String clientLeft = "System: " + username + " has left the chat";
                broadcastMessage(clientJoin);
                System.out.println(clientJoin);

                synchronized (clientWriters) {
                    clientWriters.put(username, out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/msg ")) {
                        handleDirectMessage(message);
                    } else if (message.startsWith("PUBLIC: ")) {
                        broadcastMessage(username + ": " + message.substring(8));
                        System.out.println(username + ": " + message);
                    } else if (message.equalsIgnoreCase("/exit")) {
                        broadcastMessage(clientLeft);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    synchronized (clientWriters) {
                        System.out.println("System: " + username + " has left the chat");
                        clientWriters.remove(username);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleDirectMessage(String message) {
            // Format: /msg <username> <message>
            String[] parts = message.split(" ", 3);
            if (parts.length == 3) {
                String recipient = parts[1];
                String directMessage = "DM from " + username + " ----> " + recipient + ": " + parts[2];
                String directPersonal = "DM from " + username + ": " + parts[2];
                System.out.println(directMessage);

                synchronized (clientWriters) {
                    PrintWriter recipientWriter = clientWriters.get(recipient);
                    if (recipientWriter != null) {
                        recipientWriter.println(directPersonal);
                    } else {
                        out.println("User " + recipient + " not found.");
                    }
                }
            } else {
                out.println("Invalid direct message format. Use: /msg <username> <message>");
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters.values()) {
                    writer.println(message);
                }
            }
        }
    }
}
