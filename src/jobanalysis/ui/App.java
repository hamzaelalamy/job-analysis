package jobanalysis.ui;

import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
	public static void main(String[] args) {
	    try {
	        // Set System Look and Feel
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	        
	        // Create the data and model directories if they don't exist
	        File dataDir = new File("data");
	        if (!dataDir.exists()) {
	            dataDir.mkdir();
	        }
	        
	        File modelDir = new File("data/job_models");
	        if (!modelDir.exists()) {
	            modelDir.mkdirs();
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    SwingUtilities.invokeLater(() -> {
	        MainFrame frame = new MainFrame();
	        frame.setVisible(true);
	    });
	}
}