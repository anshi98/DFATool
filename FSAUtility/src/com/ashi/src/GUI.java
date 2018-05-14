package com.ashi.src;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
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

@SuppressWarnings("serial")
public class GUI extends JPanel implements Serializable {

	private static final int CLICKBOX = 4;

	private List<Node> nodes;
	private Node selectedNode;
	private Node beginningNode;
	private Connection selectedConnection;

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
			System.out.println("HELLO");
			if (arg0.getKeyCode() == KeyEvent.VK_DELETE) {
				System.out.println("HELLO");

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
						Line2D.Double currConnection = new Line2D.Double(node.getxPos(), node.getyPos(), adj.getxPos(),
								adj.getyPos());

						Rectangle clickbox = new Rectangle(me.getX() - CLICKBOX, me.getY() - CLICKBOX, CLICKBOX * 2,
								CLICKBOX * 2);

						if (currConnection.intersects(clickbox)) {
							System.out.println(node + " " + adj);
							selectedNode = null;
							beginningNode = null;
							selectedConnection = new Connection(node, adj);
							repaint();
							return;
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
				nodes.add(new Node(me.getX(), me.getY(), Integer.toString(nodes.size()), NodeState.REGULAR));

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

		for (Node node : nodes) {
			g2d.drawOval(node.getxPos() - Node.RADIUS, node.getyPos() - Node.RADIUS, Node.RADIUS * 2, Node.RADIUS * 2);

			drawCenteredString(g, node.getName(), new Rectangle(node.getxPos() - Node.RADIUS,
					(int) (node.getyPos() - (Node.RADIUS * 2.25f)), Node.RADIUS * 2, Node.RADIUS * 2));

			for (Entry<Node, Set<String>> entry : node.getConnections().entrySet()) {
				Node adj = entry.getKey();
				Set<String> triggers = entry.getValue();

				if (selectedConnection != null && selectedConnection.getStartNode().equals(node)
						&& selectedConnection.getEndNode().equals(adj)) {
					g2d.setColor(Color.BLUE);
					drawConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
					g2d.setColor(Color.BLACK);
				} else {
					drawConnection(triggers, g2d, node.getxPos(), node.getyPos(), adj.getxPos(), adj.getyPos());
				}

				System.out.println(node.getConnections());
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

		if (dx < 0) {
			System.out.println(dx);
			AffineTransform text = AffineTransform.getTranslateInstance(x2, y2);
			text.concatenate(AffineTransform.getRotateInstance(angle + Math.PI));
			g.setTransform(text);

		}

		StringBuilder display = new StringBuilder();

		for (String trigger : triggers) {
			display.append(trigger + " ");
		}

		System.out.println(triggers);

		g.drawString(display.toString(), len / 2, 0);
	}
}
