import java.io.*;
import java.net.*;
import java.util.*;

public class ChatApp {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private Map<String, List<String>> messageHistory;

    public ChatApp() {
        clients = new ArrayList<>();
        messageHistory = new HashMap<>();
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message, String sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(sender + ": " + message);
        }
        // Store message in history
        messageHistory.get(sender).add(message);
    }

    public void privateMessage(String message, String sender, String receiver) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(receiver)) {
                client.sendMessage("(Private) " + sender + ": " + message);
                // Store message in history
                messageHistory.get(sender).add("(Private to " + receiver + ") " + message);
            }
        }
    }

    public void createChatroom(String chatroomName) {
        // Create a new chatroom
        // Implementation details omitted
    }

    public List<String> getMessageHistory(String username) {
        return messageHistory.getOrDefault(username, new ArrayList<>());
    }

    public static void main(String[] args) {
        ChatApp chatApp = new ChatApp();
        chatApp.start(8000);
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;
    private ChatApp chatApp;
    private String clientName;

    public ClientHandler(Socket socket, ChatApp chatApp) {
        try {
            clientSocket = socket;
            inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            this.chatApp = chatApp;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }

    public void sendMessage(String message) {
        outputWriter.println(message);
    }

    @Override
    public void run() {
        try {
            // Client authentication and name assignment
            // Implementation details omitted

            // Handle incoming messages
            String message;
            while ((message = inputReader.readLine()) != null) {
                if (message.startsWith("/private ")) {
                    // Private message
                    String[] parts = message.split(" ", 3);
                    String receiver = parts[1];
                    String privateMessage = parts[2];
                    chatApp.privateMessage(privateMessage, clientName, receiver);
                } else if (message.startsWith("/chatroom ")) {
                    // Chatroom message
                    String[] parts = message.split(" ", 2);
                    String chatroomMessage = parts[1];
                    // Implementation details omitted
                } else {
                    // Broadcast message
                    chatApp.broadcast(message, clientName);
                }
            }

            // Clean up resources
            inputReader.close();
            outputWriter.close();
            clientSocket.close();
            chatApp.getClients().remove(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
