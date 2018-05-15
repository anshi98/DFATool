package com.ashi.src;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class Node extends JComponent implements Serializable {
	public static final int RADIUS = 50;
	public static final int INNER_RADIUS_TERMINAL = (int) (RADIUS * 0.80);
	public static final int BEGINNING_ARROW_OFFSET = 100;
	public static final int LOOP_ARROW_OFFSET = 150;

	private int xPos;
	private int yPos;
	private String name;
	private NodeState nodeState;
	private Map<Node, Set<String>> connections;

	public Node(int xPos, int yPos, String name, NodeState nodeState) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.name = name;
		this.nodeState = nodeState;

		connections = new HashMap<>();
	}

	/**
	 * 
	 * @param clickedX
	 * @param clickedY
	 * @return
	 */
	public boolean clickedOn(int clickedX, int clickedY) {
		return Math.pow((xPos - clickedX), 2) + Math.pow((yPos - clickedY), 2) < Math.pow(RADIUS, 2);
	}

	/**
	 * 
	 * @param node
	 */
	public void addNode(Node node, String trigger) {
		if (connections.get(node) == null) {
			connections.put(node, new HashSet<>());
		}

		Set<String> toAddTo = connections.get(node);

		toAddTo.add(trigger);
	}

	public int getxPos() {
		return xPos;
	}

	public void setxPos(int xPos) {
		this.xPos = xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NodeState getNodeState() {
		return nodeState;
	}

	public void setNodeState(NodeState nodeState) {
		this.nodeState = nodeState;
	}

	public Map<Node, Set<String>> getConnections() {
		return connections;
	}

	public void setConnections(Map<Node, Set<String>> connections) {
		this.connections = connections;
	}

	@Override
	public String toString() {
		return xPos + " " + yPos + " " + name + " " + nodeState;
	}

	@Override
	public boolean equals(Object other) {
		Node otherNode = (Node) other;
		return this.name.equals(otherNode.name) && this.nodeState.equals(otherNode.nodeState);
	}

	@Override
	public int hashCode() {
		return name.hashCode() + nodeState.hashCode();
	}
}