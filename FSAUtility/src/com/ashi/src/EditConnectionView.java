package com.ashi.src;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A custom JPanel to allow the user to manipulate connections (Specifically,
 * the accepted keywords)
 * 
 * @author Andy
 *
 */
@SuppressWarnings("serial")
public class EditConnectionView extends JPanel {

	public static final int WIDTH = 500; // Width of window to edit connection
	public static final int HEIGHT = 150; // Height of window to edit connection

	Connection connection; // The connection to edit
	DefaultListModel<String> dlm;
	JList<String> listOfTriggers; // Accepted keywords for the connection

	public EditConnectionView(Connection connection) {
		setLayout(new BorderLayout());

		this.connection = connection;

		initList();

		initControls();
	}

	/**
	 * Creates the main interface for the window, consisting of 3 buttons to create,
	 * delete, and remove all triggers from the connection
	 */
	private void initControls() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(3, 1));
		controlPanel.add(createAddTriggerButton());
		controlPanel.add(createDeleteTriggerButton());
		controlPanel.add(createRemoveAllTriggersButton());
		add(controlPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates a button to remove all triggers from a connection
	 * 
	 * @return The button
	 */
	private Component createRemoveAllTriggersButton() {
		JButton removeAllTriggers = new JButton("Remove All Triggers");
		removeAllTriggers.addActionListener(e -> {
			Set<String> triggers = connection.getTriggers();

			// Clears triggers from the list itself
			triggers.clear();

			// Visually update the list
			dlm.removeAllElements();

		});
		return removeAllTriggers;
	}

	/**
	 * Creates a button to remove a single selected trigger from a connection
	 * 
	 * @return The button
	 */
	private Component createDeleteTriggerButton() {
		JButton deleteTrigger = new JButton("Delete Trigger");
		deleteTrigger.addActionListener(e -> {
			int selectedIndex;

			if ((selectedIndex = listOfTriggers.getSelectedIndex()) != -1) {
				// Keyword is selected

				// Get the list of triggers
				Set<String> triggers = connection.getTriggers();

				// Remove the selected trigger from the list
				triggers.remove(listOfTriggers.getSelectedValue());

				// Visually update the list
				dlm.removeElementAt(selectedIndex);

			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No trigger selected");
			}
		});
		return deleteTrigger;
	}

	/**
	 * Creates a button to add a trigger to a connection
	 * 
	 * @return The button
	 */
	private Component createAddTriggerButton() {
		JButton addTrigger = new JButton("Add Trigger");
		addTrigger.addActionListener(e -> {
			// Get the list of triggers
			Set<String> triggers = connection.getTriggers();

			String newTriggerName = JOptionPane.showInputDialog(null, "Enter Trigger Name");

			if (newTriggerName != null) {
				// User clicked on OK
				if (!newTriggerName.equals("")) {
					// Trigger is not empty string
					if (!newTriggerName.contains(" ")) {
						// Trigger doesn't contain strings (Required, because strings are used to
						// delimit accepted strings)

						// Add trigger to list
						triggers.add(newTriggerName);

						// Refresh list GUI
						dlm.addElement(newTriggerName);

					} else {
						JOptionPane.showMessageDialog(null, "ERROR: Trigger cannot contain spaces");
					}

				} else {
					JOptionPane.showMessageDialog(null, "ERROR: Trigger cannot be empty string");
				}
			}
		});
		return addTrigger;
	}

	/**
	 * Creates the visual list for displaying the accepted keywords
	 */
	private void initList() {
		dlm = new DefaultListModel<>();

		listOfTriggers = new JList<>(dlm);

		// Set dimensions
		listOfTriggers.setPreferredSize(new Dimension(WIDTH / 2, HEIGHT));

		// Make scrollable
		JScrollPane jsp = new JScrollPane(listOfTriggers);

		// Get list of accepted strings from connection
		Map<Node, Set<String>> map = connection.getStartNode().getConnections();
		Set<String> acceptedStrings = map.get(connection.getEndNode());

		// Add keywords to list
		for (String trigger : acceptedStrings) {
			dlm.addElement(trigger);
		}

		add(jsp, BorderLayout.WEST);
	}
}
