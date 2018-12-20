package com.ashi.src;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

import java.io.Serializable;

import java.text.AttributedString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Custom JPanel used to do anything graphical in the program (Working with
 * nodes, connections, etc.)
 * 
 * @author Andy
 *
 */
@SuppressWarnings("serial")
public class GUI extends JPanel implements Serializable {

	private static final int CLICKBOX = 4; // It's impossible to click on a connection line exactly, so the program must
											// see if a connection line is close enough to the click. This is done by
											// creating a box of size CLICKBOX around the cursor and seeing if it
											// intersects a line. This allows for easy connection selection
	private static final int ARC_HEIGHT = 60; // Two nodes that connect to each other will need to have their
												// connection lines arced so you can distinguish between them. This
												// constant determines the height of the arcs
	private static final int NODE_NAME_HEIGHT_OFFSET = 70; // Height to offset name of node by
	private static final int KEYWORDS_LINE_OFFSET = 30; // The amount of space to offset the keywords of a connection
														// by its connection line
	private List<Node> nodes;
	private Node selectedNode;
	private Node connectionStartNode; // If you want to create a connection between nodes, the program will see if you
	// selected a initial node that will hold the connection, which is stored in
	// this variable. If not, the program will just select a node like normal
	private Connection selectedConnection;
	private int nodeIndex = 0; // Used to give default names to the nodes based on their index
	private Node testNode; // Used to highlight the node you're currently on in query string testing

	private int screenX, screenY, panelX, panelY;

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}

	public Node getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
	}

	public Node getConnectionStartNode() {
		return connectionStartNode;
	}

	public void setConnectionStartNode(Node beginningNode) {
		this.connectionStartNode = beginningNode;
	}

	public int getScreenX() {
		return screenX;
	}

	public void setScreenX(int screenX) {
		this.screenX = screenX;
	}

	public int getScreenY() {
		return screenY;
	}

	public void setScreenY(int screenY) {
		this.screenY = screenY;
	}

	public int getPanelX() {
		return panelX;
	}

	public void setPanelX(int panelX) {
		this.panelX = panelX;
	}

	public int getPanelY() {
		return panelY;
	}

	public void setPanelY(int panelY) {
		this.panelY = panelY;
	}

	public Connection getSelectedConnection() {
		return selectedConnection;
	}

	public void setSelectedConnection(Connection selectedConnection) {
		this.selectedConnection = selectedConnection;
	}

	public Node getTestNode() {
		return testNode;
	}

	public void setTestNode(Node testNode) {
		this.testNode = testNode;
	}

	public GUI() {
		// Make the JPanel focusable so the keyListener will work
		setFocusable(true);

		// Add listeners
		addMouseListener(new CustomMouseListener());
		addMouseMotionListener(new CustomMouseListener());
		addKeyListener(new CustomKeyListener());

		nodes = new ArrayList<>();

		selectedNode = null;
	}

	private class CustomKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getKeyCode() == KeyEvent.VK_DELETE) {

				if (selectedNode != null) {
					// User wants to delete node
					removeCurrentNode();
				} else if (selectedConnection != null) {
					// User wants to delete connection
					removeCurrentConnection();
				} else {
					JOptionPane.showMessageDialog(null, "ERROR: No entity selected");
				}

				// Refresh the screen
				repaint();
			}
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
		}

		@Override
		public void keyTyped(KeyEvent arg0) {

		}

	}

	/**
	 * Remove the currently selected connection in the GUI
	 */
	private void removeCurrentConnection() {
		// Get the end nodes and use them to delete the connection
		Node startingNode = selectedConnection.getStartNode();
		Node endingNode = selectedConnection.getEndNode();
		startingNode.getConnections().remove(endingNode);

		// Reset the selectedConnection variable, as the selected connection was just
		// deleted
		selectedConnection = null;
	}

	/**
	 * Remove the currently selected node in the GUI
	 */
	private void removeCurrentNode() {
		// Remove all connections between other nodes and the node you want to delete
		Iterator<Node> iter = nodes.iterator();
		while (iter.hasNext()) {
			Node currNode = iter.next();
			Map<Node, Set<String>> connections = currNode.getConnections();
			connections.remove(selectedNode);
		}

		// Remove the node itself
		nodes.remove(selectedNode);

		// Reset the selectedNode variable, as the node that was just selected has been
		// deleted
		selectedNode = null;
	}

	private class CustomMouseListener extends MouseAdapter implements MouseMotionListener {
		public void mousePressed(MouseEvent me) {

			// Left click
			if (me.getButton() == MouseEvent.BUTTON1) {

				// Check to see if node was clicked
				for (Node node : nodes) {

					if (node.clickedOn(me.getX(), me.getY())) {
						// Node has been clicked on. The program then checks if you want to create
						// a connection, or select another node. If the connectionStart variable is not
						// null, then
						// a connection is to be created
						if (connectionStartNode != null) {
							createConnection(node);
							// Connection has been created, so exit no matter what
							return;
						} else {
							// Not creating a connection so selecting another node
							selectNode(node, me);
							// New node has been selected, so exit no matter what
							return;
						}
					}
				}

				// Node was not clicked, so user possibly clicked on connection
				for (Node node : nodes) {
					for (Node adj : node.getConnections().keySet()) {
						// Adj is the opposite node to all connections the current node has.

						/*
						 * This code works by going through every connection in the GUI, and assigning
						 * currConnection with the actual line object of the connection depending on the
						 * connection type (rounded arc for node pairs that are connected to each other,
						 * a simple line for a one way connection between two nodes, and an inwards
						 * up-pointing line for self-looping connections)
						 */

						Shape currConnection = null;
						if (node.equals(adj)) {
							// This connection is a self-looping connection
							currConnection = createSelfLoopLine(node);
						} else if (node.getConnections().containsKey(adj) && adj.getConnections().containsKey(node)) {
							// This connection contains 2 distinct nodes connected to each other
							currConnection = createRoundedLine(adj, node);
						} else {
							// This connection is a one way connection between 2 distinct nodes
							currConnection = createSimpleLine(adj, node);
						}

						// Check if ***currently assigned*** connection line has been clicked
						if (checkForConnectionClick(me, adj, node, currConnection)) {
							// Connection has been clicked so exit out and prevent variables from being
							// reset
							return;
						}

					}
				}

				// Clicked on empty space so reset variables
				resetVariables();

				// Refresh screen
				repaint();
			}

			// Middle mouse
			if (me.getButton() == MouseEvent.BUTTON2) {
				// Check if beginning node for a connection was clicked
				if (checkConnectionStartNodeSelected(me)) {
					// Connection start node variable has been set, so exit out and prevent
					// variables from being reset
					return;
				}

				// No beginning node selected so restore variables back to default
				resetVariables();

				// Refresh screen
				repaint();
			}

			// Right mouse
			if (me.getButton() == MouseEvent.BUTTON3) {
				// Create a new node
				nodes.add(new Node(me.getX(), me.getY(), Integer.toString(nodeIndex++), NodeState.REGULAR));

				// Reset all variables since a new node was just created
				resetVariables();

				// Refresh screen
				repaint();
			}

		}

		// Only activated if click is down and mouse is moving! Since a button has to be
		// clicked, it's possible that selectedNode has changed to a selected node.
		// Hence, if it's not null, it means you're dragging a node to a new position
		public void mouseDragged(MouseEvent me) {
			if (selectedNode != null) {

				int dx = me.getXOnScreen() - screenX;
				int dy = me.getYOnScreen() - screenY;

				selectedNode.setxPos(panelX + dx);
				selectedNode.setyPos(panelY + dy);

				// Node dragged to new position, so refresh screen
				repaint();
			}
		}

		public void mouseReleased(MouseEvent e) {

		}

	}

	/**
	 * Changes the passed-in node parameter to be the new selected node, along with
	 * assigning some coordinate variables with the given mouse event
	 * 
	 * @param node To assign to selected node variable
	 * @param me   Event object to set screen and panel variables with
	 */
	private void selectNode(Node node, MouseEvent me) {
		selectedNode = node;

		screenX = me.getXOnScreen();
		screenY = me.getYOnScreen();
		panelX = me.getX();
		panelY = me.getY();

		// Reset node variables besides selectedNode
		connectionStartNode = null;
		selectedConnection = null;

		// Refresh screen
		repaint();
	}

	/**
	 * Checks to see whether a node has been selected to be a beginning node
	 * 
	 * @param me Mouse coordinates used to check whether a node has been selected
	 * @return Whether a node has been assigned to be a connection start node for a
	 *         potential connection
	 */
	public boolean checkConnectionStartNodeSelected(MouseEvent me) {
		for (Node node : nodes) {
			if (node.clickedOn(me.getX(), me.getY())) {
				// Node has been selected to be a connection start node
				selectedNode = null;
				connectionStartNode = node;
				selectedConnection = null;
				repaint();
				return true;
			}
		}

		// No node selected
		return false;
	}

	/**
	 * Resets variables based on some clearing-action by the user (Clicking on empty
	 * space)
	 */
	private void resetVariables() {
		selectedNode = null;
		connectionStartNode = null;
		selectedConnection = null;
		testNode = null;
	}

	/**
	 * Checks to see if a connection line Shape object has been clicked. If it has,
	 * change the selected connection variable
	 * 
	 * @param me       MouseEvent to check if the curve has been clicked
	 * @param adj      The adjacent node of the connection
	 * @param currNode The original node of the connection
	 * @return Whether the curve has been clicked
	 */
	public boolean checkForConnectionClick(MouseEvent me, Node adj, Node currNode, Shape currConnection) {
		Rectangle clickbox = new Rectangle(me.getX() - CLICKBOX, me.getY() - CLICKBOX, CLICKBOX * 2, CLICKBOX * 2);

		if (currConnection.intersects(clickbox)) {
			// Connection has been clicked (i.e. clickbox intersects the stored line
			// object), so reset node variables (Since nodes weren't clicked), and change
			// the selected connection now that the program knows what connection has been
			// clicked
			selectedNode = null;
			connectionStartNode = null;
			selectedConnection = new Connection(currNode, adj);

			// Refresh screen
			repaint();
			return true;
		}

		return false;
	}

	/**
	 * Creates a straight line connection Shape object from a one-way connection
	 * between two nodes
	 * 
	 * @param adj  The adjacent node
	 * @param node The original node
	 * @return The connection line shape
	 */
	public Shape createSimpleLine(Node adj, Node node) {
		return new Line2D.Double(node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
	}

	/**
	 * Creates an inwards-pointing arrow Shape object from a self-looping connection
	 * 
	 * @param node The node of the self-connection
	 * @return The connection line shape
	 */
	public Shape createSelfLoopLine(Node node) {
		return new Line2D.Double(node.getxPos(), node.getyPos() + Node.LOOP_ARROW_OFFSET, node.getxPos(),
				node.getyPos());
	}

	/**
	 * Creates a rounded arc for a two-way connection between two nodes
	 * 
	 * @param adj  The adjacent node
	 * @param node The original node
	 * @return The rounded arc shape
	 */
	public Shape createRoundedLine(Node adj, Node currNode) {
		double dx = adj.getxPos() - currNode.getxPos(), dy = adj.getyPos() - currNode.getyPos();
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(currNode.getxPos(), currNode.getyPos());
		at.concatenate(AffineTransform.getRotateInstance(angle));

		Shape currConnection = new Arc2D.Double(0, 0 - ARC_HEIGHT / 2, len, ARC_HEIGHT, 0, 180, Arc2D.CHORD);

		currConnection = at.createTransformedShape(currConnection);

		return currConnection;
	}

	/**
	 * Creates a connection from the selected node with another node with an
	 * accepted string determined by the user
	 * 
	 * @param otherNode The other node for the currently selected node (connection
	 *                  start node) to connect to
	 */
	private void createConnection(Node otherNode) {
		String trigger = JOptionPane.showInputDialog(null, "Enter accepted string");

		if (trigger != null) {
			// User clicked on cancel
			if (!trigger.equals("")) {
				// Trigger is not empty string
				if (!trigger.contains(" ")) {
					// Trigger doesn't contain strings (Required, because strings are used to
					// delimit accepted strings)

					// Add a connection to the current node with the specified trigger to the other
					// node
					connectionStartNode.addNode(otherNode, trigger);

					// Reset variables due to a connection just being created
					resetVariables();

					// Refresh screen
					repaint();
					return;
				} else {
					JOptionPane.showMessageDialog(null, "ERROR: Trigger cannot contain spaces");
				}
			} else {
				JOptionPane.showMessageDialog(null, "ERROR: Trigger cannot be empty string");
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		// Graphics2D is a child of Graphics, so it has all the functionality of
		// Graphics and then some. Therefore, we can call every Graphics function using
		// g2d, along with new functions implemented in Graphics2D
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (Node node : nodes) {
			// Draw the node itself
			drawNode(g2d, node);

			// Draw node name
			drawNodeName(g2d, node);

			// Reset color back to default
			g2d.setColor(Color.BLACK);

			// Draw connections
			drawConnectionsForNode(g2d, node);

			// Reset color back to default
			g2d.setColor(Color.BLACK);
		}

	}

	/**
	 * Draws all connections for a given node
	 * 
	 * @param g2d  The graphics object to draw with
	 * @param node The node to draw the connections of
	 */
	private void drawConnectionsForNode(Graphics2D g2d, Node node) {
		for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {
			Node adj = entry.getKey();
			Set<String> triggers = entry.getValue();

			// Current connection is selected, so draw it in blue
			if (selectedConnection != null && selectedConnection.getStartNode().equals(node)
					&& selectedConnection.getEndNode().equals(adj)) {
				g2d.setColor(Color.BLUE);
				if (selectedConnection.getStartNode().equals(selectedConnection.getEndNode())) {
					// Beginning and end nodes are the same, so draw self loop
					drawLoopArrow(triggers, g2d, node);
				} else if (node.getConnections().containsKey(adj) && adj.getConnections().containsKey(node)) {
					// Beginning and end nodes are connected both ways, so must draw arc
					drawArcConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
				} else {
					// Draw line since it's only a one way connection
					drawConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
				}
			}

			else {
				// Connection not selected
				if (entry.getKey().equals(node)) {
					// Connection has end points as the same nodes, hence connection is self looping
					drawLoopArrow(triggers, g2d, node);
				} else if (node.getConnections().containsKey(adj) && adj.getConnections().containsKey(node)) {
					// Connection contains 2 nodes connected to each other, hence you draw an arc
					drawArcConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
				} else {
					// Connection contains one way connection between nodes, hence a simple arrow
					drawConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
				}
			}
		}
	}

	/**
	 * Draws a node based on its node state
	 * 
	 * @param g2d  The graphics object to draw with
	 * @param node The node to draw
	 */
	private void drawNode(Graphics2D g2d, Node node) {
		switch (node.getNodeState()) {
		case TERMINAL: {
			// Terminal node, so the program must render two circles

			if (selectedNode != null && node.equals(selectedNode)) {
				// Colored blue since selected
				g2d.setColor(Color.BLUE);
				drawTerminalNode(g2d, node);
			} else if (connectionStartNode != null && node.equals(connectionStartNode)) {
				// Colored green since beginning node
				g2d.setColor(Color.GREEN);
				drawTerminalNode(g2d, node);
			} else if (testNode != null && node.equals(testNode)) {
				// Colored red since test node
				g2d.setColor(Color.RED);
				drawTerminalNode(g2d, node);
			} else {
				// Simply draw without color
				drawTerminalNode(g2d, node);
			}
		}
		case BEGINNING: {
			// Beginning node, so the program must render the inwards pointing arrow
			if (selectedNode != null && node.equals(selectedNode)) {
				// Colored blue since selected
				g2d.setColor(Color.BLUE);
				drawBeginningNode(g2d, node);
			} else if (connectionStartNode != null && node.equals(connectionStartNode)) {
				// Colored green since beginning node
				g2d.setColor(Color.GREEN);
				drawBeginningNode(g2d, node);
			} else if (testNode != null && node.equals(testNode)) {
				// Colored red since test node
				g2d.setColor(Color.RED);
				drawBeginningNode(g2d, node);
			} else {
				// Simply draw without color
				drawBeginningNode(g2d, node);
			}
		}
		case REGULAR: {
			// Regular node, so just draw normally
			if (selectedNode != null && node.equals(selectedNode)) {
				// Colored blue since selected
				g2d.setColor(Color.BLUE);
				drawRegularNode(g2d, node);
			} else if (connectionStartNode != null && node.equals(connectionStartNode)) {
				// Colored green since beginning
				g2d.setColor(Color.GREEN);
				drawRegularNode(g2d, node);
			} else if (testNode != null && node.equals(testNode)) {
				// Colored red since test node
				g2d.setColor(Color.RED);
				drawRegularNode(g2d, node);
			} else {
				// Simply draw without color
				drawRegularNode(g2d, node);
			}
		}
		}
	}

	/**
	 * Draws the node name of a given node
	 * 
	 * @param g2d  The graphics object to draw with
	 * @param node The node to display the name of
	 */
	private void drawNodeName(Graphics2D g2d, Node node) {
		drawCenteredString(g2d, node.getName(), new Rectangle(node.getxPos() - Node.RADIUS,
				(int) (node.getyPos() - (Node.RADIUS + NODE_NAME_HEIGHT_OFFSET)), Node.RADIUS * 2, Node.RADIUS * 2));
	}

	/**
	 * Draws regular node with a single circle
	 * 
	 * @param g2d  The graphics object to draw with
	 * @param node The node to draw
	 */
	private void drawRegularNode(Graphics2D g2d, Node node) {
		g2d.drawOval(node.getxPos() - Node.RADIUS, node.getyPos() - Node.RADIUS, Node.RADIUS * 2, Node.RADIUS * 2);

	}

	/**
	 * Draws beginning node with a single circle and an arrow pointing inwards from
	 * the left
	 * 
	 * @param g2d  The graphics object to draw with
	 * @param node The node to draw
	 */
	private void drawBeginningNode(Graphics2D g2d, Node node) {
		drawArrow(g2d, node.getxPos() - Node.BEGINNING_ARROW_OFFSET, node.getyPos(), node.getxPos(), node.getyPos());
		g2d.drawOval(node.getxPos() - Node.RADIUS, node.getyPos() - Node.RADIUS, Node.RADIUS * 2, Node.RADIUS * 2);
	}

	/**
	 * Draws terminal node with a double circles
	 * 
	 * @param g2d  The graphics object to draw with
	 * @param node The node to draw
	 */
	private void drawTerminalNode(Graphics2D g2d, Node node) {
		g2d.drawOval(node.getxPos() - Node.RADIUS, node.getyPos() - Node.RADIUS, Node.RADIUS * 2, Node.RADIUS * 2);
		g2d.drawOval(node.getxPos() - Node.INNER_RADIUS_TERMINAL, node.getyPos() - Node.INNER_RADIUS_TERMINAL,
				Node.INNER_RADIUS_TERMINAL * 2, Node.INNER_RADIUS_TERMINAL * 2);
	}

	/**
	 * Creates a string centered in a rectangle shape
	 * 
	 * @param g    The graphics object to draw with
	 * @param text The string to render
	 * @param rect The rectangle to center the string in
	 */
	public void drawCenteredString(Graphics g, String text, Rectangle rect) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics();
		// Determine the X coordinate for the text
		int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
		// Determine the Y coordinate for the text (note we add the ascent, as in java
		// 2d 0 is top of the screen)
		int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
		// Draw the String
		g.drawString(text, x, y);
	}

	/**
	 * Draws a straight line connection
	 * 
	 * @param triggers The list of accepted strings for the connection
	 * @param g2d      The graphics object to draw with
	 * @param x1       The initial x coordinate of the line
	 * @param y1       The initial y coordinate of the line
	 * @param x2       The ending x coordinate of the line
	 * @param y2       The ending y coordinate of the line
	 */
	public void drawConnection(Set<String> triggers, Graphics g2d, int x1, int y1, int x2, int y2) {

		final int ARR_SIZE = 10;

		Graphics2D g = (Graphics2D) g2d.create();

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		g.drawLine(0, 0, len, 0);

		g.fillPolygon(new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len }, new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 },
				4);

		StringBuilder display = new StringBuilder();

		for (String trigger : triggers) {
			display.append(trigger + " ");
		}

		AttributedString stylized = new AttributedString(display.toString());
		stylized.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, display.toString().length() - 1);

		g.drawString(stylized.getIterator(), len / 2, -KEYWORDS_LINE_OFFSET);
	}

	/**
	 * Like drawConnection, but creates an arced line for nodes that have a two-way
	 * connection
	 * 
	 * @param triggers The list of accepted strings for the connection
	 * @param g2d      The graphics object to draw with
	 * @param x1       The initial x coordinate of the line
	 * @param y1       The initial y coordinate of the line
	 * @param x2       The ending x coordinate of the line
	 * @param y2       The ending y coordinate of the line
	 */
	public void drawArcConnection(Set<String> triggers, Graphics g2d, int x1, int y1, int x2, int y2) {

		final int ARR_SIZE = 10;

		Graphics2D g = (Graphics2D) g2d.create();

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		g.drawArc(0, 0 - ARC_HEIGHT / 2, len, ARC_HEIGHT, 0, 180);

		g.fillPolygon(new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len }, new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 },
				4);

		StringBuilder display = new StringBuilder();

		for (String trigger : triggers) {
			display.append(trigger + " ");
		}

		AttributedString stylized = new AttributedString(display.toString());
		stylized.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, display.toString().length() - 1);

		g.drawString(stylized.getIterator(), len / 2, -ARC_HEIGHT - KEYWORDS_LINE_OFFSET);
	}

	/**
	 * Creates a triangle representing the tip of an arrow for a connection
	 * 
	 * @param g2d The graphics object to draw with
	 * @param x1  The initial x coordinate of the arrow
	 * @param y1  The initial y coordinate of the arrow
	 * @param x2  The ending x coordinate of the arrow
	 * @param y2  The ending y coordinate of the arrow
	 */
	public void drawArrow(Graphics g2d, int x1, int y1, int x2, int y2) {
		final int ARR_SIZE = 9;

		Graphics2D g = (Graphics2D) g2d.create();

		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		g.drawLine(0, 0, len, 0);

		g.fillPolygon(new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len }, new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 },
				4);
	}

	/**
	 * For drawing self-loops for a single node (Consists of an inwards-pointing
	 * arrow from the bottom)
	 * 
	 * @param triggers List of accepted strings to draw
	 * @param g2d      The graphics object to draw with
	 * @param node     The node to draw the connection with
	 */
	public void drawLoopArrow(Set<String> triggers, Graphics g2d, Node node) {
		drawConnection(triggers, g2d, node.getxPos(), node.getyPos() + Node.LOOP_ARROW_OFFSET, node.getxPos(),
				node.getyPos());

	}
}
