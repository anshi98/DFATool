package com.ashi.src;

import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

/**
 * Object used to represent a node in the automata. Purely informational, as the
 * GUI is the one that draws the nodes based on the information given by the
 * node (position, radius, name, etc.)
 * 
 * @author Andy
 *
 */
@SuppressWarnings("serial")
public class Node extends JComponent implements Serializable {
	public static final int RADIUS = 50;
	public static final int INNER_RADIUS_TERMINAL = (int) (RADIUS * 0.80); // Terminal nodes have an inner circle. This
																			// constant determines the radius of the
																			// inner circle
	public static final int BEGINNING_ARROW_OFFSET = 100; // Beginning nodes are represented with an arrow pointing
															// inwards to the center from the left. This constant
															// determines the
															// length of the arrow
	public static final int LOOP_ARROW_OFFSET = 150; // If a node loops back on itself upon a certain query string, this
														// will be indicated by an inwards-pointing arrow from the
														// bottom containing the accepted string. This constant
														// determines the length of the arrow

	private int xPos;
	private int yPos;
	private String name;
	private NodeState nodeState;
	private Map<Node, Set<String>> connections; // The node variable contains the opposite node being referred to in the
												// connection

	public Node(int xPos, int yPos, String name, NodeState nodeState) {
		this.xPos = xPos;
		this.yPos = yPos;
		this.name = name;
		this.nodeState = nodeState;

		connections = new HashMap<>();
	}

	/**
	 * Sees if node has been clicked based on given x and y mouse coordinates during
	 * click
	 * 
	 * @param x coordinate of mouse during click
	 * @param y coordinate of mouse during click
	 * @return Whether the node has been clicked
	 */
	public boolean clickedOn(int clickedX, int clickedY) {
		return Math.pow((xPos - clickedX), 2) + Math.pow((yPos - clickedY), 2) < Math.pow(RADIUS, 2);
	}

	/**
	 * Creates a connection to another node
	 * 
	 * @param Opposite node
	 * @param Accepted keyword (Trigger)
	 */
	public void addNode(Node node, String trigger) {
		if (connections.get(node) == null) {
			// There have been no connections established between these nodes yet, so create
			// one
			connections.put(node, new HashSet<>());
		}

		// Remember that "connections" is an object that every node has. It maps a set
		// of all
		// accepted query strings from the current node to the opposite node

		// toAddTo are the accepted keywords to get from "this" to "node"
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

	/*
	 * Connections variable is not printed due to the possibility of a stack
	 * overflow if automata contains nodes the connect to each other in a closed
	 * loop
	 */
	@Override
	public String toString() {
		return xPos + " " + yPos + " " + name + " " + nodeState;
	}

	/**
	 * Uses only name variable
	 */
	@Override
	public boolean equals(Object other) {
		Node otherNode = (Node) other;
		return this.name.equals(otherNode.name);
	}

	/**
	 * Uses only name variable
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}