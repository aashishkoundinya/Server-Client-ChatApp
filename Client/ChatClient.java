package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            new Thread(new ServerHandler(socket)).start();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println(username);

            System.out.println("-----------------" + username + "'s Terminal-----------------");

            new Thread(new ServerHandler(socket)).start();

            while (scanner.hasNextLine()) {
                out.println(scanner.nextLine());
            }

            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                e.printStackTrace();
            }
        }
    }
}
