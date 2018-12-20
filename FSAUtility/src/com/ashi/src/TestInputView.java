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

/**
 * Window for testing automata with query string
 * 
 * @author Andy
 *
 */
@SuppressWarnings("serial")
public class TestInputView extends JPanel {
	public static final int WIDTH = 300; // Width of testing window
	public static final int HEIGHT_PER_CONNECTION_ITEM = 18; // Height of window will depend on how many query string
																// segments take up the window
	public static final int BASE_HEIGHT = 100; // Base height of the window

	List<String> inputs;
	JList<String> list;
	DefaultListModel<String> dlm;
	Node node;
	JButton downButton;

	public TestInputView(List<String> inputs, Node node) {
		this.inputs = inputs;
		this.node = node;

		setLayout(new BorderLayout());

		initList();
		initTestButton();

		// Set the testNode to the node with node state BEGINNING. There will always be
		// a node with node state BEGINNING, since a TestInputView window cannot be
		// created if there isn't one
		for (Node curr : Main.gui.getNodes()) {
			if (curr.getNodeState() == NodeState.BEGINNING) {
				Main.gui.setTestNode(curr);
				break;
			}
		}

		// To see initial testing node
		Main.gui.repaint();
	}

	/**
	 * Creates a button for advancing to next segment of query string. When clicked,
	 * the program will check if the testing window is at the last segment of the
	 * query string. If it is, there are no more segments to test. It will go
	 * through all connections of the current node, with the corresponding opposite
	 * node, seeing if the connection accepts the next query string segment. If it
	 * does, it'll advance to the node accepting the segment
	 * 
	 * @return The test button
	 */
	private void initTestButton() {
		downButton = new JButton("Test");
		downButton.addActionListener(e -> {
			// Check for self-looping connections first, as they have priority
			if (!checkForSelfLoop()) {
				// If program can't find self loop, carry on with checking rest of the nodes
				if (!checkOtherNodes()) {
					// No connections found accepting the query string segment
					JOptionPane.showMessageDialog(null, "ERROR: No connection found");
				}
			}

		});

		add(downButton, BorderLayout.CENTER);
	}

	/**
	 * Checks to see if there's another node to accept the query segment
	 * 
	 * @return Whether another node accepts the next query string segment
	 */
	private boolean checkOtherNodes() {
		for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {
			// "entry" contains a node connected to the current node, along with all strings
			// that are accepted for that node. This for-loop goes through all these
			// connections

			if (entry.getValue().contains(dlm.getElementAt(list.getSelectedIndex() + 1))) {
				// Found a node that accepts the next segment

				// Change current node to the accepting node
				node = entry.getKey();

				// Update the GUI
				Main.gui.setTestNode(node);
				Main.gui.repaint();

				// Move on to the next segment
				list.setSelectedIndex(list.getSelectedIndex() + 1);

				// See if you're on the final segment, and on a terminal node
				endingCheck();
				return true;
			}

		}

		// Didn't find node in loop
		return false;
	}

	/**
	 * See if the testing ended on a terminal node
	 */
	private void endingCheck() {
		if (list.getSelectedIndex() == inputs.size() - 1) {
			// On last segment

			// Disable button to prevent OoB
			downButton.setEnabled(false);

			if (Main.gui.getTestNode().getNodeState().equals(NodeState.TERMINAL)) {
				// Testing ended on terminal node, and is therefore successful
				JOptionPane.showMessageDialog(null, "Query string successful. Ended on terminal node.");
			} else {
				// Testing did not end on terminal, and is therefore unsuccessful
				JOptionPane.showMessageDialog(null, "Query string unsuccessful. Did not end on terminal node.");
			}
		}
	}

	/**
	 * Checks to see if there's a self-looping connection to be considered first
	 * 
	 * @return Whether a self-looping connection accepting the segment has been
	 *         found
	 */
	private boolean checkForSelfLoop() {
		// Due to the getConnections() being a set, it's possible that if a node has a
		// connection to itself with accepted string X, along with connection to another
		// node with the same accepted string X, it's possible for the connection with
		// the other node to be considered first, which is incorrect behavior, so the
		// program must check first whether there's a self-looping connection to test
		// first
		for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {

			if (node.equals(entry.getKey())
					&& entry.getValue().contains(dlm.getElementAt(list.getSelectedIndex() + 1))) {
				// Found self-looping node with the desired string segment

				// Move on to the next segment
				list.setSelectedIndex(list.getSelectedIndex() + 1);

				// See if you're on the final segment, and on a terminal node
				endingCheck();

				// No need to check for rest of nodes, so exit
				return true;
			}
		}

		// Didn't find node in loop
		return false;
	}

	/**
	 * Creates the list of query string segments to add to the testing window.
	 * Default selected segment is at index 0 ("START")
	 */
	private void initList() {
		dlm = new DefaultListModel<>();
		list = new JList<>(dlm);
		// Make segments non-selectable
		list.setEnabled(false);

		// Add dummy START element
		inputs.add(0, "START");

		// Add segments into the list
		for (String input : inputs) {
			dlm.addElement(input);
		}

		// Default the selected index to 0
		list.setSelectedIndex(0);

		add(list, BorderLayout.NORTH);
	}

}
