package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

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
        private PrintWriter out;
        private BufferedReader in;
        private String clientId;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                
                username = in.readLine();
                System.out.println(username + " has joined the chat");

                synchronized (clientHandlers) {
                    clientHandlers.add(this);
                }

                broadcastMessage(username + " has joined the chat", this);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(username + ": " + message);
                    broadcastMessage(username + ": " + message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                }
            }
        }

        private void broadcastMessage(String message, ClientHandler sender) {
            synchronized (clientHandlers) {
                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler != sender) {
                        clientHandler.out.println(message);
                    }
                }
            }
        }
    }
}
