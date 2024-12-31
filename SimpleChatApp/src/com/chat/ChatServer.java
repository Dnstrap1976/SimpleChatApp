package com.chat;

// ChatServer.java

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8888;
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
            System.out.println("User " + username + " quitted");
        }
    }

    public static Set<String> getUsernames() {
        return usernames;
    }

    static class UserThread extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private String username;

        public UserThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);

                printUsers();

                username = reader.readLine();
                addUsername(username);
                broadcast("New user joined: " + username, this);

                String serverMessage;
                do {
                    serverMessage = reader.readLine();
                    broadcast(username + ": " + serverMessage, this);
                } while (!serverMessage.equals("bye"));

                removeUser(username, this);
                socket.close();

            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        void printUsers() {
            if (getUsernames().isEmpty()) {
                writer.println("No other users connected");
            } else {
                writer.println("Connected users: " + getUsernames());
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }
    }
}