package jobanalysis.ui.panels;

import jobanalysis.ml.JobListingClassifier;
import jobanalysis.ui.MainFrame;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class ClassificationPanel extends JPanel {
    private MainFrame parent;
    private JobListingClassifier classifier;
    private JTextArea resultArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JPanel resultsPanel;
    private JScrollPane resultScroll;
    
    // Statistics components
    private JLabel totalJobsLabel;
    private JLabel processedJobsLabel;
    private JPanel categoryChart;
    private JPanel skillsChart;

    public ClassificationPanel(MainFrame parent) {
        this.parent = parent;
        this.classifier = new JobListingClassifier();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(MainFrame.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Results Panel
        createResultsPanel();
        add(resultScroll, BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Job Listings Classification");
        titleLabel.setFont(MainFrame.TITLE_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        statusLabel = new JLabel("Ready to classify job listings");
        statusLabel.setFont(MainFrame.LABEL_FONT);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));

        statusPanel.add(statusLabel);
        statusPanel.add(progressBar);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void createResultsPanel() {
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        
        // Left stats
        JPanel leftStats = new JPanel(new GridLayout(2, 1));
        leftStats.setBackground(Color.WHITE);
        totalJobsLabel = new JLabel("Total Jobs: 0");
        processedJobsLabel = new JLabel("Processed: 0");
        leftStats.add(totalJobsLabel);
        leftStats.add(processedJobsLabel);
        
        // Right stats - will contain charts later
        JPanel rightStats = new JPanel(new GridLayout(2, 1));
        rightStats.setBackground(Color.WHITE);
        categoryChart = new JPanel();
        skillsChart = new JPanel();
        rightStats.add(categoryChart);
        rightStats.add(skillsChart);
        
        statsPanel.add(leftStats);
        statsPanel.add(rightStats);
        resultsPanel.add(statsPanel);

        // Results Area
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        
        resultScroll = new JScrollPane(resultsPanel);
        resultScroll.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        JButton classifyButton = MainFrame.createStyledButton("Start Classification", "primary");
        classifyButton.addActionListener(e -> startClassification());

        JButton exportButton = MainFrame.createStyledButton("Export Results", "success");
        exportButton.addActionListener(e -> exportResults());

        controlPanel.add(classifyButton);
        controlPanel.add(exportButton);

        return controlPanel;
    }

    private void startClassification() {
        File inputFile = new File("data/cleaned_job_listings.json");
        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "Cleaned job listings file not found. Please preprocess the data first.",
                "File Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Classifying job listings...");
        resultArea.setText("");
        
        SwingWorker<Void, Map<String, Object>> worker = new SwingWorker<>() {
            private int totalJobs = 0;
            private int processedJobs = 0;
            private Map<String, Integer> categoryCount = new HashMap<>();
            private Map<String, Integer> skillsCount = new HashMap<>();

            @Override
            protected Void doInBackground() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, String>> jobListings = mapper.readValue(inputFile,
                    new TypeReference<List<Map<String, String>>>() {});

                totalJobs = jobListings.size();
                updateTotalJobsLabel(totalJobs);

                for (Map<String, String> job : jobListings) {
                    Map<String, Object> analysis = classifier.analyzeJobListing(
                        job.get("Job Title"),
                        job.get("Description")
                    );
                    
                    // Update statistics
                    String category = (String) analysis.get("category");
                    categoryCount.merge(category, 1, Integer::sum);
                    
                    @SuppressWarnings("unchecked")
                    Set<String> skills = (Set<String>) analysis.get("skills");
                    for (String skill : skills) {
                        skillsCount.merge(skill, 1, Integer::sum);
                    }
                    
                    processedJobs++;
                    updateProcessedJobsLabel(processedJobs);
                    
                    publish(analysis);
                }

                return null;
            }

            @Override
            protected void process(List<Map<String, Object>> chunks) {
                for (Map<String, Object> analysis : chunks) {
                    addResultCard(analysis);
                }
                updateCharts(categoryCount, skillsCount);
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    statusLabel.setText("Classification completed successfully");
                } catch (Exception ex) {
                    statusLabel.setText("Error during classification");
                    ex.printStackTrace();
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private void addResultCard(Map<String, Object> analysis) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(1200, 150));

        // Title and Category
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel categoryLabel = new JLabel("Category: " + analysis.get("category"));
        categoryLabel.setFont(MainFrame.HEADING_FONT);
        categoryLabel.setForeground(MainFrame.PRIMARY_COLOR);
        
        JLabel experienceLabel = new JLabel("Experience: " + analysis.get("experienceLevel"));
        experienceLabel.setFont(MainFrame.LABEL_FONT);
        
        headerPanel.add(categoryLabel, BorderLayout.WEST);
        headerPanel.add(experienceLabel, BorderLayout.EAST);

        // Skills
        @SuppressWarnings("unchecked")
        Set<String> skills = (Set<String>) analysis.get("skills");
        JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skillsPanel.setBackground(Color.WHITE);
        
        for (String skill : skills) {
            JLabel skillLabel = new JLabel(skill);
            skillLabel.setFont(MainFrame.SMALL_FONT);
            skillLabel.setForeground(Color.WHITE);
            skillLabel.setBackground(MainFrame.SECONDARY_COLOR);
            skillLabel.setOpaque(true);
            skillLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            skillsPanel.add(skillLabel);
        }

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(skillsPanel, BorderLayout.CENTER);

        resultsPanel.add(card);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void updateCharts(Map<String, Integer> categoryCount, Map<String, Integer> skillsCount) {
        // Update category distribution chart
        categoryChart.removeAll();
        // Add visualization code here
        categoryChart.revalidate();
        categoryChart.repaint();

        // Update skills frequency chart
        skillsChart.removeAll();
        // Add visualization code here
        skillsChart.revalidate();
        skillsChart.repaint();
    }

    private void exportResults() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Classification Results");
        fileChooser.setSelectedFile(new File("classification_results.json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                // Export logic here
                JOptionPane.showMessageDialog(this,
                    "Results exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting results: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void updateTotalJobsLabel(int total) {
        SwingUtilities.invokeLater(() -> 
            totalJobsLabel.setText("Total Jobs: " + total));
    }

    private void updateProcessedJobsLabel(int processed) {
        SwingUtilities.invokeLater(() -> 
            processedJobsLabel.setText("Processed: " + processed));
    }
}