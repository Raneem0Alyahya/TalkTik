package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * The Window class represents a private chat window between two users.
 * It provides a user interface for sending and receiving private messages.
 */
public class Window extends JFrame {
    public String receiverName; // Name of the receiver
    private Client client; // Reference to the main client
    private String clientName; // Name of the current client
    JPanel messages; // Panel to display sent messages
    JPanel messages2; // Panel to display received messages

    /**
     * Constructor for the Window class.
     *
     * @param client       The main client instance.
     * @param receiverName The name of the receiver for this private chat.
     */
    public Window(Client client, String receiverName) {
        this.client = client;
        this.receiverName = receiverName;
        this.clientName = client.getClientName();

        // Set up the window
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));
        setTitle(receiverName);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Add a window listener to handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                // Hide the window instead of closing it
                Window.this.setVisible(false);
            }
        });

        // Set up the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem clearItem = new JMenuItem("Disconnect");

        // Add action listener for the disconnect menu item
        clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Remove the window from the client and hide it
                client.removeWindow(Window.this);
                setVisible(false);
            }
        });

        fileMenu.add(clearItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Set up the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2));
        mainPanel.setBackground(Color.decode("#E8DFF5"));
        mainPanel.setBorder(new EmptyBorder(10, 30, 10, 30));

        // Set up the text input area
        JTextArea textInput = new JTextArea();
        textInput.setColumns(30);
        textInput.setRows(2);
        textInput.setLineWrap(true);
        textInput.setFont(new Font("Arial", Font.PLAIN, 20));
        JScrollPane textScroll = new JScrollPane(textInput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Set up the send button
        JButton send = new JButton("Send");
        send.setFont(new Font("Arial", Font.BOLD, 20));
        send.setBackground(Color.decode("#fce1e4"));

        // Set up the bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setPreferredSize(new Dimension(100, 100));
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.setBackground(Color.decode("#E8DFF5"));
        bottomPanel.add(textScroll);
        bottomPanel.add(send);
        add(bottomPanel, BorderLayout.SOUTH);

        // Set up the messages panels
        Color allMessagesColor = Color.decode("#faf3f0");
        messages = new JPanel();
        messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
        messages.setBackground(allMessagesColor);

        messages2 = new JPanel();
        messages2.setLayout(new BoxLayout(messages2, BoxLayout.Y_AXIS));
        messages2.setBackground(allMessagesColor);

        // Set up the scroll pane for messages
        JScrollPane scrollMessages = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add a component listener to auto-scroll to the bottom
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scrollMessages.getVerticalScrollBar().setValue(scrollMessages.getVerticalScrollBar().getMaximum());
            }
        });

        // Add the messages panels to the main panel
        mainPanel.add(messages);
        mainPanel.add(messages2);
        add(scrollMessages);

        // Set the background color of the content pane
        getContentPane().setBackground(Color.PINK);

        // Initially hide the window
        setVisible(false);

        // Add action listener for the send button
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!textInput.getText().trim().isEmpty()) {
                    // Create labels for the sent message
                    JLabel message1 = new JLabel("<html><p style='width:300px'>" + textInput.getText().replace("\n", "<br>") + "</p></html>");
                    JLabel message2 = new JLabel("<html><p style='width:300px'>" + textInput.getText().replace("\n", "<br>") + "</p></html>");

                    // Create space labels for formatting
                    JLabel space = new JLabel(" ");
                    space.setPreferredSize(new Dimension(10, 10));
                    JLabel space2 = new JLabel(" ");
                    space2.setPreferredSize(new Dimension(10, 10));

                    // Style the sent message
                    message1.setOpaque(true);
                    message1.setBackground(Color.decode("#ddedea"));
                    message1.setForeground(Color.black);
                    message1.setFont(new Font("Arial", Font.BOLD, 30));

                    // Style the received message
                    message2.setOpaque(false);
                    message2.setForeground(Color.decode("#faf3f0"));
                    message2.setFont(new Font("Arial", Font.BOLD, 30));

                    // Add the sent message to the messages panel
                    JLabel me = new JLabel("ME:");
                    messages.add(me);
                    messages.add(message1);
                    messages.add(space);
                    messages.revalidate();
                    messages.repaint();

                    // Add the received message to the messages2 panel
                    messages2.add(new JLabel(" "));
                    messages2.add(message2);
                    messages2.add(space2);
                    messages2.revalidate();
                    messages2.repaint();

                    // Send the message to the receiver
                    String newMessage = textInput.getText().replace("\n", "@@@@");
                    client.sendPrivateMessage(receiverName, newMessage);

                    // Clear the text input area
                    textInput.setText("");
                }
            }
        });
    }
}