package com.ashi.src;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	public static final int HEIGHT = 1000;
	public static final int WIDTH = 1000;
	public static GUI gui = new GUI();
	public static JFrame f = new JFrame("Automata Simulator");

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
	 * interactive interface (Testing interface and node interface))
	 */
	public static void initUI() {

		f.setSize(WIDTH, HEIGHT);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setLayout(new BorderLayout());
		f.add(gui, BorderLayout.CENTER);

		initMenus();
		initInputInterface();
	}

	/**
	 * Initializes the input options interface for testing the automata
	 */
	private static void initInputInterface() {
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());

		JTextField inputsBox = new JTextField(80);

		interfacePanel.add(inputsBox, BorderLayout.WEST);
		interfacePanel.add(createTestInputButton(inputsBox), BorderLayout.CENTER);

		f.add(interfacePanel, BorderLayout.NORTH);
	}

	private static Component createTestInputButton(JTextField inputsBox) {
		JButton toReturn = new JButton("Test");
		toReturn.addActionListener(e -> {
			if (!inputsBox.getText().equals("")) {

				for (Node node : gui.getNodes()) {
					if (node.getNodeState() == NodeState.BEGINNING) {
						List<String> tokens = new ArrayList<>(Arrays.asList(inputsBox.getText().split(" ")));

						JFrame testFrame = new JFrame("Test input");
						testFrame.setSize(TestInputView.WIDTH, TestInputView.HEIGHT);

						testFrame.add(new TestInputView(tokens, node));
						testFrame.setVisible(true);

						testFrame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent e) {
								f.setEnabled(true);
								gui.setTestNode(null);
							}

						});

						f.setEnabled(false);

						return;
					}
				}

				JOptionPane.showMessageDialog(null, "ERROR: No beginning node");
			}
		});
		return toReturn;
	}

	private static void initMenus() {
		JMenuBar menuBar = new JMenuBar();

		menuBar.add(createFileMenu());

		//

		menuBar.add(createEditMenu());

		f.setJMenuBar(menuBar);
	}

	private static JMenu createEditMenu() {
		JMenu editMenu = new JMenu("Edit");

		editMenu.add(createEditConnectionButton());
		editMenu.addSeparator();
		editMenu.add(createEditNodeButton());

		//

		JMenu stateMenu = new JMenu("Change State");

		stateMenu.add(createBeginningNodeButton());
		stateMenu.add(createTerminalNodeButton());
		stateMenu.add(createRegularNodeButton());

		editMenu.add(stateMenu);

		return editMenu;
	}

	private static JMenuItem createRegularNodeButton() {
		JMenuItem toReturn = new JMenuItem("Regular Node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				gui.getSelectedNode().setNodeState(NodeState.REGULAR);
				gui.repaint();
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}
		});
		return toReturn;
	}

	private static JMenuItem createTerminalNodeButton() {
		JMenuItem toReturn = new JMenuItem("Terminal Node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				gui.getSelectedNode().setNodeState(NodeState.TERMINAL);
				gui.repaint();
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}

		});
		return toReturn;
	}

	private static JMenuItem createBeginningNodeButton() {
		JMenuItem toReturn = new JMenuItem("Beginning Node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				boolean alreadyHasBeginningNode = false;

				for (Node currNode : gui.getNodes()) {
					if (currNode.getNodeState().equals(NodeState.BEGINNING)) {
						alreadyHasBeginningNode = true;
						break;
					}
				}

				if (!alreadyHasBeginningNode) {
					gui.getSelectedNode().setNodeState(NodeState.BEGINNING);
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

	private static JMenuItem createEditNodeButton() {
		JMenuItem toReturn = new JMenuItem("Edit node");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				String newName;

				if ((newName = JOptionPane.showInputDialog(null, "Enter new node name")) != null) {
					boolean nameTaken = false;

					for (Node node : gui.getNodes()) {
						if (node.getName().equals(newName)) {
							nameTaken = true;
							break;
						}
					}

					if (!nameTaken) {
						gui.getSelectedNode().setName(newName);

						gui.repaint();
					} else {
						JOptionPane.showMessageDialog(null, "ERROR: Name already taken");
					}
				}
			}
		});
		return toReturn;
	}

	private static JMenuItem createEditConnectionButton() {
		JMenuItem toReturn = new JMenuItem("Edit connection");
		toReturn.addActionListener(e -> {
			if (gui.getSelectedConnection() != null) {
				JFrame frame = new JFrame("Edit Connection");
				frame.setSize(EditConnectionView.WIDTH, EditConnectionView.HEIGHT);
				frame.add(new EditConnectionView(gui.getSelectedConnection()));
				frame.setVisible(true);

				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						f.setEnabled(true);
					}

				});

				f.setEnabled(false);
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No connection selected");
			}
		});
		return toReturn;
	}

	private static JMenu createFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(createWriteOutButton());
		fileMenu.add(createReadInButton());
		fileMenu.addSeparator();
		fileMenu.add(createExitItemButton());
		return fileMenu;
	}

	private static JMenuItem createExitItemButton() {
		JMenuItem toReturn = new JMenuItem("Exit");
		toReturn.addActionListener(e -> {
			System.exit(0);
		});
		return toReturn;
	}

	private static JMenuItem createReadInButton() {
		JMenuItem toReturn = new JMenuItem("Read in state");
		toReturn.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser("Select State File");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("SER Files", "ser");
			chooser.setFileFilter(filter);
			chooser.showOpenDialog(null);
			if (chooser.getSelectedFile() != null) {
				try {
					FileInputStream fileIn = new FileInputStream(chooser.getSelectedFile());
					ObjectInputStream in = new ObjectInputStream(fileIn);

					GUI newGui = (GUI) in.readObject();

					gui.setNodes(newGui.getNodes());
					gui.setSelectedNode(newGui.getSelectedNode());
					gui.setBeginningNode(newGui.getBeginningNode());

					gui.repaint();

					System.out.println(gui.getNodes());
					in.close();
					fileIn.close();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		return toReturn;
	}

	private static JMenuItem createWriteOutButton() {
		JMenuItem toReturn = new JMenuItem("Write out state");
		toReturn.addActionListener(e -> {
			try {
				String serialName = JOptionPane.showInputDialog(null, "Enter name of file");

				if (serialName != null) {
					FileOutputStream fileOut = new FileOutputStream("./serials/" + serialName + ".ser");
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(gui);
					out.close();
					fileOut.close();
					System.out.printf("Serialized data is saved in ./serials/" + serialName + ".ser");
				}
			} catch (IOException i) {
				i.printStackTrace();
			}
		});
		return toReturn;
	}
}