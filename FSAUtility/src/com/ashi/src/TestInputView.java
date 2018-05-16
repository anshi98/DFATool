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

public class TestInputView extends JPanel {
	public static final int WIDTH = 300;
	public static final int HEIGHT = 100;

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

	private void initControls() {
		JPanel controls = new JPanel();

		JButton downButton = new JButton("↓");
		downButton.addActionListener(e -> {

			for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {
				System.out.println(dlm.getElementAt(list.getSelectedIndex() + 1));
				if (list.getSelectedIndex() != inputs.size() - 1
						&& entry.getValue().contains(dlm.getElementAt(list.getSelectedIndex() + 1))) {

					node = entry.getKey();
					Main.gui.setTestNode(node);
					Main.gui.repaint();
					list.setSelectedIndex(list.getSelectedIndex() + 1);
					return;
				}
			}

			JOptionPane.showMessageDialog(null, "ERROR: No connection found");

		});

		controls.add(downButton);

		add(controls, BorderLayout.CENTER);
	}

	private void initList() {
		dlm = new DefaultListModel<>();

		list = new JList<>(dlm);

		inputs.add(0, "START");

		for (String input : inputs) {
			dlm.addElement(input);
		}

		list.setSelectedIndex(0);

		add(list, BorderLayout.NORTH);

		Main.gui.setTestNode(node);
	}

}
