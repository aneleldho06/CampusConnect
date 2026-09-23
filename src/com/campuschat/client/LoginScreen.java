package com.campuschat.client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Login and registration window for CampusChat.
 */
public class LoginScreen extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Login");
    private final JButton registerButton = new JButton("Register");
    private final JLabel statusLabel = new JLabel(" ");

    private static final String HOST = "localhost";
    private static final int PORT = ChatServerPort.PORT;

    public LoginScreen() {
        setTitle("CampusChat - Login");
        setSize(380, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildInterface();
    }

    private void buildInterface() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("CampusChat", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.add(loginButton);
        buttons.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(buttons, gbc);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        panel.add(statusLabel, gbc);

        add(panel);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());
        passwordField.addActionListener(e -> login());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || password.isBlank()) {
            setStatus("Enter username and password.");
            return;
        }

        setButtonsEnabled(false);

        ChatClient client = new ChatClient(HOST, PORT);

        try {
            client.connect(username, password, response ->
                    SwingUtilities.invokeLater(() -> {
                        if (response == null) {
                            setStatus("No response from server.");
                            setButtonsEnabled(true);
                            client.close();
                            return;
                        }

                        String[] parts = response.split("\\|", -1);

                        if (parts.length > 0 && "AUTH_OK".equals(parts[0])) {

    String loggedInUser = parts.length >= 2
            ? parts[1]
            : username;

    ChatClientGUI chatGUI =
            new ChatClientGUI(loggedInUser, client);

    chatGUI.setVisible(true);

    dispose();

} else if (parts.length > 0 && "AUTH_FAIL".equals(parts[0])) {

    String error = parts.length >= 2
            ? parts[1]
            : "Login failed.";

    setStatus(error);
    setButtonsEnabled(true);
    client.close();

} else {

    // Ignore SYSTEM, USERS, etc.
    // These messages belong to the chat window.
}
                    }));
        } catch (IOException e) {
            setStatus("Could not connect to server.");
            setButtonsEnabled(true);
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isBlank() || password.isBlank()) {
            setStatus("Enter username and password.");
            return;
        }

        setButtonsEnabled(false);

        ChatClient client = new ChatClient(HOST, PORT);

        try {
            client.register(username, password, response ->
                    SwingUtilities.invokeLater(() -> {
                        if (response == null) {
                            setStatus("No response from server.");
                        } else {
                            String[] parts = response.split("\\|", -1);
                            setStatus(parts.length >= 2 ? parts[1] : response);
                        }
                        setButtonsEnabled(true);
                    }));
        } catch (IOException e) {
            setStatus("Could not connect to server.");
            setButtonsEnabled(true);
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void setButtonsEnabled(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerButton.setEnabled(enabled);
    }

    /** Keeps the port in one place without importing the server class into the UI. */
    private static final class ChatServerPort {
        private static final int PORT = 5000;
    }
}
