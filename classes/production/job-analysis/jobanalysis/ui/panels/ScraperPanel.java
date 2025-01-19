// ScraperPanel.java
package jobanalysis.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import jobanalysis.models.JobOffer;
import jobanalysis.scraping.JSoupScraper;

public class ScraperPanel extends JPanel {
    private MainFrame parent;
    private JTextField urlField;
    private JComboBox<String> portalSelector;
    private JSpinner pageSpinner;
    private JSoupScraper scraper;
    private JobDisplayPanel displayPanel;

    public ScraperPanel(MainFrame parent) {
        this.parent = parent;
        this.scraper = new JSoupScraper();
        this.setLayout(new BorderLayout());
        this.setBackground(MainFrame.BACKGROUND_COLOR);

        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Portal selector
        JLabel portalLabel = new JLabel("Job Portal:");
        portalLabel.setFont(MainFrame.LABEL_FONT);
        String[] portals = {"LinkedIn", "Indeed", "Glassdoor", "Other"};
        portalSelector = new JComboBox<>(portals);

        // URL input
        JLabel urlLabel = new JLabel("URL:");
        urlLabel.setFont(MainFrame.LABEL_FONT);
        urlField = MainFrame.createStyledTextField();

        // Page count input
        JLabel pageLabel = new JLabel("Number of Pages:");
        pageLabel.setFont(MainFrame.LABEL_FONT);
        pageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));

        // Scrape button
        JButton scrapeButton = MainFrame.createStyledButton("Scrape Jobs");

        // Add components
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(portalLabel, gbc);

        gbc.gridx = 1;
        inputPanel.add(portalSelector, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(urlLabel, gbc);

        gbc.gridx = 1;
        inputPanel.add(urlField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(pageLabel, gbc);

        gbc.gridx = 1;
        inputPanel.add(pageSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        inputPanel.add(scrapeButton, gbc);

        // Create display panel
        displayPanel = new JobDisplayPanel();

        // Add panels to main panel
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(displayPanel), BorderLayout.CENTER);

        // Add action listener
        scrapeButton.addActionListener(e -> handleScraping());
    }

    private void handleScraping() {
        String url = urlField.getText();
        String portal = (String) portalSelector.getSelectedItem();
        int pages = (Integer) pageSpinner.getValue();

        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid URL", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show loading message
        displayPanel.showLoading();

        // Run scraping in background thread
        SwingWorker<List<JobOffer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JobOffer> doInBackground() {
                if (pages > 1) {
                    return scraper.scrapeMultiplePages(url, pages);
                } else {
                    return scraper.scrapeJobPortal(portal, url);
                }
            }

            @Override
            protected void done() {
                try {
                    List<JobOffer> jobs = get();
                    displayPanel.displayJobs(jobs);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ScraperPanel.this,
                            "Error scraping jobs: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    displayPanel.showError();
                }
            }
        };
        worker.execute();
    }
}