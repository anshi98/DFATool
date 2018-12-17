package com.ashi.src;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class TestInputView extends JPanel {
	public static final int WIDTH = 300; // Width of testing window
	public static final int HEIGHT_PER_CONNECTION_ITEM = 18; // Height of window will depend on how long the query
																// string is
	public static final int BASE_HEIGHT = 100; // Base height of the window

	List<String> inputs;
	JList<String> list;
	DefaultListModel<String> dlm;
	Node node;

	public TestInputView(List<String> inputs, Node node) {
		this.inputs = inputs;
		this.node = node;

		setLayout(new BorderLayout());

		initList();

		initControls();
	}

	/**
	 * Creates control panel which contains button for testing automata with query
	 * string
	 */
	private void initControls() {
		JPanel controls = new JPanel();

		controls.add(initTestButton());

		add(controls, BorderLayout.CENTER);
	}

	/**
	 * Creates a button for advancing to next segment of query string. When clicked,
	 * the program will check if testing window is at the last segment of the query
	 * string. If it is, there's no more segments to test. It will go through all
	 * connections of the current node, with the corresponding opposite node, seeing
	 * if the any connection contains the wanted query string segment. If it does,
	 * it'll advance to the node accepting the segment
	 * 
	 * @return The test button
	 */
	private JButton initTestButton() {
		JButton downButton = new JButton("Test");
		downButton.addActionListener(e -> {

			for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {
				// "entry" contains a node connected to the current node, along with all strings
				// that are accepted for that node. This for loop goes through all these
				// connections
				System.out.println(entry);
				if (list.getSelectedIndex() != inputs.size() - 1
						&& entry.getValue().contains(dlm.getElementAt(list.getSelectedIndex() + 1))) {
					// If not at the end of the query string and found a connection accepting the
					// wanted query string segment, change the current node to the node accepting
					// the segment
					node = entry.getKey();

					Main.gui.setTestNode(node);
					Main.gui.repaint();
					list.setSelectedIndex(list.getSelectedIndex() + 1);
					return;
				}
			}

			JOptionPane.showMessageDialog(null, "ERROR: No connection found");

		});

		return downButton;
	}

	/**
	 * Creates the list of query string segments to add to the testing window.
	 * Default selected segment is at index 0 ("START")
	 */
	private void initList() {
		dlm = new DefaultListModel<>();

		list = new JList<>(dlm);
		list.setEnabled(false);
		inputs.add(0, "START");

		for (String input : inputs) {
			dlm.addElement(input);
		}

		list.setSelectedIndex(0);

		add(list, BorderLayout.NORTH);

		Main.gui.setTestNode(node);
	}

}
