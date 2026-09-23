package com.campuschat;

import com.campuschat.client.LoginScreen;
import com.campuschat.server.ChatServer;

import javax.swing.SwingUtilities;

/**
 * Optional launcher for the complete local demo.
 */
public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(
                () -> new ChatServer().start(),
                "CampusChatServer"
        );

        serverThread.setDaemon(true);
        serverThread.start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        SwingUtilities.invokeLater(() -> {
            LoginScreen user1Window = new LoginScreen();
            user1Window.setTitle("CampusChat - User 1");
            user1Window.setVisible(true);

            LoginScreen user2Window = new LoginScreen();
            user2Window.setTitle("CampusChat - User 2");
            user2Window.setLocation(
                    user1Window.getX() + 450,
                    user1Window.getY()
            );
            user2Window.setVisible(true);
        });
    }
}
