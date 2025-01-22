package jobanalysis.ui.panels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jobanalysis.models.JobOffer;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

public class JobDisplayPanel extends JPanel {
    private JPanel contentPanel;
    private JLabel statusLabel;
    private final Color HOVER_COLOR = new Color(245, 247, 250);
    private final int CARD_WIDTH = 900;
    private final int CARD_HEIGHT = 220;
    private List<JobOffer> currentJobs = new ArrayList<>();
    private final ObjectMapper objectMapper;

    // Custom colors
    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color NEUTRAL_COLOR = new Color(108, 117, 125);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_BORDER_COLOR = new Color(230, 230, 230);

    public JobDisplayPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        // Initialize Jackson ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        initializeComponents();
    }

    private void initializeComponents() {
        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(BACKGROUND_COLOR);
        addControlButtons(controlPanel);
        add(controlPanel, BorderLayout.NORTH);

        // Content Panel with vertical scrolling
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Scrollable pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND_COLOR);

        // Status label for messages
        statusLabel = new JLabel("No jobs to display", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(NEUTRAL_COLOR);

        add(scrollPane, BorderLayout.CENTER);
        showStatus();
    }

    private void addControlButtons(JPanel controlPanel) {
        // Export Button with dropdown
        JButton exportButton = createStyledButton("Export", SUCCESS_COLOR);
        JPopupMenu exportMenu = new JPopupMenu();

        JMenuItem csvMenuItem = new JMenuItem("Export as CSV");
        csvMenuItem.addActionListener(e -> exportToCsv(currentJobs));

        JMenuItem jsonMenuItem = new JMenuItem("Export as JSON");
        jsonMenuItem.addActionListener(e -> exportToJson(currentJobs));

        exportMenu.add(csvMenuItem);
        exportMenu.add(jsonMenuItem);

        exportButton.addActionListener(e -> {
            exportMenu.show(exportButton, 0, exportButton.getHeight());
        });

        controlPanel.add(exportButton);
    }

    private void exportToJson(List<JobOffer> jobs) {
        if (jobs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No jobs to export",
                    "Export Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Jobs as JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        fileChooser.setSelectedFile(new File("job_listings.json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".json")) {
                file = new File(file.getParentFile(), file.getName() + ".json");
            }

            try {
                // Write JSON with pretty printing
                objectMapper.writeValue(file, jobs);

                JOptionPane.showMessageDialog(this,
                        "Successfully exported " + jobs.size() + " jobs to JSON!",
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting to JSON: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void exportToCsv(List<JobOffer> jobs) {
        if (jobs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No jobs to export",
                    "Export Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Jobs as CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("job_listings.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(file)) {
                // Write CSV header
                writer.println("Title,Company,Location,Salary,Description,Employment Type," +
                        "Posted Date,Workplace Type,Experience Level,Required Skills,Benefits,URL");

                // Write job data
                for (JobOffer job : jobs) {
                    writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                            escapeCSV(job.getTitle()),
                            escapeCSV(job.getCompany()),
                            escapeCSV(job.getLocation()),
                            escapeCSV(job.getSalary()),
                            escapeCSV(job.getDescription()),
                            escapeCSV(job.getEmploymentType()),
                            escapeCSV(job.getPostedDate()),
                            escapeCSV(job.getWorkplaceType()),
                            escapeCSV(job.getExperienceLevel()),
                            escapeCSV(job.getRequiredSkills()),
                            escapeCSV(job.getBenefits()),
                            escapeCSV(job.getUrl())
                    ));
                }

                JOptionPane.showMessageDialog(this,
                        "Successfully exported " + jobs.size() + " jobs to CSV!",
                        "Export Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting to CSV: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void displayJobs(List<JobOffer> jobs) {
        this.currentJobs = new ArrayList<>(jobs);
        contentPanel.removeAll();

        if (jobs.isEmpty()) {
            showStatus();
            return;
        }

        for (JobOffer job : jobs) {
            contentPanel.add(createJobCard(job));
            contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        revalidate();
        repaint();
    }

    private JPanel createJobCard(JobOffer job) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Left Panel - Company Logo/Icon
        JPanel logoPanel = createLogoPanel(job.getCompany());

        // Center Panel - Main Job Info
        JPanel mainInfoPanel = new JPanel();
        mainInfoPanel.setLayout(new BoxLayout(mainInfoPanel, BoxLayout.Y_AXIS));
        mainInfoPanel.setBackground(Color.WHITE);

        // Title and Company
        JLabel titleLabel = new JLabel(job.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(40, 40, 40));

        JLabel companyLabel = new JLabel(job.getCompany());
        companyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        companyLabel.setForeground(new Color(100, 100, 100));

        // Details Panel
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        detailsPanel.setBackground(Color.WHITE);

        addDetailBadgesIfPresent(detailsPanel, job);

        // Description Panel
        JTextArea descArea = new JTextArea();
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setForeground(new Color(80, 80, 80));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(Color.WHITE);
        descArea.setText(job.getDescription());
        descArea.setRows(3);

        // Right Panel - Action Buttons
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(Color.WHITE);

        JButton viewButton = createStyledButton("View Details", PRIMARY_COLOR);
        JButton saveButton = createStyledButton("Save", SUCCESS_COLOR);

        viewButton.addActionListener(e -> openJobUrl(job.getUrl()));
        saveButton.addActionListener(e -> saveJob(job));

        actionPanel.add(viewButton);
        actionPanel.add(Box.createVerticalStrut(10));
        actionPanel.add(saveButton);

        // Assemble the card
        mainInfoPanel.add(titleLabel);
        mainInfoPanel.add(Box.createVerticalStrut(5));
        mainInfoPanel.add(companyLabel);
        mainInfoPanel.add(Box.createVerticalStrut(10));
        mainInfoPanel.add(detailsPanel);
        mainInfoPanel.add(Box.createVerticalStrut(10));
        mainInfoPanel.add(descArea);

        card.add(logoPanel, BorderLayout.WEST);
        card.add(mainInfoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    private void addDetailBadgesIfPresent(JPanel detailsPanel, JobOffer job) {
        if (job.getLocation() != null && !job.getLocation().isEmpty()) {
            addDetailBadge(detailsPanel, "📍", job.getLocation(), new Color(230, 240, 255));
        }
        if (job.getSalary() != null && !job.getSalary().isEmpty()) {
            addDetailBadge(detailsPanel, "💰", job.getSalary(), new Color(230, 255, 240));
        }
        if (job.getPostedDate() != null && !job.getPostedDate().isEmpty()) {
            addDetailBadge(detailsPanel, "📅", job.getPostedDate(), new Color(255, 240, 230));
        }
        if (job.getEmploymentType() != null && !job.getEmploymentType().isEmpty()) {
            addDetailBadge(detailsPanel, "💼", job.getEmploymentType(), new Color(240, 230, 255));
        }
        if (job.getWorkplaceType() != null && !job.getWorkplaceType().isEmpty()) {
            addDetailBadge(detailsPanel, "🏢", job.getWorkplaceType(), new Color(255, 230, 245));
        }
        if (job.getExperienceLevel() != null && !job.getExperienceLevel().isEmpty()) {
            addDetailBadge(detailsPanel, "⭐", job.getExperienceLevel(), new Color(255, 245, 230));
        }
    }

    private JPanel createLogoPanel(String company) {
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setPreferredSize(new Dimension(60, 60));
        logoPanel.setBackground(new Color(245, 247, 250));
        logoPanel.setBorder(new LineBorder(CARD_BORDER_COLOR, 1, true));

        String initial = "?";
        if (company != null && !company.trim().isEmpty()) {
            initial = company.trim().substring(0, 1).toUpperCase();
        }

        JLabel logoLabel = new JLabel(initial);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoPanel.add(logoLabel);

        return logoPanel;
    }

    private void addDetailBadge(JPanel panel, String icon, String text, Color bgColor) {
        JLabel label = new JLabel(icon + " " + text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(60, 60, 60));
        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        label.setBackground(bgColor);
        label.setOpaque(true);
        panel.add(label);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void openJobUrl(String url) {
        try {
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening URL: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveJob(JobOffer job) {
        // Implement job saving functionality
        JOptionPane.showMessageDialog(this,
                "Job saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"").trim();
    }

    public void showLoading() {
        contentPanel.removeAll();
        statusLabel.setText("Loading jobs...");
        showStatus();
    }

    public void showError() {
        contentPanel.removeAll();
        statusLabel.setText("Error loading jobs");
        showStatus();
    }

    private void showStatus() {
        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(statusLabel);
        contentPanel.add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }
}