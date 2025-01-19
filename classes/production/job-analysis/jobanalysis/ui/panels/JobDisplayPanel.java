package jobanalysis.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import jobanalysis.models.JobOffer;

public class JobDisplayPanel extends JPanel {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private static final String LOADING_CARD = "LOADING";
    private static final String CONTENT_CARD = "CONTENT";
    private static final String ERROR_CARD = "ERROR";

    public JobDisplayPanel() {
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Loading panel
        JPanel loadingPanel = new JPanel(new GridBagLayout());
        loadingPanel.add(new JLabel("Loading jobs..."));

        // Error panel
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.add(new JLabel("Error loading jobs"));

        // Add cards
        contentPanel.add(loadingPanel, LOADING_CARD);
        contentPanel.add(new JPanel(), CONTENT_CARD);
        contentPanel.add(errorPanel, ERROR_CARD);

        add(contentPanel, BorderLayout.CENTER);
    }

    public void showLoading() {
        cardLayout.show(contentPanel, LOADING_CARD);
    }

    public void showError() {
        cardLayout.show(contentPanel, ERROR_CARD);
    }

    public void displayJobs(List<JobOffer> jobs) {
        JPanel jobsPanel = new JPanel();
        jobsPanel.setLayout(new BoxLayout(jobsPanel, BoxLayout.Y_AXIS));

        for (JobOffer job : jobs) {
            jobsPanel.add(createJobPanel(job));
            jobsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Update content card
        contentPanel.remove(1); // Remove old content
        contentPanel.add(new JScrollPane(jobsPanel), CONTENT_CARD);
        cardLayout.show(contentPanel, CONTENT_CARD);
    }

    private JPanel createJobPanel(JobOffer job) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Title and company
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(MainFrame.TITLE_FONT.deriveFont(14f));
        JLabel companyLabel = new JLabel(job.getCompany());
        companyLabel.setForeground(Color.GRAY);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(companyLabel, BorderLayout.CENTER);

        // Location and salary
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (job.getLocation() != null && !job.getLocation().isEmpty()) {
            detailsPanel.add(new JLabel("📍 " + job.getLocation()));
        }
        if (job.getSalary() != null && !job.getSalary().isEmpty()) {
            detailsPanel.add(new JLabel("💰 " + job.getSalary()));
        }

        // Description (if available)
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        if (job.getDescription() != null && !job.getDescription().isEmpty()) {
            JTextArea descriptionArea = new JTextArea(job.getDescription());
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setEditable(false);
            descriptionArea.setRows(3);
            descriptionPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        }

        // URL button
        JButton urlButton = new JButton("View Job");
        urlButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI(job.getUrl()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Assemble panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        if (!descriptionPanel.isPreferredSizeSet()) {
            panel.add(descriptionPanel, BorderLayout.CENTER);
        }
        panel.add(urlButton, BorderLayout.SOUTH);

        return panel;
    }
}