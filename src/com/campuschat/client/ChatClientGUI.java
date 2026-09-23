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
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientGUI extends JFrame {

    private final String username;
    private final ChatClient client;

    // Terminal colors
    private static final Color BG = new Color(5, 10, 7);
    private static final Color PANEL = new Color(8, 15, 10);
    private static final Color GREEN = new Color(0, 255, 100);
    private static final Color DARK_GREEN = new Color(0, 120, 45);
    private static final Color DIM_GREEN = new Color(70, 160, 95);
    private static final Color BORDER = new Color(0, 90, 35);

    private static final Font TERMINAL_FONT =
            new Font("Monospaced", Font.PLAIN, 14);

    private static final Font TERMINAL_BOLD =
            new Font("Monospaced", Font.BOLD, 14);

    private final JTextArea messageArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("[ SEND ]");

    private final DefaultListModel<String> usersModel =
            new DefaultListModel<>();

    private final JList<String> usersList =
            new JList<>(usersModel);

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss");

    public ChatClientGUI(String username, ChatClient client) {
        this.username = username;
        this.client = client;

        setTitle("CampusChat // " + username);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 450));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildInterface();

        client.setMessageConsumer(this::handleServerMessage);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(
                    java.awt.event.WindowEvent event) {

                client.close();
            }
        });
    }

    private void buildInterface() {

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        configureMessageArea();
        configureUsersList();

        JScrollPane messageScrollPane =
                new JScrollPane(messageArea);

        JScrollPane usersScrollPane =
                new JScrollPane(usersList);

        messageScrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        usersScrollPane.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        usersScrollPane.setPreferredSize(
                new Dimension(210, 0)
        );

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                messageScrollPane,
                usersScrollPane
        );

        splitPane.setDividerLocation(670);
        splitPane.setResizeWeight(1.0);

        splitPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        splitPane.setBackground(BG);

        add(splitPane, BorderLayout.CENTER);

        add(createInputPanel(), BorderLayout.SOUTH);
    }

    private JPanelWithHeader createHeader() {

        JPanelWithHeader header = new JPanelWithHeader();

        JLabel titleLabel =
                new JLabel("  CAMPUSCHAT // TERMINAL");

        titleLabel.setFont(
                new Font("Monospaced", Font.BOLD, 19)
        );

        titleLabel.setForeground(GREEN);

        JLabel userLabel =
                new JLabel("● ONLINE  //  " + username + "  ");

        userLabel.setFont(
                new Font("Monospaced", Font.BOLD, 13)
        );

        userLabel.setForeground(GREEN);

        header.add(titleLabel);
        header.add(userLabel);

        return header;
    }

    private void configureMessageArea() {

        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        messageArea.setFont(TERMINAL_FONT);

        messageArea.setForeground(GREEN);
        messageArea.setBackground(BG);

        messageArea.setCaretColor(GREEN);

        messageArea.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );
    }

    private void configureUsersList() {

        usersList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        usersList.setFont(TERMINAL_FONT);

        usersList.setForeground(GREEN);
        usersList.setBackground(PANEL);

        usersList.setSelectionBackground(DARK_GREEN);
        usersList.setSelectionForeground(Color.WHITE);

        usersList.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER),
                        " ONLINE_USERS ",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        TERMINAL_BOLD,
                        GREEN
                )
        );

        usersList.addListSelectionListener(event -> {

            if (!event.getValueIsAdjusting()) {

                String selectedUser =
                        usersList.getSelectedValue();

                if (selectedUser != null
                        && !selectedUser.equals(username)) {

                    inputField.setToolTipText(
                            "PRIVATE MESSAGE → " + selectedUser
                    );

                } else {

                    inputField.setToolTipText(
                            "PUBLIC MESSAGE → ALL USERS"
                    );
                }
            }
        });
    }

    private javax.swing.JPanel createInputPanel() {

        javax.swing.JPanel panel =
                new javax.swing.JPanel(new BorderLayout(8, 8));

        panel.setBackground(PANEL);

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        inputField.setFont(TERMINAL_FONT);

        inputField.setForeground(GREEN);
        inputField.setBackground(BG);

        inputField.setCaretColor(GREEN);

        inputField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(
                                8,
                                10,
                                8,
                                10
                        )
                )
        );

        inputField.setPreferredSize(
                new Dimension(0, 42)
        );

        inputField.setToolTipText(
                "PUBLIC MESSAGE → ALL USERS"
        );

        sendButton.setPreferredSize(
                new Dimension(100, 42)
        );

        sendButton.setFont(TERMINAL_BOLD);

        sendButton.setForeground(GREEN);
        sendButton.setBackground(BG);

        sendButton.setBorder(
                BorderFactory.createLineBorder(
                        DARK_GREEN
                )
        );

        sendButton.setFocusPainted(false);

        sendButton.setOpaque(true);

        sendButton.addActionListener(
                event -> sendMessage()
        );

        inputField.addActionListener(
                event -> sendMessage()
        );

        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        return panel;
    }

    private void sendMessage() {

        String message =
                inputField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        String selectedUser =
                usersList.getSelectedValue();

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

    private void handleServerMessage(String message) {

        SwingUtilities.invokeLater(() -> {

            if (message == null || message.isEmpty()) {
                return;
            }

            String[] parts =
                    message.split("\\|", -1);

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
                                "ERROR: " + parts[1]
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
            }
        });
    }

    private void handlePublicMessage(
            String[] parts) {

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

    private void handlePrivateMessage(
            String[] parts) {

        if (parts.length < 4) {
            return;
        }

        String sender = parts[1];
        String recipient = parts[2];
        String message = parts[3];

        appendMessage(
                "[" + currentTime() + "] "
                        + "[PRIVATE] "
                        + sender
                        + " -> "
                        + recipient
                        + ": "
                        + message
        );
    }

    private void updateUsers(
            String commaSeparatedUsers) {

        usersModel.clear();

        if (commaSeparatedUsers == null
                || commaSeparatedUsers.isBlank()) {

            return;
        }

        String[] users =
                commaSeparatedUsers.split(",");

        for (String user : users) {

            if (!user.isBlank()) {

                usersModel.addElement(
                        "> " + user
                );
            }
        }
    }

    private void appendMessage(
            String message) {

        messageArea.append(
                message + "\n"
        );

        messageArea.setCaretPosition(
                messageArea.getDocument().getLength()
        );
    }

    private void appendSystemMessage(
            String message) {

        appendMessage(
                "[" + currentTime() + "] "
                        + "[SYSTEM] "
                        + message
        );
    }

    private String currentTime() {

        return timeFormat.format(
                new Date()
        );
    }

    private static class JPanelWithHeader
            extends javax.swing.JPanel {

        JPanelWithHeader() {

            super(new GridLayout(1, 2));

            setBackground(BG);

            setBorder(
                    BorderFactory.createMatteBorder(
                            0,
                            0,
                            1,
                            0,
                            DARK_GREEN
                    )
            );
        }

        @Override
        public java.awt.Component add(
                java.awt.Component component) {

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