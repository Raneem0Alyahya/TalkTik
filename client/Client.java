package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * The Client class represents the main chat application.
 * It provides a user interface for global chat, private messaging, and user profile management.
 */
public class Client extends JFrame {
    private String clientName; // Name of the client
    private Socket socket; // Socket for communication with the server
    private PrintWriter output; // Output stream to send messages to the server
    private Scanner input; // Input stream to receive messages from the server
    private boolean running; // Flag to control the receive thread
    private JPanel conversations; // Panel to display chat buttons
    private JTextArea globalChatArea; // Text area to display global chat messages
    private static String ip = "192.168.8.119"; // Server IP address
    private static int port = 6789; // Server port

    List<Window> openedWindows = new LinkedList<>(); // List of opened private chat windows
    Map<Window, JButton> buttonsMap = new HashMap<>(); // Map to associate windows with their buttons

    /**
     * Returns the name of the client.
     *
     * @return The client's name.
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Constructor for the Client class.
     * Initializes the user interface and sets up event listeners.
     */
    public Client() {
        setLayout(new BorderLayout());
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));
        setTitle("TALKTIK");
        setBackground(Color.decode("#FAF3F0"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Add a window listener to handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                try {
                    // Notify the server about the exit and close the socket
                    output.println("EXIT");
                    socket.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // Set up the top panel for adding new private chats
        JLabel addNew = new JLabel("Add new private chat");
        addNew.setFont(new Font("Arial", Font.BOLD, 20));
        JTextField receiverName = new JTextField(10);
        receiverName.setFont(new Font("Arial", Font.BOLD, 30));
        JButton open = new JButton("open");
        open.setFont(new Font("Arial", Font.BOLD, 20));
        open.setBackground(Color.decode("#fce1e4"));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(addNew);
        topPanel.add(receiverName);
        topPanel.add(open);
        add(topPanel, BorderLayout.NORTH);

        // Set up the center panel for chats and global chat
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2));
        add(centerPanel, BorderLayout.CENTER);
        centerPanel.setBackground(Color.decode("#fce1e4"));
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Set up the right panel for profile and global chat
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 2));

        // Set up the profile panel
        JPanel profile = new JPanel();
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        JLabel profileTitle = new JLabel("Profile");
        profileTitle.setFont(new Font("Arial", Font.BOLD, 70));
        profileTitle.setBackground(Color.decode("#e8dff5"));
        profileTitle.setOpaque(true);
        profile.add(profileTitle);
        rightPanel.add(profile);

        profileTitle.setHorizontalAlignment(SwingConstants.CENTER);
        profileTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, profileTitle.getPreferredSize().height));

        // Set up the global chat panel
        JPanel globalChat = new JPanel();
        globalChat.setLayout(new BorderLayout());

        JLabel globalChatTitle = new JLabel("Global Chat");
        globalChatTitle.setFont(new Font("Arial", Font.BOLD, 70));
        globalChatTitle.setBackground(Color.decode("#e8dff5"));
        globalChatTitle.setOpaque(true);
        globalChatTitle.setHorizontalAlignment(SwingConstants.CENTER);
        globalChatTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, globalChatTitle.getPreferredSize().height));
        globalChat.add(globalChatTitle, BorderLayout.NORTH);

        JButton send = new JButton("Send");
        send.setFont(new Font("Arial", Font.BOLD, 20));
        send.setBackground(Color.decode("#fce1e4"));

        JTextArea textInput = new JTextArea();
        textInput.setColumns(20);
        textInput.setRows(2);
        textInput.setLineWrap(true);
        textInput.setFont(new Font("Arial", Font.PLAIN, 20));

        JScrollPane textScroll = new JScrollPane(textInput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(60, 60));
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(textScroll);
        bottomPanel.add(send);
        globalChat.add(bottomPanel, BorderLayout.SOUTH);

        globalChatArea = new JTextArea();
        globalChatArea.setEditable(false);
        globalChatArea.setLineWrap(true);
        globalChatArea.setWrapStyleWord(true);
        globalChatArea.setFont(new Font("Arial", Font.PLAIN, 30));
        globalChatArea.setForeground(Color.BLACK);

        JScrollPane globalChatAreaScroll = new JScrollPane(globalChatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        globalChatArea.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                globalChatAreaScroll.getVerticalScrollBar().setValue(globalChatAreaScroll.getVerticalScrollBar().getMaximum());
            }
        });

        globalChat.add(globalChatAreaScroll, BorderLayout.CENTER);
        rightPanel.add(globalChat);

        // Set up the chats panel
        JPanel chats = new JPanel(new BorderLayout());
        chats.setBorder(new MatteBorder(0, 0, 0, 1, Color.BLACK));

        JLabel conversationsTitle = new JLabel("Chats");
        conversationsTitle.setFont(new Font("Arial", Font.BOLD, 70));
        conversationsTitle.setBackground(Color.decode("#e8dff5"));
        conversationsTitle.setOpaque(true);

        conversations = new JPanel();
        conversations.setLayout(new BoxLayout(conversations, BoxLayout.Y_AXIS));

        JScrollPane chatsScrollPane = new JScrollPane(conversations);
        chatsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        conversationsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        conversationsTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, conversationsTitle.getPreferredSize().height));

        chats.add(conversationsTitle, BorderLayout.PAGE_START);
        chats.add(chatsScrollPane, BorderLayout.CENTER);

        centerPanel.add(chats);
        centerPanel.add(rightPanel);

        // Prompt the user to enter a username
        do {
            clientName = JOptionPane.showInputDialog(null, "Enter Your username. \nUser name must contain at most 10 characters");
            if (clientName == null)
                System.exit(0);
            if (clientName.length() < 1 || clientName.length() > 10)
                JOptionPane.showMessageDialog(null, "The username that you has entered isn't valid. Please try again.");
        } while (clientName.length() < 1 || clientName.length() > 10);

        // Set up the profile information
        JLabel userNameLabel = new JLabel("Username");
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 30));
        profile.add(Box.createRigidArea(new Dimension(0, 30)));
        profile.add(userNameLabel);
        profile.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel userName = new JLabel(clientName);
        userName.setFont(new Font("Arial", Font.ITALIC, 20));
        profile.add(userName);

        JLabel signUpTimeLabel = new JLabel("Sign up time");
        signUpTimeLabel.setFont(new Font("Arial", Font.BOLD, 30));
        profile.add(Box.createRigidArea(new Dimension(0, 30)));
        profile.add(signUpTimeLabel);
        profile.add(Box.createRigidArea(new Dimension(0, 10)));

        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        JLabel signUpTime = new JLabel((now.format(formatter)));
        signUpTime.setFont(new Font("Arial", Font.ITALIC, 20));
        profile.add(signUpTime);

        // Add action listener for the open button
        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!receiverName.getText().trim().isEmpty()) {
                    // Check if the receiver exists
                    output.println("Client Exist#" + receiverName.getText());
                    receiverName.setText("");
                }
            }
        });

        // Add key listener for the receiver name text field
        receiverName.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    open.doClick();
                }
            }
        });

        // Add action listener for the send button in the global chat
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!textInput.getText().trim().isEmpty()) {
                    String globalMessage = textInput.getText().replaceAll("\n", "@@@@");
                    textInput.setText("");
                    output.println("GLOBAL CHAT#" + clientName + ": " + globalMessage);
                }
            }
        });
    }

    /**
     * Adds a new private chat window for the specified receiver.
     *
     * @param receiverName The name of the receiver.
     */
    public void addCleint(String receiverName) {
        Optional<Window> result = openedWindows.stream()
                .filter(a -> a.receiverName.equals(receiverName))
                .findFirst();

        if (result.isPresent()) {
            result.get().setVisible(true);
        } else {
            Window newWindow = new Window(Client.this, receiverName);
            openedWindows.add(newWindow);
            addButton(newWindow, receiverName);
        }
    }

    /**
     * Adds a button for the new private chat window.
     *
     * @param newWindow    The new private chat window.
     * @param receiverName The name of the receiver.
     */
    public void addButton(Window newWindow, String receiverName) {
        ActionListener windowDisplayListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton btn = (JButton) e.getSource();
                Window win = null;

                for (Map.Entry<Window, JButton> entry : buttonsMap.entrySet()) {
                    if (entry.getValue().equals(btn)) {
                        win = entry.getKey();
                        win.setVisible(true);
                        win.setExtendedState(Frame.MAXIMIZED_BOTH);
                        break;
                    }
                }
            }
        };

        JButton btn;
        if (receiverName.equals(clientName))
            btn = new JButton(receiverName + "(you)");
        else
            btn = new JButton(receiverName);

        btn.setContentAreaFilled(false);
        btn.setBorder(null);
        btn.setOpaque(true);
        btn.setBackground(Color.decode("#fce1e4"));
        btn.setFont(new Font("Arial", Font.BOLD, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getMinimumSize().height));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.decode("#f8bdc4"));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.decode("#fce1e4"));
            }
        });

        buttonsMap.put(newWindow, btn);
        btn.addActionListener(windowDisplayListener);
        conversations.add(Box.createRigidArea(new Dimension(0, 10)));
        conversations.add(btn);

        conversations.revalidate();
        conversations.repaint();
        newWindow.setVisible(true);
    }

    /**
     * The main method starts the client application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Client client = new Client();
                client.connect(ip, port);
            }
        });
    }

    /**
     * Connects the client to the server.
     *
     * @param serverAddress The server's IP address.
     * @param portNumber    The server's port number.
     */
    public void connect(String serverAddress, int portNumber) {
        try {
            socket = new Socket(serverAddress, portNumber);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new Scanner(socket.getInputStream());
            running = true;

            setVisible(true);

            // Send the client's name to the server
            output.println(clientName);

            // Start a thread to handle incoming messages
            Thread receiveThread = new Thread(new ReceiveHandler());
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a private message to the specified recipient.
     *
     * @param recipient The name of the recipient.
     * @param message   The message to be sent.
     */
    public void sendPrivateMessage(String recipient, String message) {
        output.println(recipient + "#" + message);
    }

    /**
     * Removes a private chat window from the client.
     *
     * @param window The window to be removed.
     */
    public void removeWindow(Window window) {
        openedWindows.remove(window);
        JButton removedButton = buttonsMap.remove(window);
        conversations.remove(removedButton);
        conversations.revalidate();
        conversations.repaint();
    }

    /**
     * The ReceiveHandler class is responsible for handling incoming messages from the server.
     */
    private class ReceiveHandler implements Runnable {
        @Override
        public void run() {
            try {
                while (running) {
                    if (input.hasNextLine()) {
                        String message = input.nextLine();
                        String[] parts = message.split("#");
                        String sender = parts[0];
                        String privateMessage = parts[1];
                        privateMessage = privateMessage.replaceAll("@@@@", "\n");

                        if (sender.equals("GLOBAL CHAT")) {
                            globalChatArea.append(privateMessage + "\n\n\n");
                        } else if (sender.equals("Client Exist response")) {
                            String receiverName = parts[2];
                            if (privateMessage.equals("true"))
                                addCleint(receiverName);
                            else
                                JOptionPane.showMessageDialog(null, "The username doesn't exist.");
                        } else {
                            Optional<Window> result = openedWindows.stream()
                                    .filter(a -> a.receiverName.equals(sender))
                                    .findFirst();

                            JLabel message4 = new JLabel("<html><p style='width:300px'>" + privateMessage.replace("\n", "<br>") + "</p></html>");
                            JLabel message3 = new JLabel("<html><p style='width:300px'>" + privateMessage.replace("\n", "<br>") + "</p></html>");
                            JLabel space = new JLabel(" ");
                            space.setPreferredSize(new Dimension(10, 10));
                            JLabel space2 = new JLabel(" ");
                            space2.setPreferredSize(new Dimension(10, 10));

                            message4.setOpaque(true);
                            message4.setBackground(Color.decode("#daeaf6"));
                            message4.setForeground(Color.black);
                            message4.setFont(new Font("Arial", Font.BOLD, 30));

                            message3.setOpaque(false);
                            message3.setForeground(Color.decode("#faf3f0"));
                            message3.setFont(new Font("Arial", Font.BOLD, 30));

                            if (result.isPresent()) {
                                JPanel senderInfo = new JPanel();
                                senderInfo.setLayout(new BoxLayout(senderInfo, BoxLayout.X_AXIS));
                                senderInfo.add(Box.createHorizontalGlue());
                                senderInfo.setOpaque(false);

                                JPanel senderInfo2 = new JPanel();
                                senderInfo2.setOpaque(false);

                                JLabel senderName = new JLabel(":" + sender);
                                senderName.setHorizontalAlignment(JLabel.RIGHT);
                                senderInfo.add(senderName);

                                result.get().messages2.add(senderInfo);
                                result.get().messages2.add(message4);
                                result.get().messages2.add(space);
                                result.get().messages2.revalidate();
                                result.get().messages2.repaint();

                                result.get().messages.add(new JLabel(" "));
                                result.get().messages.add(message3);
                                result.get().messages.add(space2);
                                result.get().messages.revalidate();
                                result.get().messages.repaint();

                                result.get().setVisible(true);
                            } else {
                                Window newWindow = new Window(Client.this, sender);
                                openedWindows.add(newWindow);
                                addButton(newWindow, sender);

                                JPanel senderInfo = new JPanel();
                                senderInfo.setLayout(new BoxLayout(senderInfo, BoxLayout.X_AXIS));
                                senderInfo.add(Box.createHorizontalGlue());
                                JLabel senderName = new JLabel(":" + sender);
                                senderName.setHorizontalAlignment(JLabel.RIGHT);
                                senderInfo.add(senderName);

                                newWindow.messages2.add(senderInfo);
                                newWindow.messages2.add(message4);
                                newWindow.messages2.add(space);
                                newWindow.messages2.revalidate();
                                newWindow.messages2.repaint();

                                newWindow.messages.add(new JLabel(" "));
                                newWindow.messages.add(message3);
                                newWindow.messages.add(space2);
                                newWindow.messages.revalidate();
                                newWindow.messages.repaint();

                                newWindow.setVisible(true);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
