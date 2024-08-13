package Client;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            new Thread(new ServerHandler(socket)).start();

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println(username);

            System.out.print("Enter chatroom code: ");
            String roomcode = scanner.nextLine();
            out.println(roomcode);

            String chatroomResponse = in.readLine();

            if (chatroomResponse.equals("INVALID ROOM")) {
                System.out.println("Invalid ChatRoom Code");
                socket.close();
                scanner.close();
                return;
            }

            System.out.println("");
            System.out.println("******* Joined chat room " + roomcode + " *******");
            System.out.println("");
            System.out.println("-----------------" + username + "'s Terminal-----------------");

            new Thread(new ServerHandler(socket)).start();

            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();

                if (message.startsWith("/msg ")) {
                    // Format for direct messages: /msg <username> <message>
                    out.println(message);
                } else if (message.equalsIgnoreCase("/exit")) {
                    out.println("/exit");
                    System.out.println("Exiting the chat");
                    break;                    
                } else if (message.equalsIgnoreCase("/commands")) {
                    showcommands();
                } else {
                    out.println("PUBLIC: " + message);
                }
            }

            scanner.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private static void showcommands() {
        System.out.println("Available Commands: ");
        System.out.println("1. /msg <username> <msg> ----> Sends a direct message to the mentioned user");
        System.out.println("2. /commands ----> Show all avaiable commands");
        System.out.println("3.  /exit ----> Ends the session");
    }

    private static class ServerHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }
}

/*package Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ChatClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private TextArea messageArea;
    private TextField inputField;
    private PrintWriter out;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Client");

        BorderPane root = new BorderPane();
        messageArea = new TextArea();
        messageArea.setEditable(false);
        ScrollPane scrollPane = new ScrollPane(messageArea);
        root.setCenter(scrollPane);

        inputField = new TextField();
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBox = new HBox(10, inputField, sendButton);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Platform.runLater(() -> {
                    messageArea.appendText("Connected to server\n");
                });

                new Thread(new ServerHandler(in)).start();

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageArea.appendText("Failed to connect to server\n");
                });
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (message != null && !message.isEmpty()) {
            out.println(message);
            inputField.clear();
        }
    }

    private class ServerHandler implements Runnable {
        private BufferedReader in;

        public ServerHandler(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    final String msg = message;
                    Platform.runLater(() -> {
                        messageArea.appendText(msg + "\n");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    messageArea.appendText("Connection lost\n");
                });
            }
        }
    }
}*/
