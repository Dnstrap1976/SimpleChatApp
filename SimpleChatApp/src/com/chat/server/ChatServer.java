package com.chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8888; // Choose your desired port
    private static Set<String> usernames = new HashSet<>();
    private static Set<UserThread> userThreads = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New user connected");

                UserThread newUser = new UserThread(socket);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    public static void addUsername(String username) {
        usernames.add(username);
    }

    public static void removeUser(String username, UserThread aUser) {
        boolean removed = usernames.remove(username);
        if (removed) {
            userThreads.remove(aUser);
            System.out.println("The user " + username + " quitted");
        }
    }

    public static Set<String> getUsernames() {
        return usernames;
    }

    public static boolean hasUsers() {
        return !usernames.isEmpty();
    }
}