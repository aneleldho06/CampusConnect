package com.campuschat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles one client connection.
 *
 * Every connected client receives its own ClientHandler thread.
 */
public class ClientHandler implements Runnable {
    private final ChatServer server;
    private final Socket socket;

    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            String firstLine = reader.readLine();

            if (firstLine == null || !handleAuthentication(firstLine)) {
                closeConnection();
                return;
            }

            server.broadcastSystemMessage(username + " joined the chat.");
            server.broadcastUserList();

            String line;

            while ((line = reader.readLine()) != null) {
                handleCommand(line);
            }
        } catch (IOException e) {
            System.out.println(
                    "Connection lost for "
                            + (username == null ? "unknown user" : username)
            );
        } finally {
            server.removeConnectedClient(username);
            closeConnection();
        }
    }

    private boolean handleAuthentication(String line) {
        String[] parts = line.split("\\|", -1);

        if (parts.length == 0) {
            return false;
        }

        String command = parts[0];

        try {
            if ("LOGIN".equals(command) && parts.length >= 3) {
                String requestedUsername = parts[1];
                String password = parts[2];

                if (!server.authenticate(requestedUsername, password)) {
                    send("AUTH_FAIL|Invalid username or password.");
                    return false;
                }

                if (!server.addConnectedClient(requestedUsername, this)) {
                    send("AUTH_FAIL|That user is already connected.");
                    return false;
                }

                username = requestedUsername;
                send("AUTH_OK|" + username);

                System.out.println(username + " connected.");
                return true;
            }

            if ("REGISTER".equals(command) && parts.length >= 3) {
                String requestedUsername = parts[1];
                String password = parts[2];

                if (requestedUsername.isBlank() || password.isBlank()) {
                    send("REGISTER_FAIL|Username and password are required.");
                    return false;
                }

                if (!server.registerUser(requestedUsername, password)) {
                    send("REGISTER_FAIL|Username already exists.");
                    return false;
                }

                send("REGISTER_OK|Account created.");
                return false;
            }

            send("AUTH_FAIL|Invalid authentication request.");
        } catch (Exception e) {
            send("AUTH_FAIL|Authentication error.");
        }

        return false;
    }

    private void handleCommand(String line) {
        String[] parts = line.split("\\|", -1);

        if (parts.length == 0) {
            return;
        }

        switch (parts[0]) {
            case "MSG":
                if (parts.length >= 2 && !parts[1].isBlank()) {
                    server.broadcastMessage(username, parts[1]);
                }
                break;

            case "PRIVATE":
                if (parts.length >= 3) {
                    String recipient = parts[1];
                    String message = parts[2];

                    if (!message.isBlank()) {
                        server.sendPrivateMessage(
                                username,
                                recipient,
                                message
                        );
                    }
                }
                break;

            default:
                send("ERROR|Unknown command.");
        }
    }

    public synchronized void send(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
