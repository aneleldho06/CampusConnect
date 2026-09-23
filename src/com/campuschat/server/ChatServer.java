package com.campuschat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main server for CampusChat.
 *
 * Responsibilities:
 * - Accept client connections
 * - Maintain registered users
 * - Maintain currently connected clients
 * - Broadcast and route messages
 */
public class ChatServer {
    public static final int PORT = 5000;

    private final Map<String, String> registeredUsers =
            new ConcurrentHashMap<>();

    private final Map<String, ClientHandler> connectedClients =
            new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public ChatServer() {
        // Demo accounts
        registeredUsers.put("user1", "pass123");
        registeredUsers.put("user2", "pass123");
    }

    public void start() {
        System.out.println("CampusChat server starting on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(this, socket);
                Thread thread = new Thread(handler, "ClientHandler");
                thread.start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Server error: " + e.getMessage());
            }
        }
    }

    public boolean registerUser(String username, String password) {
        return registeredUsers.putIfAbsent(username, password) == null;
    }

    public boolean authenticate(String username, String password) {
        String storedPassword = registeredUsers.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    public boolean addConnectedClient(String username, ClientHandler handler) {
        return connectedClients.putIfAbsent(username, handler) == null;
    }

    public void removeConnectedClient(String username) {
        if (username != null) {
            connectedClients.remove(username);
            broadcastSystemMessage(username + " disconnected.");
            broadcastUserList();
            System.out.println(username + " disconnected.");
        }
    }

    public ClientHandler getClient(String username) {
        return connectedClients.get(username);
    }

    public void broadcastMessage(String sender, String message) {
        String formattedMessage = "MESSAGE|" + sender + "|" + message;

        for (ClientHandler client : connectedClients.values()) {
            client.send(formattedMessage);
        }

        System.out.println(sender + ": " + message);
    }

    public void sendPrivateMessage(
            String sender,
            String recipient,
            String message
    ) {
        ClientHandler recipientHandler = connectedClients.get(recipient);

        if (recipientHandler == null) {
            ClientHandler senderHandler = connectedClients.get(sender);

            if (senderHandler != null) {
                senderHandler.send(
                        "ERROR|User '" + recipient + "' is not online."
                );
            }

            return;
        }

        String formattedMessage =
                "PRIVATE|" + sender + "|" + recipient + "|" + message;

        recipientHandler.send(formattedMessage);

        // Also display the private message in the sender's window.
        ClientHandler senderHandler = connectedClients.get(sender);

        if (senderHandler != null && !sender.equals(recipient)) {
            senderHandler.send(formattedMessage);
        }

        System.out.println(
                "[Private] " + sender + " -> " + recipient + ": " + message
        );
    }

    public void broadcastSystemMessage(String message) {
        String formattedMessage = "SYSTEM|" + message;

        for (ClientHandler client : connectedClients.values()) {
            client.send(formattedMessage);
        }
    }

    public void broadcastUserList() {
        StringBuilder users = new StringBuilder();

        for (String username : connectedClients.keySet()) {
            if (users.length() > 0) {
                users.append(",");
            }

            users.append(username);
        }

        String message = "USERS|" + users;

        for (ClientHandler client : connectedClients.values()) {
            client.send(message);
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}
