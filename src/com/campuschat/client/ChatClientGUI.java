package com.campuschat.client;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Swing chat window for one logged-in user.
 *
 * Public class name must match the filename:
 * ChatClientGUI.java -> public class ChatClientGUI
 */
public class ChatClientGUI extends JFrame {
    private final String username;
    private final ChatClient client;

    private final JTextArea messageArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    private final DefaultListModel<String> usersModel =
            new DefaultListModel<>();

    private final JList<String> usersList =
            new JList<>(usersModel);

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss");

    public ChatClientGUI(String username, ChatClient client) {
        this.username = username;
        this.client = client;

        setTitle("CampusChat - " + username);
        setSize(800, 530);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildInterface();

        /*
         * ChatClient calls this callback whenever a message arrives
         * from the server.
         */
        client.setMessageConsumer(this::handleServerMessage);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(
                    java.awt.event.WindowEvent event
            ) {
                client.close();
            }
        });
    }

    private void buildInterface() {
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        configureMessageArea();
        configureUsersList();

        JScrollPane messageScrollPane =
                new JScrollPane(messageArea);

        JScrollPane usersScrollPane =
                new JScrollPane(usersList);

        usersScrollPane.setPreferredSize(
                new Dimension(180, 0)
        );

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                messageScrollPane,
                usersScrollPane
        );

        splitPane.setDividerLocation(610);
        splitPane.setResizeWeight(1.0);

        add(splitPane, BorderLayout.CENTER);
        add(createInputPanel(), BorderLayout.SOUTH);
    }

    private JPanelWithHeader createHeader() {
        JPanelWithHeader header = new JPanelWithHeader();

        JLabel titleLabel = new JLabel("CampusChat");
        titleLabel.setFont(
                new Font("SansSerif", Font.BOLD, 20)
        );
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel(
                "Logged in as: " + username
        );
        userLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        userLabel.setForeground(
                new Color(225, 235, 250)
        );

        header.add(titleLabel);
        header.add(userLabel);

        return header;
    }

    private void configureMessageArea() {
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        messageArea.setBackground(Color.WHITE);
        messageArea.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );
    }

    private void configureUsersList() {
        usersList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        usersList.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        usersList.setBorder(
                BorderFactory.createTitledBorder(
                        "Online users"
                )
        );

        usersList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                String selectedUser =
                        usersList.getSelectedValue();

                if (selectedUser != null
                        && !selectedUser.equals(username)) {
                    inputField.setToolTipText(
                            "Send a private message to "
                                    + selectedUser
                    );
                } else {
                    inputField.setToolTipText(
                            "Send a public message"
                    );
                }
            }
        });
    }

    private javax.swing.JPanel createInputPanel() {
        javax.swing.JPanel panel =
                new javax.swing.JPanel(new BorderLayout(8, 8));

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        inputField.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        inputField.setPreferredSize(
                new Dimension(0, 40)
        );

        sendButton.setPreferredSize(
                new Dimension(90, 40)
        );

        sendButton.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        sendButton.setBackground(
                new Color(55, 115, 220)
        );

        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        sendButton.addActionListener(event -> sendMessage());

        inputField.addActionListener(event -> sendMessage());

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private void sendMessage() {
        String message = inputField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        String selectedUser = usersList.getSelectedValue();

        /*
         * If another online user is selected, send a private message.
         * Otherwise, send a public broadcast message.
         */
        if (selectedUser != null
                && !selectedUser.equals(username)) {
            client.sendPrivateMessage(
                    selectedUser,
                    message
            );
        } else {
            client.sendPublicMessage(message);
        }

        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    /**
     * Handles messages received from the server.
     *
     * Network code runs on a background thread, so all Swing updates
     * are placed on the Event Dispatch Thread using invokeLater.
     */
    private void handleServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message == null || message.isEmpty()) {
                return;
            }

            String[] parts = message.split("\\|", -1);

            if (parts.length == 0) {
                return;
            }

            switch (parts[0]) {
                case "MESSAGE":
                    handlePublicMessage(parts);
                    break;

                case "PRIVATE":
                    handlePrivateMessage(parts);
                    break;

                case "SYSTEM":
                    if (parts.length >= 2) {
                        appendSystemMessage(parts[1]);
                    }
                    break;

                case "USERS":
                    if (parts.length >= 2) {
                        updateUsers(parts[1]);
                    }
                    break;

                case "ERROR":
                    if (parts.length >= 2) {
                        appendSystemMessage(
                                "Error: " + parts[1]
                        );
                    }
                    break;

                case "CONNECTION_CLOSED":
                    appendSystemMessage(
                            "Disconnected from server."
                    );

                    sendButton.setEnabled(false);
                    inputField.setEnabled(false);
                    break;

                default:
                    appendSystemMessage(message);
                    break;
            }
        });
    }

    private void handlePublicMessage(String[] parts) {
        if (parts.length < 3) {
            return;
        }

        String sender = parts[1];
        String message = parts[2];

        appendMessage(
                "[" + currentTime() + "] "
                        + sender
                        + ": "
                        + message
        );
    }

    private void handlePrivateMessage(String[] parts) {
        if (parts.length < 4) {
            return;
        }

        String sender = parts[1];
        String recipient = parts[2];
        String message = parts[3];

        appendMessage(
                "[" + currentTime() + "] "
                        + "[Private] "
                        + sender
                        + " -> "
                        + recipient
                        + ": "
                        + message
        );
    }

    private void updateUsers(String commaSeparatedUsers) {
        usersModel.clear();

        if (commaSeparatedUsers == null
                || commaSeparatedUsers.isBlank()) {
            return;
        }

        String[] users =
                commaSeparatedUsers.split(",");

        for (String user : users) {
            if (!user.isBlank()) {
                usersModel.addElement(user);
            }
        }
    }

    private void appendMessage(String message) {
        messageArea.append(message);
        messageArea.append("\n");

        messageArea.setCaretPosition(
                messageArea.getDocument().getLength()
        );
    }

    private void appendSystemMessage(String message) {
        appendMessage(
                "[" + currentTime() + "] * " + message
        );
    }

    private String currentTime() {
        return timeFormat.format(new Date());
    }

    /**
     * Small header panel with a blue background.
     */
    private static class JPanelWithHeader
            extends javax.swing.JPanel {

        JPanelWithHeader() {
            super(new GridLayout(1, 2));

            setBackground(
                    new Color(40, 85, 160)
            );

            setBorder(
                    BorderFactory.createEmptyBorder(
                            10,
                            12,
                            10,
                            12
                    )
            );
        }

        @Override
        public java.awt.Component add(java.awt.Component component) {
            if (getComponentCount() == 0) {
                return super.add(component);
            } else {
                javax.swing.JPanel rightPanel =
                        new javax.swing.JPanel(
                                new BorderLayout()
                        );

                rightPanel.setOpaque(false);
                rightPanel.add(
                        component,
                        BorderLayout.EAST
                );

                return super.add(rightPanel);
            }
        }
    }
}
