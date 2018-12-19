package com.ashi.src;

import java.util.Set;

/**
 * An object representing a node-to-node connection
 * 
 * @author Andy
 *
 */
public class Connection {
	private Node startNode;
	private Node endNode;

	public Connection(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}

	/**
	 * Returns the set of words that are accepted for the connection
	 * 
	 * @return The set of accepted keywords
	 */
	public Set<String> getTriggers() {
		Set<String> set = startNode.getConnections().get(endNode);
		return set;
	}

	public Node getStartNode() {
		return startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Node getEndNode() {
		return endNode;
	}

	public void setEndNode(Node endNode) {
		this.endNode = endNode;
	}

	@Override
	public String toString() {
		return "[Start Node: " + startNode + ", End Node: " + endNode + "]";
	}

	/**
	 * Both variables are used for equals
	 */
	@Override
	public boolean equals(Object other) {
		Connection otherConnection = (Connection) other;
		return this.startNode.equals(otherConnection.startNode) && this.endNode.equals(otherConnection.endNode);
	}

	/**
	 * Both variables are used for hashCode
	 */
	@Override
	public int hashCode() {
		return startNode.hashCode() + endNode.hashCode();
	}
}
