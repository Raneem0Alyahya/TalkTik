package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The ClientHandler class is responsible for managing communication with a single client.
 * It handles incoming messages from the client and routes them to the appropriate destination
 * (global chat or private message).
 */
public class ClientHandler extends Thread {
    private Server server; // Reference to the server
    private Socket clientSocket; // Socket for communication with the client
    private PrintWriter output; // Output stream to send messages to the client
    private Scanner input; // Input stream to receive messages from the client
    private String clientName; // Name of the client
    private String receiverName; // Name of the receiver for private messages

    /**
     * Constructor for the ClientHandler.
     *
     * @param server       The server instance.
     * @param clientSocket The socket for communication with the client.
     */
    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    /**
     * Returns the name of the client.
     *
     * @return The client's name.
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Sends a message to the client.
     *
     * @param message The message to be sent.
     */
    public void sendMessage(String message) {
        output.println(message);
    }

    /**
     * Sends a global message to the client.
     *
     * @param message The global message to be sent.
     */
    public void sendGlobalMessage(String message) {
        output.println(message);
    }

    /**
     * The run method handles communication with the client.
     * It reads messages from the client and processes them (global chat, private message, or exit).
     */
    @Override
    public void run() {
        try {
            // Initialize output and input streams
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new Scanner(clientSocket.getInputStream());

            // Read the client's name
            if (input.hasNextLine()) {
                clientName = input.nextLine();
                System.out.println(clientName + " connected");
            }

            // Process incoming messages from the client
            while (input.hasNextLine()) {
                String message = input.nextLine();

                // Handle exit request
                if (message.equals("EXIT")) {
                    server.removeClient(this); // Remove the client from the server
                    clientSocket.close(); // Close the client socket
                    System.out.println(clientName + " disconnected");
                    break;
                }

                // Handle other messages
                else {
                    // Split the message into parts (receiver name and message content)
                    String[] parts = message.split("#");
                    receiverName = parts[0];
                    String privateMessage = parts[1];

                    // Check if the client exists
                    if (receiverName.equals("Client Exist")) {
                        server.clientExist(this, privateMessage);
                    }

                    // Handle global chat messages
                    else if (receiverName.equals("GLOBAL CHAT")) {
                        server.sendGlobalMessage(message);
                    }

                    // Handle private messages
                    else {
                        server.sendPrivateMessage(clientName, receiverName, privateMessage);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IO exceptions
        }
    }
}