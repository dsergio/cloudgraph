import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import cloudgraph.*;

public class Tester {

	public static void main(String[] args) {
		
		CloudGraphListDirected graph1 = new CloudGraphListDirected("graph1");
		
		boolean insertGui = true;
//		insertGui = false; // toggle this to either display GUI or construct data structure
		
		TestPaint testPaint = null;
		
		if (insertGui) {
			
			testPaint = new TestPaint(graph1, true);
			testPaint.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			
			
		} else {
		
			LocationItem v1 = graph1.setVertex(300, 400);
			LocationItem v2 = graph1.setVertex(100, 200);
			LocationItem v3 = graph1.setVertex(600, 500);
			LocationItem v4 = graph1.setVertex(300, 300);
			LocationItem v5 = graph1.setVertex(600, 200);
			LocationItem v6 = graph1.setVertex(600, 100);
			
			graph1.SetEdgeDirected(v1, v2, 5);
			graph1.SetEdgeDirected(v1, v3, 15);
			graph1.SetEdgeDirected(v3, v4, 25);
			graph1.SetEdgeDirected(v5, v2, 100);
			graph1.SetEdgeDirected(v6, v4, 60);
			
		}
		
	}
	
}
