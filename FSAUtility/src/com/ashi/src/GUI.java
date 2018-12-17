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
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.io.Serializable;
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
	private static final int ARC_HEIGHT = 60; // Two nodes that connect to each other will need to have their connection
												// lines arced so you can distinguish between them

	private List<Node> nodes;
	private Node selectedNode;
	private Node beginningNode; // If you want to create a connection between nodes, the program will see if you
								// selected a initial node that will hold the connection, which is stored in
								// this variable. If not, the program will just select a node like normal
	private Connection selectedConnection;
	private int nodeIndex = 0; // Used to give default names to the nodes based on their index
	private Shape arc = null; // TODO probably remove
	private Node testNode = null; // Used to highlight the node you're currently on in query string testing

	private int screenX, screenY, panelX, panelY;

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public Node getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(Node selectedNode) {
		this.selectedNode = selectedNode;
	}

	public Node getBeginningNode() {
		return beginningNode;
	}

	public void setBeginningNode(Node beginningNode) {
		this.beginningNode = beginningNode;
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
		setFocusable(true);

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
					Iterator<Node> iter = nodes.iterator();
					while (iter.hasNext()) {
						Node currNode = iter.next();
						Map<Node, Set<String>> connections = currNode.getConnections();
						connections.remove(selectedNode);
					}

					nodes.remove(selectedNode);

					selectedNode = null;

				} else if (selectedConnection != null) {
					Node startingNode = selectedConnection.getStartNode();
					Node endingNode = selectedConnection.getEndNode();

					startingNode.getConnections().remove(endingNode);

					selectedConnection = null;
				}

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

	private class CustomMouseListener extends MouseAdapter implements MouseMotionListener {
		public void mousePressed(MouseEvent me) {

			if (me.getButton() == MouseEvent.BUTTON1) {
				for (Node node : nodes) {

					if (node.clickedOn(me.getX(), me.getY())) {
						if (beginningNode != null) {
							String trigger = JOptionPane.showInputDialog(null, "Enter accepted string");

							if (trigger != null) {
								if (!trigger.contains(" ")) {

									beginningNode.addNode(node, trigger);

									selectedNode = null;
									beginningNode = null;
									selectedConnection = null;

									repaint();
									return;
								} else {
									JOptionPane.showMessageDialog(null, "ERROR: Trigger cannot contain spaces");
								}
							}
						} else {
							selectedNode = node;

							screenX = me.getXOnScreen();
							screenY = me.getYOnScreen();
							panelX = me.getX();
							panelY = me.getY();

							beginningNode = null;
							selectedConnection = null;

							repaint();
							return;
						}
					}

					for (Node adj : node.getConnections().keySet()) {
						System.out.println("E");
						Line2D.Double currConnection = null;
						arc = null;
						if (node.equals(adj)) {
							currConnection = new Line2D.Double(node.getxPos(), node.getyPos(), adj.getxPos(),
									adj.getyPos());
						} else {
							System.out.println("ELSE");
							if (node.getConnections().containsKey(adj) && adj.getConnections().containsKey(node)) {

								double dx = adj.getxPos() - node.getxPos(), dy = adj.getyPos() - node.getyPos();
								double angle = Math.atan2(dy, dx);
								int len = (int) Math.sqrt(dx * dx + dy * dy);
								AffineTransform at = AffineTransform.getTranslateInstance(node.getxPos(),
										node.getyPos());
								at.concatenate(AffineTransform.getRotateInstance(angle));

								arc = new Arc2D.Double(0, 0 - ARC_HEIGHT / 2, len, ARC_HEIGHT, 0, 180, Arc2D.CHORD);

								arc = at.createTransformedShape(arc);
							} else {
								currConnection = new Line2D.Double(node.getxPos(),
										node.getyPos() + Node.LOOP_ARROW_OFFSET, node.getxPos(), node.getyPos());
							}

						}

						Rectangle clickbox = new Rectangle(me.getX() - CLICKBOX, me.getY() - CLICKBOX, CLICKBOX * 2,
								CLICKBOX * 2);

						if (arc != null) {
							System.out.println(arc.intersects(clickbox));

							if (arc.intersects(clickbox)) {

								selectedNode = null;
								beginningNode = null;
								selectedConnection = new Connection(node, adj);
								repaint();
								return;
							}
						} else {
							if (currConnection.intersects(clickbox)) {

								selectedNode = null;
								beginningNode = null;
								selectedConnection = new Connection(node, adj);
								repaint();
								return;
							}
						}
					}
				}

				// Clicked on empty space
				selectedNode = null;
				beginningNode = null;
				selectedConnection = null;
				repaint();
			}

			if (me.getButton() == MouseEvent.BUTTON2) {

				for (Node node : nodes) {
					if (node.clickedOn(me.getX(), me.getY())) {
						selectedNode = null;
						beginningNode = node;
						selectedConnection = null;
						repaint();
						return;
					}
				}

				selectedNode = null;
				beginningNode = null;
				selectedConnection = null;

				repaint();
			}

			if (me.getButton() == MouseEvent.BUTTON3) {

				nodes.add(new Node(me.getX(), me.getY(), Integer.toString(nodeIndex++), NodeState.REGULAR));

				selectedNode = null;
				beginningNode = null;
				selectedConnection = null;
				repaint();
			}

		}

		public void mouseDragged(MouseEvent me) {
			if (selectedNode != null) {

				int dx = me.getXOnScreen() - screenX;
				int dy = me.getYOnScreen() - screenY;

				selectedNode.setxPos(panelX + dx);
				selectedNode.setyPos(panelY + dy);

				repaint();
			}
		}

		public void mouseReleased(MouseEvent e) {

		}

	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (arc != null) {
			g2d.draw(arc);
		}

		for (Node node : nodes) {
			g2d.drawOval(node.getxPos() - Node.RADIUS, node.getyPos() - Node.RADIUS, Node.RADIUS * 2, Node.RADIUS * 2);

			if (node.getNodeState() == NodeState.TERMINAL) {
				g2d.drawOval(node.getxPos() - Node.INNER_RADIUS_TERMINAL, node.getyPos() - Node.INNER_RADIUS_TERMINAL,
						Node.INNER_RADIUS_TERMINAL * 2, Node.INNER_RADIUS_TERMINAL * 2);
			}

			if (node.getNodeState() == NodeState.BEGINNING) {
				drawArrow(g2d, node.getxPos() - Node.BEGINNING_ARROW_OFFSET, node.getyPos(), node.getxPos(),
						node.getyPos());
			}

			drawCenteredString(g, node.getName(), new Rectangle(node.getxPos() - Node.RADIUS,
					(int) (node.getyPos() - (Node.RADIUS * 2.25f)), Node.RADIUS * 2, Node.RADIUS * 2));

			for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {
				Node adj = entry.getKey();
				Set<String> triggers = entry.getValue();

				if (selectedConnection != null && selectedConnection.getStartNode().equals(node)
						&& selectedConnection.getEndNode().equals(adj)) {
					g2d.setColor(Color.BLUE);
					if (selectedConnection.getStartNode().equals(selectedConnection.getEndNode())) {
						drawLoopArrow(triggers, g2d, node);
					} else {

						if (node.getConnections().containsKey(adj) && adj.getConnections().containsKey(node)) {
							drawArcConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(),
									adj.getyPos());
						} else {
							drawConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
						}
					}
					g2d.setColor(Color.BLACK);
				} else {
					if (entry.getKey().equals(node)) {
						drawLoopArrow(triggers, g2d, node);
					} else {
						if (node.getConnections().containsKey(adj) && adj.getConnections().containsKey(node)) {
							drawArcConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(),
									adj.getyPos());
						} else {
							drawConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
						}
					}
				}

			}
		}

		if (selectedNode != null) {
			g2d.setColor(Color.BLUE);

			g2d.drawOval(selectedNode.getxPos() - Node.RADIUS, selectedNode.getyPos() - Node.RADIUS, Node.RADIUS * 2,
					Node.RADIUS * 2);
			drawCenteredString(g, selectedNode.getName(), new Rectangle(selectedNode.getxPos() - Node.RADIUS,
					(int) (selectedNode.getyPos() - (Node.RADIUS * 2.25f)), Node.RADIUS * 2, Node.RADIUS * 2));
		}

		if (beginningNode != null) {
			g2d.setColor(Color.GREEN);

			g2d.drawOval(beginningNode.getxPos() - Node.RADIUS, beginningNode.getyPos() - Node.RADIUS, Node.RADIUS * 2,
					Node.RADIUS * 2);
			drawCenteredString(g, beginningNode.getName(), new Rectangle(beginningNode.getxPos() - Node.RADIUS,
					(int) (beginningNode.getyPos() - (Node.RADIUS * 2.25f)), Node.RADIUS * 2, Node.RADIUS * 2));
		}

		if (testNode != null) {
			g2d.setColor(Color.RED);

			g2d.drawOval(testNode.getxPos() - Node.RADIUS, testNode.getyPos() - Node.RADIUS, Node.RADIUS * 2,
					Node.RADIUS * 2);
		}
	}

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

		if (dx < 0) {

			AffineTransform text = AffineTransform.getTranslateInstance(x2, y2);
			text.concatenate(AffineTransform.getRotateInstance(angle + Math.PI));
			g.setTransform(text);

		}

		StringBuilder display = new StringBuilder();

		for (String trigger : triggers) {
			display.append(trigger + " ");
		}

		g.drawString(display.toString(), len / 2, 0);
	}

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

		g.drawString(display.toString(), len / 2, -ARC_HEIGHT);
	}

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

	public void drawLoopArrow(Set<String> triggers, Graphics g2d, Node node) {
		drawConnection(triggers, g2d, node.getxPos(), node.getyPos() + Node.LOOP_ARROW_OFFSET, node.getxPos(),
				node.getyPos());

	}
}
