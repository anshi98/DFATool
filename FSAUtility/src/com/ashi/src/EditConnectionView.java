package com.ashi.src;

import java.awt.BorderLayout;
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

@SuppressWarnings("serial")
public class EditConnectionView extends JPanel {

	public static final int WIDTH = 500;
	public static final int HEIGHT = 150;

	Connection connection;
	DefaultListModel<String> dlm;
	JList<String> listOfTriggers;

	public EditConnectionView(Connection connection) {
		setLayout(new BorderLayout());

		this.connection = connection;

		initList();

		initControls();
	}

	private void initControls() {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(3, 1));

		JButton deleteTrigger = new JButton("Delete Trigger");
		deleteTrigger.addActionListener(e -> {
			int selectedIndex;

			if ((selectedIndex = listOfTriggers.getSelectedIndex()) != -1) {
				Set<String> triggers = connection.getTriggers();
				System.out.println("BLE" + triggers);
				triggers.remove(listOfTriggers.getSelectedValue());

				System.out.println(triggers);
				dlm.removeElementAt(selectedIndex);
				Main.gui.repaint();
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: No trigger selected");
			}
		});

		JButton addTrigger = new JButton("Add Trigger");
		addTrigger.addActionListener(e -> {
			Set<String> triggers = connection.getTriggers();

			String newTriggerName;
			if ((newTriggerName = JOptionPane.showInputDialog(null, "Enter Trigger Name")) != null) {
				triggers.add(newTriggerName);

				dlm.addElement(newTriggerName);

				Main.gui.repaint();
			}
		});

		JButton removeAllTriggers = new JButton("Remove All Triggers");
		removeAllTriggers.addActionListener(e -> {
			Set<String> triggers = connection.getTriggers();

			triggers.clear();

			dlm.removeAllElements();

			Main.gui.repaint();
		});

		controlPanel.add(addTrigger);

		controlPanel.add(deleteTrigger);

		controlPanel.add(removeAllTriggers);
		add(controlPanel, BorderLayout.CENTER);
	}

	private void initList() {

		dlm = new DefaultListModel<>();

		listOfTriggers = new JList<>(dlm);

		listOfTriggers.setPreferredSize(new Dimension(WIDTH / 2, HEIGHT));

		JScrollPane jsp = new JScrollPane(listOfTriggers);

		Map<Node, Set<String>> map = connection.getStartNode().getConnections();

		Set<String> connections = map.get(connection.getEndNode());

		for (String trigger : connections) {
			dlm.addElement(trigger);
		}

		add(jsp, BorderLayout.WEST);
	}
}
