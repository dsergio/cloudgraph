import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

import cloudgraph.*;

public class TestPaint extends JFrame implements KeyListener, ActionListener {

	final PaintPanel paintPan;

	CloudGraphListDirected graph;
	List<LocationItem> points;
	private JTextField output;

	JButton searchButton = new JButton("Search");
	JButton showButton = new JButton("Show");
	boolean displayOutput = false;
	JTextArea info = new JTextArea();
	
	JList list;
	
	int searchRange = 60;
	
	public TestPaint(CloudGraphListDirected graph, boolean displayOutput) {
		this.displayOutput = displayOutput;
		this.graph = graph;
		
		setTitle("Title TBD");
		setSize(1200, 800);
		
		
		setLayout(new BorderLayout());

		paintPan = new PaintPanel();
		
		if (displayOutput) {
			showButton.setText("Hide");
		} else {
			showButton.setText("Show");
		}
		add(showButton, BorderLayout.PAGE_START);
		showButton.addActionListener(this);
		add(paintPan, BorderLayout.CENTER);
		
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);
		list.setFixedCellWidth(200);
		
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		
		add(list, BorderLayout.LINE_START);
		String[] data = {""};
		list.setListData(data);
		info.setText(
					"          TBD            "
					
		);
		
		info.setEditable(false);
		add(info, BorderLayout.LINE_END);
		
		output = new JTextField("");
		output.setColumns(100);
		DefaultCaret caret = (DefaultCaret) output.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); // JTextArea always set focus on the last message appended.
		add(output, BorderLayout.PAGE_END);

		setVisible(true);
		output.addKeyListener(this); // Adds the specified key listener to receive key events from this component.
		

		Component mouseClick = new MyComponent();
		addMouseListener((MouseListener) mouseClick);
		

		setVisible(true);

	}
	
	public TestPaint(CloudGraphListDirected graph) {
		this(graph, false);
	}


	public class MyComponent extends JComponent implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}
		

		

	}

	class PaintPanel extends JPanel {

		private int x, y;
		private Color color = Color.RED;
		

		public PaintPanel() {
			setBackground(Color.LIGHT_GRAY);
		}

		public void setPoints(List<LocationItem> pointsToAdd) {
			points = pointsToAdd;
		}

		public void addPoint(LocationItem item) {
			points.add(item);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			color = Color.BLACK;
			
			Graphics2D drawImage = (Graphics2D) g;
			
			
			drawImage.drawLine(0, 10, paintPan.getWidth() - 50, 10);
			drawImage.drawLine(10, 0, 10, paintPan.getHeight() - 50);
			for (int i = 50; i < paintPan.getWidth() - 50; i += 50) {
				drawImage.drawLine(i, 5, i, 15);
				drawImage.drawString("" + i, i, 20);
				
				
				drawImage.setColor(Color.GRAY);
				Stroke existing = drawImage.getStroke();
				Stroke dashed = new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
				drawImage.setStroke(dashed);
				drawImage.drawLine(i, 25, i, paintPan.getHeight() - 50);
				drawImage.setColor(Color.BLACK);
				drawImage.setStroke(existing);
			}
			for (int i = 50; i < paintPan.getHeight() - 50; i += 50) {
				drawImage.drawLine(5, i, 15, i);
				drawImage.drawString("" + i, 20, i);
				
				drawImage.setColor(Color.GRAY);
				Stroke existing = drawImage.getStroke();
				Stroke dashed = new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
				drawImage.setStroke(dashed);
				drawImage.drawLine(25, i, paintPan.getWidth() - 50, i);
				drawImage.setColor(Color.BLACK);
				drawImage.setStroke(existing);
			}
			
			if (displayOutput) {
				for (LocationItem item : graph.getPoints()) {
					int x = item.getX();
					int y = item.getY();
					drawImage = (Graphics2D) g;
					if (color != null) {
						drawImage.setColor(color);
						drawImage.drawOval(x, y, 5, 5);
						drawImage.drawString("(" + x + ", " + y + ")", x, y);
						drawImage.drawString(item.getType(), x, y + 20);
					}
				}
				
				for (Edge e : graph.getEdges()) {
					int x1 = e.v1().getX();
					int y1 = e.v1().getY();
					
					int x2 = e.v2().getX();
					int y2 = e.v2().getY();
					drawImage = (Graphics2D) g;
					
					if (color != null) {
						drawImage.setColor(color);
						drawImage.drawLine(x1, y1, x2, y2);
						drawImage.drawString("" + e.weight(), (x1 + x2) / 2, (y1 + y2) / 2);
						
					}
				}
			}
			color = Color.RED;
		}

		public void updateGraphics(int x, int y) throws IOException {
			color = Color.RED;
			
			graph.setVertex(x, y);
			
			repaint();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (displayOutput) {
			displayOutput = false;
			showButton.setText("Show");
		} else {
			displayOutput = true;
			showButton.setText("Hide");
		}
		repaint();
	}
}
