package com.ashi.src;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				initUI();
			}
		});
	}

	public static void initUI() {

		JFrame f = new JFrame("Swing Hello World");
		f.setSize(WIDTH, HEIGHT);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setLayout(new BorderLayout());
		f.add(gui, BorderLayout.CENTER);

		initMenus(f);
		initInputInterface(f);
	}

	private static void initInputInterface(JFrame f) {
		JPanel interfacePanel = new JPanel();
		interfacePanel.setLayout(new BorderLayout());

		JTextField inputsBox = new JTextField(80);

		JButton testInput = new JButton("Test");
		testInput.addActionListener(e -> {
			if (!inputsBox.getText().equals("")) {
				// TODO

				System.out.println(inputsBox.getText());
				System.out.println("CREASH");
			}
		});

		interfacePanel.add(inputsBox, BorderLayout.WEST);
		interfacePanel.add(testInput, BorderLayout.CENTER);

	}

	private static void initMenus(JFrame f) {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem writeOutState = new JMenuItem("Write out state");
		writeOutState.addActionListener(e -> {
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

		fileMenu.add(writeOutState);

		JMenuItem readInState = new JMenuItem("Read in state");
		readInState.addActionListener(e -> {
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

		fileMenu.add(readInState);

		fileMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(e -> {
			System.exit(0);
		});

		fileMenu.add(exitItem);

		menuBar.add(fileMenu);
		//

		JMenu editMenu = new JMenu("Edit");

		JMenuItem editConnection = new JMenuItem("Edit connection");
		editConnection.addActionListener(e -> {
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
		editMenu.add(editConnection);
		editMenu.addSeparator();

		JMenuItem editNode = new JMenuItem("Edit node");
		editNode.addActionListener(e -> {
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

		editMenu.add(editNode);

		JMenu stateMenu = new JMenu("Change State");

		JMenuItem beginningNode = new JMenuItem("Beginning Node");
		beginningNode.addActionListener(e -> {
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
				} else {
					JOptionPane.showMessageDialog(null, "ERROR: Beginning node already exists");
				}
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}
		});

		JMenuItem terminalNode = new JMenuItem("Terminal Node");
		terminalNode.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				gui.getSelectedNode().setNodeState(NodeState.TERMINAL);

				
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}

		});

		JMenuItem regularNode = new JMenuItem("Regular Node");
		regularNode.addActionListener(e -> {
			if (gui.getSelectedNode() != null) {
				gui.getSelectedNode().setNodeState(NodeState.REGULAR);
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No node selected");
			}
		});

		stateMenu.add(beginningNode);
		stateMenu.add(terminalNode);
		stateMenu.add(regularNode);

		editMenu.add(stateMenu);

		menuBar.add(editMenu);

		f.setJMenuBar(menuBar);
	}
}