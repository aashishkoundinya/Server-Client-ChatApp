package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<String, ChatRoom> chatRooms = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");

        chatRooms.put("room123", new ChatRoom("room123"));
        chatRooms.put("room456", new ChatRoom("room456"));
        chatRooms.put("room789", new ChatRoom("room789"));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private String username;
        private String chatRoomCode;
        private ChatRoom currentChatRoom;
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
                chatRoomCode = in.readLine();

                synchronized (chatRooms) {
                    currentChatRoom = chatRooms.get(chatRoomCode);
                }

                if (currentChatRoom == null) {
                    out.println("INVALID_ROOM");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace(System.out);
                    }
                    return;
                } else {
                    out.println("JOINED_ROOM");
                    currentChatRoom.addClient(this);
                    broadcastMessage("System: " + username + " has joined the chat");
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/msg ")) {
                        handleDirectMessage(message);
                    } else if (message.startsWith("PUBLIC: ")) {
                        broadcastMessage(username + ": " + message.substring(8));
                    } else if (message.equalsIgnoreCase("/exit")) {
                        broadcastMessage("System: " + username + " has left the chat");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            } finally {
                if (username != null && currentChatRoom != null) {
                    currentChatRoom.removeClient(this);
                    System.out.println("System: " + username + " has left the chat");
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
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

                synchronized (currentChatRoom) {
                    ClientHandler recipientHandler = currentChatRoom.getClient(recipient);
                    if (recipientHandler != null) {
                        recipientHandler.out.println(directPersonal);
                    } else {
                        out.println("User " + recipient + " not found in this chat room.");
                    }
                }
            } else {
                out.println("Invalid direct message format. Use: /msg <username> <message>");
            }
        }

        private void broadcastMessage(String message) {
            currentChatRoom.broadcastMessage(message, this);
        }
    }

    private static class ChatRoom {
        private String code;
        private final Map<String, ClientHandler> clients = new HashMap<>();

        public ChatRoom(String code) {
            this.code = code;
        }

        public synchronized void addClient(ClientHandler client) {
            clients.put(client.username, client);
        }

        public synchronized void removeClient(ClientHandler client) {
            clients.remove(client.username);
        }

        public synchronized void broadcastMessage(String message, ClientHandler sender) {
            for (ClientHandler client : clients.values()) {
                if (client != sender) {
                    client.out.println(message);
                }
            }
        }

        public synchronized ClientHandler getClient(String username) {
            return clients.get(username);
        }
    }
}
