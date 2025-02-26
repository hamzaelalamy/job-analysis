package jobanalysis.ui.panels;

import jobanalysis.ml.JobListingClassifier;
import jobanalysis.models.JobListing;
import jobanalysis.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
    private JPanel categoryChartPanel;
    private JPanel skillsChartPanel;
    
    // Charts data
    private Map<String, Integer> categoryCount;
    private Map<String, Integer> skillsCount;

    public ClassificationPanel(MainFrame parent) {
        this.parent = parent;
        this.classifier = new JobListingClassifier();
        this.categoryCount = new HashMap<>();
        this.skillsCount = new HashMap<>();
        
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
        JLabel titleLabel = new JLabel("Job Listings NLP Analysis");
        titleLabel.setFont(MainFrame.TITLE_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        statusLabel = new JLabel("Ready to analyze job listings");
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
        JPanel leftStats = MainFrame.createShadowPanel();
        leftStats.setLayout(new GridLayout(3, 1));
        
        totalJobsLabel = new JLabel("Total Jobs: 0");
        totalJobsLabel.setFont(MainFrame.HEADING_FONT);
        
        processedJobsLabel = new JLabel("Processed: 0");
        processedJobsLabel.setFont(MainFrame.HEADING_FONT);
        
        JLabel nlpLabel = new JLabel("NLP Analysis Results:");
        nlpLabel.setFont(MainFrame.HEADING_FONT.deriveFont(Font.BOLD));
        nlpLabel.setForeground(MainFrame.PRIMARY_COLOR);
        
        leftStats.add(nlpLabel);
        leftStats.add(totalJobsLabel);
        leftStats.add(processedJobsLabel);
        
        // Right stats - Charts
        JPanel rightStats = new JPanel(new GridLayout(2, 1, 0, 10));
        rightStats.setBackground(Color.WHITE);
        
        categoryChartPanel = MainFrame.createShadowPanel();
        categoryChartPanel.setLayout(new BorderLayout());
        JLabel categoryLabel = new JLabel("Category Distribution");
        categoryLabel.setFont(MainFrame.HEADING_FONT);
        categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        categoryChartPanel.add(categoryLabel, BorderLayout.NORTH);
        
        skillsChartPanel = MainFrame.createShadowPanel();
        skillsChartPanel.setLayout(new BorderLayout());
        JLabel skillsLabel = new JLabel("Top Skills");
        skillsLabel.setFont(MainFrame.HEADING_FONT);
        skillsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        skillsChartPanel.add(skillsLabel, BorderLayout.NORTH);
        
        rightStats.add(categoryChartPanel);
        rightStats.add(skillsChartPanel);
        
        statsPanel.add(leftStats);
        statsPanel.add(rightStats);
        
        resultsPanel.add(statsPanel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Results Area Heading
        JLabel resultsHeading = new JLabel("Analysis Results:");
        resultsHeading.setFont(MainFrame.HEADING_FONT);
        resultsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsPanel.add(resultsHeading);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        resultScroll = new JScrollPane(resultsPanel);
        resultScroll.setBorder(BorderFactory.createEmptyBorder());
        resultScroll.getVerticalScrollBar().setUnitIncrement(16);
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        JButton analyzeButton = MainFrame.createStyledButton("Analyze Job Listings", "primary");
        analyzeButton.setPreferredSize(new Dimension(200, 40));
        analyzeButton.addActionListener(e -> startNLPAnalysis());

        JButton backButton = MainFrame.createStyledButton("Back to Upload", "outline");
        backButton.setPreferredSize(new Dimension(200, 40));
        backButton.addActionListener(e -> parent.showFileUpload());

        JButton exportButton = MainFrame.createStyledButton("Export Results", "success");
        exportButton.setPreferredSize(new Dimension(200, 40));
        exportButton.addActionListener(e -> exportResults());

        controlPanel.add(backButton);
        controlPanel.add(analyzeButton);
        controlPanel.add(exportButton);

        return controlPanel;
    }

    private void startNLPAnalysis() {
        File inputFile = new File("data/cleaned_job_listings.json");
        if (!inputFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "Cleaned job listings file not found at: " + inputFile.getAbsolutePath() + 
                "\nPlease ensure the file exists or process raw data first.",
                "File Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Analyzing job listings...");
        
        // Clear previous results
        clearResultsArea();
        categoryCount.clear();
        skillsCount.clear();
        
        SwingWorker<Void, Map<String, Object>> worker = new SwingWorker<>() {
            private int totalJobs = 0;
            private int processedJobs = 0;

            @Override
            protected Void doInBackground() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                // Read as List of Maps instead of JobListing objects
                List<Map<String, Object>> jobListings = mapper.readValue(inputFile,
                    new TypeReference<List<Map<String, Object>>>() {});

                totalJobs = jobListings.size();
                updateTotalJobsLabel(totalJobs);

                for (Map<String, Object> jobData : jobListings) {
                    // Get title and description (check for both upper and lowercase)
                    String title = getStringValue(jobData, "title", "Title", "Job Title");
                    String description = getStringValue(jobData, "description", "Description");
                    
                    // Skip if title or description is missing
                    if (title == null || description == null) {
                        continue;
                    }
                    
                    Map<String, Object> analysis = classifier.analyzeJobListing(title, description);
                    
                    // Add analysis to job data
                    jobData.put("analysisData", analysis);
                    
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
                    
                    publish(jobData);
                    
                    // Slow down to avoid UI freezing
                    Thread.sleep(10);
                }

                return null;
            }

            @Override
            protected void process(List<Map<String, Object>> chunks) {
                for (Map<String, Object> jobData : chunks) {
                    addResultCard(jobData);
                    updateCharts();
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    statusLabel.setText("Analysis completed successfully. Processed " + processedJobs + " job listings.");
                } catch (Exception ex) {
                    statusLabel.setText("Error during analysis");
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ClassificationPanel.this,
                        "Error analyzing job listings: " + ex.getMessage(),
                        "Analysis Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    // Helper method to get string values from map with multiple possible keys
    private String getStringValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            if (data.containsKey(key) && data.get(key) instanceof String) {
                return (String) data.get(key);
            }
        }
        return null;
    }

    // Update this method to work with Map instead of JobListing
    private void addResultCard(Map<String, Object> jobData) {
        @SuppressWarnings("unchecked")
        Map<String, Object> analysis = (Map<String, Object>) jobData.get("analysisData");
        if (analysis == null) return;
        
        JPanel card = MainFrame.createShadowPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(1200, 180));
        card.setPreferredSize(new Dimension(1200, 170));

        // Title and Category
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        String title = getStringValue(jobData, "title", "Title", "Job Title");
        JLabel titleLabel = new JLabel(title != null ? title : "No Title");
        titleLabel.setFont(MainFrame.HEADING_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);
        
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        metaPanel.setBackground(Color.WHITE);
        
        JLabel categoryLabel = createPillLabel("Category: " + analysis.get("category"), MainFrame.BUTTON_COLOR);
        JLabel experienceLabel = createPillLabel("Experience: " + analysis.get("experienceLevel"), MainFrame.SECONDARY_COLOR);
        
        metaPanel.add(categoryLabel);
        metaPanel.add(experienceLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(metaPanel, BorderLayout.EAST);

        // Skills Panel
        JPanel skillsPanel = new JPanel();
        skillsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        skillsPanel.setBackground(Color.WHITE);
        
        JLabel skillsTitle = new JLabel("Skills Identified: ");
        skillsTitle.setFont(MainFrame.LABEL_FONT.deriveFont(Font.BOLD));
        skillsPanel.add(skillsTitle);
        
        @SuppressWarnings("unchecked")
        Set<String> skills = (Set<String>) analysis.get("skills");
        int skillCount = 0;
        for (String skill : skills) {
            if (skillCount++ > 12) {  // Limit to display only top 12 skills
                JLabel moreLabel = createPillLabel("+" + (skills.size() - 12) + " more", MainFrame.WARNING_COLOR);
                skillsPanel.add(moreLabel);
                break;
            }
            JLabel skillLabel = createPillLabel(skill, MainFrame.SUCCESS_COLOR);
            skillsPanel.add(skillLabel);
        }

        // Company info if available
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        String company = getStringValue(jobData, "company", "Company");
        if (company != null && !company.isEmpty()) {
            JLabel companyLabel = new JLabel("Company: " + company);
            companyLabel.setFont(MainFrame.LABEL_FONT);
            infoPanel.add(companyLabel, BorderLayout.WEST);
        }
        
        // Assemble the card
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(skillsPanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        // Add the card to the results panel
        resultsPanel.add(card);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    private void clearResultsArea() {
        // Remove all result cards while keeping stats panels
        while (resultsPanel.getComponentCount() > 2) {
            resultsPanel.remove(resultsPanel.getComponentCount() - 1);
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void addResultCard(JobListing job) {
        Map<String, Object> analysis = job.getAnalysisData();
        if (analysis == null) return;
        
        JPanel card = MainFrame.createShadowPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setMaximumSize(new Dimension(1200, 180));
        card.setPreferredSize(new Dimension(1200, 170));

        // Title and Category
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(MainFrame.HEADING_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);
        
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        metaPanel.setBackground(Color.WHITE);
        
        JLabel categoryLabel = createPillLabel("Category: " + analysis.get("category"), MainFrame.BUTTON_COLOR);
        JLabel experienceLabel = createPillLabel("Experience: " + analysis.get("experienceLevel"), MainFrame.SECONDARY_COLOR);
        
        metaPanel.add(categoryLabel);
        metaPanel.add(experienceLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(metaPanel, BorderLayout.EAST);

        // Skills Panel
        JPanel skillsPanel = new JPanel();
        skillsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        skillsPanel.setBackground(Color.WHITE);
        
        JLabel skillsTitle = new JLabel("Skills Identified: ");
        skillsTitle.setFont(MainFrame.LABEL_FONT.deriveFont(Font.BOLD));
        skillsPanel.add(skillsTitle);
        
        @SuppressWarnings("unchecked")
        Set<String> skills = (Set<String>) analysis.get("skills");
        int skillCount = 0;
        for (String skill : skills) {
            if (skillCount++ > 12) {  // Limit to display only top 12 skills
                JLabel moreLabel = createPillLabel("+" + (skills.size() - 12) + " more", MainFrame.WARNING_COLOR);
                skillsPanel.add(moreLabel);
                break;
            }
            JLabel skillLabel = createPillLabel(skill, MainFrame.SUCCESS_COLOR);
            skillsPanel.add(skillLabel);
        }

        // Company info if available
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        if (job.getCompany() != null && !job.getCompany().isEmpty()) {
            JLabel companyLabel = new JLabel("Company: " + job.getCompany());
            companyLabel.setFont(MainFrame.LABEL_FONT);
            infoPanel.add(companyLabel, BorderLayout.WEST);
        }
        
        // Assemble the card
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(skillsPanel, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);

        // Add the card to the results panel
        resultsPanel.add(card);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JLabel createPillLabel(String text, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setFont(MainFrame.SMALL_FONT);
        label.setForeground(Color.WHITE);
        label.setBackground(bgColor);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }

    private void updateCharts() {
        // Update Category Chart
        categoryChartPanel.removeAll();
        JLabel categoryTitle = new JLabel("Category Distribution");
        categoryTitle.setFont(MainFrame.HEADING_FONT);
        categoryTitle.setHorizontalAlignment(SwingConstants.CENTER);
        categoryChartPanel.add(categoryTitle, BorderLayout.NORTH);
        
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBackground(Color.WHITE);
        
        // Sort categories by count
        List<Map.Entry<String, Integer>> sortedCategories = new ArrayList<>(categoryCount.entrySet());
        sortedCategories.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        
        for (Map.Entry<String, Integer> entry : sortedCategories) {
            JPanel categoryBar = createCategoryBar(entry.getKey(), entry.getValue());
            categoriesPanel.add(categoryBar);
            categoriesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        JScrollPane categoryScroll = new JScrollPane(categoriesPanel);
        categoryScroll.setBorder(null);
        categoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        categoryChartPanel.add(categoryScroll, BorderLayout.CENTER);
        
        // Update Skills Chart
        skillsChartPanel.removeAll();
        JLabel skillsTitle = new JLabel("Top 10 Skills");
        skillsTitle.setFont(MainFrame.HEADING_FONT);
        skillsTitle.setHorizontalAlignment(SwingConstants.CENTER);
        skillsChartPanel.add(skillsTitle, BorderLayout.NORTH);
        
        JPanel skillsPanel = new JPanel();
        skillsPanel.setLayout(new BoxLayout(skillsPanel, BoxLayout.Y_AXIS));
        skillsPanel.setBackground(Color.WHITE);
        
        // Sort skills by count and take top 10
        List<Map.Entry<String, Integer>> sortedSkills = new ArrayList<>(skillsCount.entrySet());
        sortedSkills.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        
        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedSkills) {
            if (count++ >= 10) break;
            JPanel skillBar = createSkillBar(entry.getKey(), entry.getValue());
            skillsPanel.add(skillBar);
            skillsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        JScrollPane skillsScroll = new JScrollPane(skillsPanel);
        skillsScroll.setBorder(null);
        skillsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        skillsChartPanel.add(skillsScroll, BorderLayout.CENTER);
        
        // Refresh UI
        categoryChartPanel.revalidate();
        categoryChartPanel.repaint();
        skillsChartPanel.revalidate();
        skillsChartPanel.repaint();
    }

    private JPanel createCategoryBar(String category, int count) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(category);
        nameLabel.setFont(MainFrame.LABEL_FONT);
        nameLabel.setPreferredSize(new Dimension(150, 20));
        
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(MainFrame.SMALL_FONT);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Calculate width based on the maximum count
        int maxCount = categoryCount.values().stream().max(Integer::compareTo).orElse(1);
        int barWidth = (int) (300 * ((double) count / maxCount));
        
        JPanel barPanel = new JPanel();
        barPanel.setBackground(MainFrame.PRIMARY_COLOR);
        barPanel.setPreferredSize(new Dimension(barWidth, 20));
        
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(barPanel, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createSkillBar(String skill, int count) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(skill);
        nameLabel.setFont(MainFrame.LABEL_FONT);
        nameLabel.setPreferredSize(new Dimension(150, 20));
        
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(MainFrame.SMALL_FONT);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Calculate width based on the maximum count
        int maxCount = skillsCount.values().stream().max(Integer::compareTo).orElse(1);
        int barWidth = (int) (300 * ((double) count / maxCount));
        
        JPanel barPanel = new JPanel();
        barPanel.setBackground(MainFrame.SUCCESS_COLOR);
        barPanel.setPreferredSize(new Dimension(barWidth, 20));
        
        panel.add(nameLabel, BorderLayout.WEST);
        panel.add(barPanel, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.EAST);
        
        return panel;
    }

    private void exportResults() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Analysis Results");
        fileChooser.setSelectedFile(new File("job_analysis_results.json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File outputFile = fileChooser.getSelectedFile();
                
                // Prepare export data
                Map<String, Object> exportData = new HashMap<>();
                exportData.put("category_distribution", categoryCount);
                exportData.put("skills_frequency", skillsCount);
                exportData.put("analysis_timestamp", new Date().toString());
                
                // Write to file
                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, exportData);
                
                JOptionPane.showMessageDialog(this,
                    "Results exported successfully to: " + outputFile.getAbsolutePath(),
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