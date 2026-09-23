package com.campuschat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Networking layer for one chat client.
 */
public class ChatClient {
    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private Thread listenerThread;
    private Consumer<String> messageConsumer;

    public void setMessageConsumer(
        java.util.function.Consumer<String> messageConsumer
) {
    this.messageConsumer = messageConsumer;
}


    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(
            String username,
            String password,
            Consumer<String> messageConsumer
    ) throws IOException {
        this.messageConsumer = messageConsumer;

        socket = new Socket(host, port);

        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        writer = new PrintWriter(
                socket.getOutputStream(),
                true
        );

        send("LOGIN|" + username + "|" + password);

        listenerThread = new Thread(
                this::listenForMessages,
                "ChatClientListener"
        );

        listenerThread.start();
    }

    public void register(
            String username,
            String password,
            Consumer<String> responseConsumer
    ) throws IOException {
        Socket registerSocket = new Socket(host, port);

        BufferedReader registerReader = new BufferedReader(
                new InputStreamReader(registerSocket.getInputStream())
        );

        PrintWriter registerWriter = new PrintWriter(
                registerSocket.getOutputStream(),
                true
        );

        registerWriter.println("REGISTER|" + username + "|" + password);

        String response = registerReader.readLine();

        registerSocket.close();

        responseConsumer.accept(response);
    }

    private void listenForMessages() {
        try {
            String line;

            while ((line = reader.readLine()) != null) {
                if (messageConsumer != null) {
                    messageConsumer.accept(line);
                }
            }
        } catch (IOException e) {
            if (messageConsumer != null) {
                messageConsumer.accept("CONNECTION_CLOSED|Server disconnected.");
            }
        }
    }

    public synchronized void send(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public void sendPublicMessage(String message) {
        send("MSG|" + message);
    }

    public void sendPrivateMessage(
            String recipient,
            String message
    ) {
        send("PRIVATE|" + recipient + "|" + message);
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
