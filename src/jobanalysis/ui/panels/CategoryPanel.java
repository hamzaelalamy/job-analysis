package jobanalysis.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import jobanalysis.models.JobOffer;
import jobanalysis.ui.MainFrame;

public class CategoryPanel extends JPanel {
    private Map<String, JPanel> categoryPanels;
    private Set<String> categories;

    public CategoryPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(MainFrame.BACKGROUND_COLOR);
        categoryPanels = new HashMap<>();
        categories = new HashSet<>(Arrays.asList(
                "Software Development",
                "Data Science",
                "DevOps",
                "Design",
                "Marketing",
                "Sales",
                "Management",
                "Other"
        ));
    }

    public void updateJobs(List<JobOffer> jobs) {
        removeAll();
        categoryPanels.clear();

        // Create category panels
        for (String category : categories) {
            JPanel categoryPanel = createCategoryPanel(category);
            categoryPanels.put(category, categoryPanel);
            add(categoryPanel);
            add(Box.createRigidArea(new Dimension(0, 15)));
        }

        // Categorize and add jobs
        for (JobOffer job : jobs) {
            String category = categorizeJob(job);
            JPanel targetPanel = categoryPanels.get(category);
            if (targetPanel != null) {
                JPanel jobPanel = createJobPanel(job);
                targetPanel.add(jobPanel);
                targetPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                targetPanel.setVisible(true);
            }
        }

        // Hide empty categories
        for (Map.Entry<String, JPanel> entry : categoryPanels.entrySet()) {
            if (entry.getValue().getComponentCount() <= 1) { // Only has title
                entry.getValue().setVisible(false);
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createCategoryPanel(String category) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainFrame.BACKGROUND_COLOR);

        // Category header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, getCategoryColor(category)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(category);
        titleLabel.setFont(MainFrame.TITLE_FONT.deriveFont(20f));
        titleLabel.setForeground(new Color(50, 50, 50));

        JLabel countLabel = new JLabel("0 jobs");
        countLabel.setFont(MainFrame.LABEL_FONT);
        countLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);

        panel.add(headerPanel);
        return panel;
    }

    private Color getCategoryColor(String category) {
        return switch (category) {
            case "Software Development" -> new Color(66, 134, 244);
            case "Data Science" -> new Color(234, 67, 53);
            case "DevOps" -> new Color(251, 188, 5);
            case "Design" -> new Color(52, 168, 83);
            case "Marketing" -> new Color(171, 71, 188);
            case "Sales" -> new Color(255, 128, 0);
            case "Management" -> new Color(3, 155, 229);
            default -> new Color(158, 158, 158);
        };
    }

    private String categorizeJob(JobOffer job) {
        String titleLower = job.getTitle().toLowerCase();
        String descLower = job.getDescription() != null ? job.getDescription().toLowerCase() : "";

        // Software Development keywords
        if (containsAny(titleLower, descLower,
                "software", "developer", "programmer", "engineer", "coding", "java", "python",
                "javascript", "web developer", "full stack", "backend", "frontend")) {
            return "Software Development";
        }

        // Data Science keywords
        if (containsAny(titleLower, descLower,
                "data scientist", "data analyst", "machine learning", "ai", "artificial intelligence",
                "analytics", "big data", "statistics", "statistical", "data mining")) {
            return "Data Science";
        }

        // DevOps keywords
        if (containsAny(titleLower, descLower,
                "devops", "cloud", "aws", "azure", "infrastructure", "ci/cd", "deployment",
                "kubernetes", "docker", "system admin", "sysadmin", "operations")) {
            return "DevOps";
        }

        // Design keywords
        if (containsAny(titleLower, descLower,
                "designer", "ux", "ui", "user experience", "user interface", "graphic",
                "creative", "artist", "illustrator", "photoshop", "figma")) {
            return "Design";
        }

        // Marketing keywords
        if (containsAny(titleLower, descLower,
                "marketing", "seo", "social media", "content", "brand", "digital marketing",
                "advertising", "communications", "pr", "public relations")) {
            return "Marketing";
        }

        // Sales keywords
        if (containsAny(titleLower, descLower,
                "sales", "account executive", "business development", "account manager",
                "customer success", "client", "revenue")) {
            return "Sales";
        }

        // Management keywords
        if (containsAny(titleLower, descLower,
                "manager", "director", "head of", "lead", "chief", "cto", "ceo", "vp",
                "supervisor", "coordinator")) {
            return "Management";
        }

        return "Other";
    }

    private boolean containsAny(String title, String description, String... keywords) {
        for (String keyword : keywords) {
            if (title.contains(keyword) || description.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private JPanel createJobPanel(JobOffer job) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(1200, 200));

        // Title and company panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(MainFrame.TITLE_FONT.deriveFont(16f));
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);

        JLabel companyLabel = new JLabel(job.getCompany());
        companyLabel.setFont(MainFrame.LABEL_FONT);
        companyLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(companyLabel, BorderLayout.CENTER);

        // Details panel
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        detailsPanel.setBackground(Color.WHITE);

        if (job.getLocation() != null && !job.getLocation().isEmpty()) {
            JLabel locationLabel = new JLabel("📍 " + job.getLocation());
            locationLabel.setFont(MainFrame.LABEL_FONT);
            detailsPanel.add(locationLabel);
        }

        if (job.getSalary() != null && !job.getSalary().isEmpty()) {
            JLabel salaryLabel = new JLabel("💰 " + job.getSalary());
            salaryLabel.setFont(MainFrame.LABEL_FONT);
            detailsPanel.add(salaryLabel);
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton viewButton = new JButton("View Details");
        viewButton.setFont(MainFrame.LABEL_FONT);
        viewButton.setBackground(MainFrame.PRIMARY_COLOR);
        viewButton.setForeground(Color.WHITE);
        viewButton.setBorderPainted(false);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new java.net.URI(job.getUrl()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error opening URL: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(viewButton);

        // Assemble all panels
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void updateCategoryCount(String category) {
        JPanel categoryPanel = categoryPanels.get(category);
        if (categoryPanel != null && categoryPanel.isVisible()) {
            JPanel headerPanel = (JPanel) categoryPanel.getComponent(0);
            JLabel countLabel = (JLabel) ((BorderLayout) headerPanel.getLayout()).getLayoutComponent(BorderLayout.EAST);
            int count = categoryPanel.getComponentCount() - 1; // Subtract header panel
            countLabel.setText(count + (count == 1 ? " job" : " jobs"));
        }
    }
}