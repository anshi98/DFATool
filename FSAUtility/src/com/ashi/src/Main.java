package com.ashi.src;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
	public static final int HEIGHT = 1000; // Height of main JFrame
	public static final int WIDTH = 1000; // Width of main JFrame
	public static final int QUERY_STRING_INPUT_LENGTH = 80; // The length of the query string input window
	public static GUI gui = new GUI(); // The JPanel object to manipulate nodes with
	public static JFrame mainFrame = new JFrame("Automata Simulator"); // The main window

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				initUI();
			}
		});
	}

	/**
	 * Initializes all GUI elements of the application (Menu bar and main
	 * interactive interface (Bar to enter query string with test button and GUI to
	 * create and manipulate nodes))
	 */
	public static void initUI() {

		mainFrame.setSize(WIDTH, HEIGHT);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.add(gui, BorderLayout.CENTER);

		// Request focus for GUI so keyListener works
		gui.requestFocus();

		initMenus();
		initInputInterface();
	}

	/**
	 * Initializes the interface for entering query strings and testing them with
	 * the automata
	 */
	private static void initInputInterface() {
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());

		JTextField inputsBox = new JTextField(QUERY_STRING_INPUT_LENGTH);

		interfacePanel.add(inputsBox, BorderLayout.WEST);
		interfacePanel.add(createTestInputButton(inputsBox), BorderLayout.CENTER);

		mainFrame.add(interfacePanel, BorderLayout.NORTH);
	}

	/**
	 * Creates a "test" button for the interface to test automata with a query
	 * string. Button works by seeing if query string is empty. If not, it'll look
	 * for a node with its state as "beginning". If it finds one, it'll create a new
	 * testing window
	 * 
	 * @param The box for inputing a query string
	 * @return The testing button
	 */
	private static Component createTestInputButton(JTextField inputsBox) {
		JButton toReturn = new JButton("Test");
		toReturn.addActionListener(e -> {
			if (!inputsBox.getText().equals("")) {
				// Query string not empty

				// Check for beginning node to start testing on
				for (Node node : gui.getNodes()) {
					if (node.getNodeState() == NodeState.BEGINNING) {
						createTestingInterface(node, inputsBox);
						return;
					}
				}

				// No beginning loop if you can exit out of the loop without returning
				JOptionPane.showMessageDialog(null, "ERROR: No beginning node");
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: Empty query string");
			}
		});
		return toReturn;
	}

	/**
	 * Creates a testing window with the selected node and tokens from the query
	 * string input box. The code will also temporarily disable the main window
	 * until testing finishes (The testing window closes)
	 * 
	 * @param node      The node to test
	 * @param inputsBox The window containing the query string
	 */
	private static void createTestingInterface(Node node, JTextField inputsBox) {
		// Split the query string into segments
		List<String> tokens = new ArrayList<>(Arrays.asList(inputsBox.getText().split(" ")));

		JFrame testFrame = new JFrame("Test input");
		testFrame.setSize(TestInputView.WIDTH,
				TestInputView.BASE_HEIGHT + TestInputView.HEIGHT_PER_CONNECTION_ITEM * tokens.size());

		// Add a new TestInputView JPanel with the string segment and the node with
		// state BEGINNING to start testing
		testFrame.add(new TestInputView(tokens, node));
		testFrame.setVisible(true);

		// Make it so that the main frame will be reenabled upon the testing window
		// closing
		testFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mainFrame.setEnabled(true);
				gui.setTestNode(null);
			}

		});

		// Disable the main window
		mainFrame.setEnabled(false);

		return;
	}

	/**
	 * Creates a menu bar for the main frame (Contains file menu and edit menu)
	 */
	private static void initMenus() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		mainFrame.setJMenuBar(menuBar);
	}

	/**
	 * Creates the edit menu for the menu bar (Contains option for editing node
	 * connections and nodes themselves)
	 * 
	 * @return The edit menu
	 */
	private static JMenu createEditMenu() {
		JMenu editMenu = new JMenu("Edit");

		editMenu.add(createEditConnectionButton());
		editMenu.addSeparator();
		editMenu.add(createEditNodeButton());

		/* ---SEPARATOR--- */

		JMenu stateMenu = new JMenu("Change State");

		stateMenu.add(createBeginningNodeButton());
		stateMenu.add(createTerminalNodeButton());
		stateMenu.add(createRegularNodeButton());

		editMenu.add(stateMenu);

		return editMenu;
	}

	/**
	 * Creates a button for turning the selected node (If there is one) into a
	 * regular node. Also refreshes the GUI
	 * 
	 * @return Button for turning node into regular node
	 */
	private static JMenuItem createRegularNodeButton() {
		JMenuItem toReturn = new JMenuItem("Regular Node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				// User has selected a node to change
				gui.getSelectedNode().setNodeState(NodeState.REGULAR);

				// Refresh the screen
				gui.repaint();
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}
		});
		return toReturn;
	}

	/**
	 * Creates a button for turning the selected node (If there is one) into a
	 * terminal node. Also refreshes the GUI
	 * 
	 * @return Button for turning node into terminal node
	 */
	private static JMenuItem createTerminalNodeButton() {
		JMenuItem toReturn = new JMenuItem("Terminal Node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				// User has selected a node to change
				gui.getSelectedNode().setNodeState(NodeState.TERMINAL);

				// Refresh the screen
				gui.repaint();
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}

		});
		return toReturn;
	}

	/**
	 * Creates a button for turning the selected node (If there is one) into a
	 * beginning node (Assuming there isn't one already). Also refreshes the GUI
	 * 
	 * @return Button for turning node into beginning node
	 */
	private static JMenuItem createBeginningNodeButton() {
		JMenuItem toReturn = new JMenuItem("Beginning Node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				// User has selected a node to change
				boolean alreadyHasBeginningNode = false;

				for (Node currNode : gui.getNodes()) {
					if (currNode.getNodeState().equals(NodeState.BEGINNING)) {
						// GUI already has one beginning node, so don't add any more by setting this
						// boolean, so the next block won't be called
						alreadyHasBeginningNode = true;
						break;
					}
				}

				if (!alreadyHasBeginningNode) {
					// Assign the BEGINNING state to the node
					gui.getSelectedNode().setNodeState(NodeState.BEGINNING);

					// Refresh the screen
					gui.repaint();
				} else {
					JOptionPane.showMessageDialog(null, "ERROR: Beginning node already exists");
				}
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}
		});
		return toReturn;
	}

	/**
	 * Creates button for editing the name of a node
	 * 
	 * @return The button for editing a node name
	 */
	private static JMenuItem createEditNodeButton() {
		JMenuItem toReturn = new JMenuItem("Edit node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				// User has selected a node to change
				String newName;

				if ((newName = JOptionPane.showInputDialog(null, "Enter new node name")) != null) {
					boolean nameTaken = false;

					for (Node node : gui.getNodes()) {
						if (node.getName().equals(newName)) {
							// Name has already been assigned to another node, so set this boolean to
							// prevent the next block from being called
							nameTaken = true;
							break;
						}
					}

					if (!nameTaken) {
						// Set new name
						gui.getSelectedNode().setName(newName);

						// Refresh the screen
						gui.repaint();
					} else {
						JOptionPane.showMessageDialog(null, "ERROR: Name already taken");
					}
				}
			}
		});
		return toReturn;
	}

	/**
	 * Creates a button that creates a window to edit the selected node connection
	 * 
	 * @return Button to create connection editing window
	 */
	private static JMenuItem createEditConnectionButton() {
		JMenuItem toReturn = new JMenuItem("Edit connection");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedConnection() != null) {
				// User has connection a node to change
				createEditConnectionView();
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No connection selected");
			}
		});
		return toReturn;
	}

	/**
	 * Creates a window to connection editing view
	 */
	private static void createEditConnectionView() {
		JFrame frame = new JFrame("Edit Connection");
		frame.setSize(EditConnectionView.WIDTH, EditConnectionView.HEIGHT);
		// Pass the currently selected connection to the new window
		frame.add(new EditConnectionView(gui.getSelectedConnection()));
		frame.setVisible(true);

		// This adds two functionalities that are called when the user finishes editing
		// the connection. It will check to see whether the user removed all keywords
		// from the connection, and will delete the connection if so. Upon closing the
		// window, the main frame will also be re-enabled
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Check if connection has no more triggers
				Connection selectedConnection = gui.getSelectedConnection();
				if (selectedConnection.getTriggers().isEmpty()) {
					// No more accepted keywords, so remove the connection
					selectedConnection.getStartNode().getConnections().remove(selectedConnection.getEndNode());
					gui.repaint();
				}
				mainFrame.setEnabled(true);
			}

		});

		// Temporarily disable the main frame
		mainFrame.setEnabled(false);
	}

	/**
	 * Creates the file menu for the menu bar. Contains buttons for writing out and
	 * reading in program state, and exiting the program
	 * 
	 * @return The file menu
	 */
	private static JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(createWriteOutButton());
		fileMenu.add(createReadInButton());
		fileMenu.addSeparator();
		fileMenu.add(createExitItemButton());
		return fileMenu;
	}

	/**
	 * Creates button for closing program
	 * 
	 * @return Exit button
	 */
	private static JMenuItem createExitItemButton() {
		JMenuItem toReturn = new JMenuItem("Exit");
		toReturn.addActionListener(e -> {
			System.exit(0);
		});
		return toReturn;
	}

	/**
	 * Creates button to read in program serial. Prompts user for file and then
	 * replaces old GUI object with new read-in GUI object
	 * 
	 * @return Read in button
	 */
	private static JMenuItem createReadInButton() {
		JMenuItem toReturn = new JMenuItem("Read in state");
		toReturn.addActionListener(e -> {
			// Create a new file chooser object
			JFileChooser chooser = new JFileChooser("Select State File");

			// Set default directory to serials folder, so the user doesn't have to
			// painstakingly access it manually
			chooser.setCurrentDirectory(new File("./serials/"));

			// Make it so that only .ser files are selectable
			FileNameExtensionFilter filter = new FileNameExtensionFilter("SER Files", "ser");
			chooser.setFileFilter(filter);

			chooser.showOpenDialog(null);
			if (chooser.getSelectedFile() != null) {
				try {
					FileInputStream fileIn = new FileInputStream(chooser.getSelectedFile());
					ObjectInputStream in = new ObjectInputStream(fileIn);

					GUI newGui = (GUI) in.readObject();
					// Replace variables of old GUI with new GUI
					gui.setNodes(newGui.getNodes());
					gui.setSelectedNode(newGui.getSelectedNode());
					gui.setBeginningNode(newGui.getBeginningNode());
					gui.setSelectedConnection(newGui.getSelectedConnection());
					gui.setNodeIndex(newGui.getNodeIndex());
					gui.setTestNode(newGui.getTestNode());
					gui.repaint();

					in.close();
					fileIn.close();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		return toReturn;
	}

	/**
	 * Creates button to write out GUI state in "serials" folder
	 * 
	 * @return Write out button
	 */
	private static JMenuItem createWriteOutButton() {
		JMenuItem toReturn = new JMenuItem("Write out state");
		toReturn.addActionListener(e -> {
			try {
				String serialName = JOptionPane.showInputDialog(null, "Enter name of file");

				if (serialName != null) {
					// User entered file name (Didn't click cancel)
					FileOutputStream fileOut = new FileOutputStream("./serials/" + serialName + ".ser");
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(gui);
					out.close();
					fileOut.close();

					// Tell the user the file has been written out successfully
					System.out.printf("Serialized data is saved in ./serials/" + serialName + ".ser");
				}
			} catch (IOException i) {
				i.printStackTrace();
			}
		});
		return toReturn;
	}
}