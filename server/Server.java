package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The Server class is responsible for managing client connections and routing messages.
 * It listens for incoming client connections, creates a ClientHandler for each client,
 * and manages the list of connected clients.
 */
public class Server {
    private static int port = 6789; // Default port for the server
    private ServerSocket serverSocket; // Server socket to listen for client connections
    private List<ClientHandler> clients; // List to keep track of connected clients

    /**
     * The main method starts the server and listens for client connections.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.connect(port); // Start the server on the specified port
    }

    /**
     * Initializes the server and starts listening for client connections.
     *
     * @param portNumber The port number on which the server will listen.
     */
    public void connect(int portNumber) {
        clients = new ArrayList<>(); // Initialize the list of clients

        try {
            serverSocket = new ServerSocket(portNumber); // Create a server socket
            System.out.println("Server started on port " + portNumber);

            // Continuously accept new client connections
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a new client connection
                System.out.println("New client connected");

                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clients.add(clientHandler); // Add the client to the list
                clientHandler.start(); // Start the client handler thread
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle any IO exceptions
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close(); // Close the server socket
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle any IO exceptions during closing
            }
        }
    }

    /**
     * Checks if a client with the specified name exists.
     *
     * @param handler      The ClientHandler requesting the check.
     * @param receiverName The name of the client to check.
     */
    public void clientExist(ClientHandler handler, String receiverName) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(receiverName)) {
                // Notify the handler that the client exists
                handler.sendMessage("Client Exist response#true#" + receiverName);
                return;
            }
        }
        // Notify the handler that the client does not exist
        handler.sendMessage("Client Exist response#false#" + receiverName);
    }

    /**
     * Sends a global message to all connected clients.
     *
     * @param globalMessage The message to be broadcasted to all clients.
     */
    public void sendGlobalMessage(String globalMessage) {
        for (ClientHandler client : clients) {
            client.sendMessage(globalMessage); // Send the message to each client
        }
    }

    /**
     * Sends a private message from one client to another.
     *
     * @param senderName   The name of the sender.
     * @param receiverName The name of the receiver.
     * @param message      The message to be sent.
     */
    public void sendPrivateMessage(String senderName, String receiverName, String message) {
        for (ClientHandler client : clients) {
            if (client.getClientName().equals(receiverName)) {
                // Send the message to the receiver
                client.sendMessage(senderName + "#" + message);
                break;
            }
        }
    }

    /**
     * Removes a client from the list of connected clients.
     *
     * @param clientHandler The ClientHandler to be removed.
     */
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler); // Remove the client from the list
    }
}
