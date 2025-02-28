package jobanalysis.ui.panels;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import jobanalysis.models.JobOffer;
import jobanalysis.scraping.JSoupScraper;
import jobanalysis.ui.MainFrame;
import jobanalysis.ui.panels.DashboardPanel;

public class ScraperPanel extends JPanel {
    // Core components
    private final MainFrame parent;
    private final JSoupScraper scraper;
    private final DashboardPanel dashboardPanel;
    private JobDisplayPanel displayPanel;
    private CategoryPanel categoryPanel;
    private StatisticPanel statisticPanel;  // Déclarez la variable

    // UI Components
    private JPanel searchPanel;
    private JTextField urlField;
    private JTextField searchTermField;
    private JTextField locationField;
    private JComboBox<String> portalSelector;
    private JSpinner pageSpinner;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTabbedPane resultsTabbedPane;

    // Constants
    private static final Map<String, String> BASE_URLS = Map.of(
            "Indeed", "https://www.indeed.com/jobs",
            "LinkedIn", "https://www.linkedin.com/jobs/search",
            "Other", ""
    );

    public ScraperPanel(MainFrame parent) {
        this.parent = parent;
        this.scraper = new JSoupScraper();
        this.displayPanel = new JobDisplayPanel();
        this.categoryPanel = new CategoryPanel();
        this.dashboardPanel = new DashboardPanel();
        this.statisticPanel = new StatisticPanel();
        initializeUI();
    }

    private static void actionPerformed(ActionEvent e) {

    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(MainFrame.BACKGROUND_COLOR);

        // Header Panel
        createHeaderPanel();

        // Main Content Panel
        createMainContentPanel();

        // Initial state
        updateFieldVisibility();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        // Status Area
        JPanel statusArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusArea.setBackground(MainFrame.BACKGROUND_COLOR);

        statusLabel = new JLabel("Ready to search");
        statusLabel.setFont(MainFrame.LABEL_FONT);
        statusArea.add(statusLabel);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(200, 20));
        statusArea.add(progressBar);

        headerPanel.add(statusArea, BorderLayout.WEST);

        // Control Buttons
        JPanel controlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlButtons.setBackground(MainFrame.BACKGROUND_COLOR);

        // Bouton Dashboard
        JButton dashboardButton = MainFrame.createStyledButton("Dashboard", "primary");
        dashboardButton.addActionListener(e -> showDashboard());

        JButton helpButton = MainFrame.createStyledButton("Help", "outline");
        helpButton.setPreferredSize(new Dimension(100, 35));
        helpButton.addActionListener(e -> showHelpDialog());

        JButton logoutButton = MainFrame.createStyledButton("Logout", "danger");
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.addActionListener(e -> parent.showLogin());

        controlButtons.add(helpButton);
        controlButtons.add(logoutButton);
        controlButtons.add(dashboardButton);

        headerPanel.add(controlButtons, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        // Search Section
        createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Results Section with Tabs
        resultsTabbedPane = new JTabbedPane();
        resultsTabbedPane.setFont(MainFrame.LABEL_FONT);

        // List view
        displayPanel = new JobDisplayPanel();
        resultsTabbedPane.addTab("List View", new JScrollPane(displayPanel));

        // Category view
        categoryPanel = new CategoryPanel();
        resultsTabbedPane.addTab("Category View", new JScrollPane(categoryPanel));

        // Dashboard view
        resultsTabbedPane.addTab("Dashboard", new JScrollPane(dashboardPanel));
        mainPanel.add(resultsTabbedPane, BorderLayout.CENTER);

        statisticPanel = new StatisticPanel();
        resultsTabbedPane.addTab("Statistiques", new JScrollPane(statisticPanel));
        add(mainPanel, BorderLayout.CENTER);
    }

    private void createSearchPanel() {
        searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        addTitle(gbc);

        // Portal Selection
        addPortalSelection(gbc);

        // Search Terms
        addSearchTerms(gbc);

        // Location Field
        addLocationField(gbc);

        // URL Field (for Other)
        addUrlField(gbc);

        // Pages to Scrape
        addPagesSpinner(gbc);

        // Search Button
        addSearchButton(gbc);

        // Setup portal change listener
        portalSelector.addActionListener(e -> updateFieldVisibility());
    }

    private void addTitle(GridBagConstraints gbc) {
        JLabel titleLabel = new JLabel("Job Search");
        titleLabel.setFont(MainFrame.TITLE_FONT.deriveFont(24f));
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        searchPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
    }

    private void addPortalSelection(GridBagConstraints gbc) {
        JLabel portalLabel = new JLabel("Job Portal:");
        portalLabel.setFont(MainFrame.LABEL_FONT);

        portalSelector = new JComboBox<>(new String[]{"Indeed", "LinkedIn", "Other"});
        portalSelector.setFont(MainFrame.LABEL_FONT);

        gbc.gridy = 1;
        gbc.gridx = 0;
        searchPanel.add(portalLabel, gbc);

        gbc.gridx = 1;
        searchPanel.add(portalSelector, gbc);
    }

    private void addSearchTerms(GridBagConstraints gbc) {
        JLabel searchLabel = new JLabel("Search Terms:");
        searchLabel.setFont(MainFrame.LABEL_FONT);

        searchTermField = new JTextField(20);
        searchTermField.setFont(MainFrame.LABEL_FONT);

        gbc.gridy = 2;
        gbc.gridx = 0;
        searchPanel.add(searchLabel, gbc);

        gbc.gridx = 1;
        searchPanel.add(searchTermField, gbc);
    }

    private void addLocationField(GridBagConstraints gbc) {
        JLabel locationLabel = new JLabel("Location:");
        locationLabel.setFont(MainFrame.LABEL_FONT);

        locationField = new JTextField(20);
        locationField.setFont(MainFrame.LABEL_FONT);

        gbc.gridy = 3;
        gbc.gridx = 0;
        searchPanel.add(locationLabel, gbc);

        gbc.gridx = 1;
        searchPanel.add(locationField, gbc);
    }

    private void addUrlField(GridBagConstraints gbc) {
        JLabel urlLabel = new JLabel("Custom URL:");
        urlLabel.setFont(MainFrame.LABEL_FONT);

        urlField = new JTextField(20);
        urlField.setFont(MainFrame.LABEL_FONT);

        gbc.gridy = 4;
        gbc.gridx = 0;
        searchPanel.add(urlLabel, gbc);

        gbc.gridx = 1;
        searchPanel.add(urlField, gbc);
    }

    private void addPagesSpinner(GridBagConstraints gbc) {
        JLabel pagesLabel = new JLabel("Pages to Scrape:");
        pagesLabel.setFont(MainFrame.LABEL_FONT);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
        pageSpinner = new JSpinner(spinnerModel);
        pageSpinner.setFont(MainFrame.LABEL_FONT);
        ((JSpinner.DefaultEditor) pageSpinner.getEditor()).getTextField().setColumns(5);

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spinnerPanel.setBackground(Color.WHITE);
        spinnerPanel.add(pageSpinner);

        gbc.gridy = 5;
        gbc.gridx = 0;
        searchPanel.add(pagesLabel, gbc);

        gbc.gridx = 1;
        searchPanel.add(spinnerPanel, gbc);
    }

    private void addSearchButton(GridBagConstraints gbc) {
        JButton searchButton = MainFrame.createStyledButton("Start Search", "primary");
        searchButton.addActionListener(e -> handleSearch());

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        searchPanel.add(searchButton, gbc);
    }

    private void updateFieldVisibility() {
        boolean isOther = "Other".equals(portalSelector.getSelectedItem());
        searchTermField.setEnabled(!isOther);
        locationField.setEnabled(!isOther);
        urlField.setEnabled(isOther);

        // Clear fields when switching
        if (isOther) {
            searchTermField.setText("");
            locationField.setText("");
        } else {
            urlField.setText("");
        }
    }

    private void showDashboard() {
        // Sélectionner l'onglet Dashboard
        for (int i = 0; i < resultsTabbedPane.getTabCount(); i++) {
            if (resultsTabbedPane.getTitleAt(i).equals("Dashboard")) {
                resultsTabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }

    private boolean validateInputs() {
        String portal = (String) portalSelector.getSelectedItem();

        if ("Other".equals(portal)) {
            String url = urlField.getText().trim();
            if (url.isEmpty()) {
                showError("Please enter a valid URL");
                return false;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                urlField.setText("https://" + url);
            }
        } else {
            if (searchTermField.getText().trim().isEmpty()) {
                showError("Please enter search terms");
                return false;
            }
        }
        return true;
    }

    private String buildSearchUrl() {
        String portal = (String) portalSelector.getSelectedItem();

        if ("Other".equals(portal)) {
            return urlField.getText().trim();
        }

        String searchTerm = searchTermField.getText().trim().replace(" ", "+");
        String location = locationField.getText().trim().replace(" ", "+");

        String baseUrl = BASE_URLS.get(portal);
        return switch (portal) {
            case "LinkedIn" -> baseUrl + "?keywords=" + searchTerm + "&location=" + location;
            case "Indeed" -> baseUrl + "?q=" + searchTerm + "&l=" + location;
            default -> "";
        };
    }

    private void handleSearch() {
        if (!validateInputs()) {
            return;
        }

        String portal = (String) portalSelector.getSelectedItem();
        String url = buildSearchUrl();
        int pages = (Integer) pageSpinner.getValue();

        System.out.println("\nStarting search:");
        System.out.println("Portal: " + portal);
        System.out.println("URL: " + url);
        System.out.println("Pages: " + pages);

        startScraping(portal, url, pages);
    }

    private void startScraping(String portal, String url, int pages) {
        progressBar.setVisible(true);
        statusLabel.setText("Scraping in progress...");
        displayPanel.showLoading();
        categoryPanel.removeAll();
        categoryPanel.revalidate();
        categoryPanel.repaint();

        SwingWorker<List<JobOffer>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JobOffer> doInBackground() {
                try {
                    System.out.println("Starting scrape with parameters:");
                    System.out.println("Portal: " + portal);
                    System.out.println("URL: " + url);
                    System.out.println("Pages: " + pages);

                    return pages > 1 ?
                            scraper.scrapeMultiplePages(url, pages) :
                            scraper.scrapeJobPortal(portal.toLowerCase(), url);
                } catch (Exception ex) {
                    System.err.println("Scraping failed: " + ex.getMessage());
                    ex.printStackTrace();
                    throw new RuntimeException("Scraping failed: " + ex.getMessage(), ex);
                }
            }

            @Override
            protected void done() {
                handleScrapingComplete(this);
            }
        };
        worker.execute();
    }

    private void handleScrapingComplete(SwingWorker<List<JobOffer>, Void> worker) {
        try {
            List<JobOffer> jobs = worker.get();
            progressBar.setVisible(false);

            if (jobs.isEmpty()) {
                statusLabel.setText("No jobs found");
                showError("No jobs found. Try adjusting your search criteria.");
                displayPanel.showError();
                dashboardPanel.updateDashboard(jobs);
            } else {
                statusLabel.setText("Found " + jobs.size() + " jobs");
                displayPanel.displayJobs(jobs);
                categoryPanel.updateJobs(jobs);
                dashboardPanel.updateDashboard(jobs);
                statisticPanel.updateStatistics(jobs);

                // Update category counts
                for(String category : new String[]{"Software Development", "Data Science", "DevOps",
                        "Design", "Marketing", "Sales", "Management", "Other"}) {
                    categoryPanel.updateCategoryCount(category);
                }
            }
        } catch (Exception ex) {
            handleError(ex);
        }
    }

    private void handleError(Exception ex) {
        progressBar.setVisible(false);
        statusLabel.setText("Error occurred");
        ex.printStackTrace();

        String message = ex.getMessage();
        if (message != null && message.contains("403")) {
            showError("Access denied by job site. Try again later or use a different search.");
        } else {
            showError("Error during scraping: " + message);
        }
        displayPanel.showError();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showHelpDialog() {
        String helpMessage = """
            Job Search Help:
            
            1. Searching Jobs:
               - Select a job portal (Indeed, LinkedIn, or Other)
               - For Indeed/LinkedIn: Enter search terms and location
               - For Other: Enter the complete URL of a job listing page
            
            2. Viewing Results:
               - List View: Shows all jobs in a chronological list
               - Category View: Organizes jobs by industry/role
               - Each job card shows title, company, location, and salary
               - Click "View Details" to open the original job posting
            
            3. Search Options:
               - Number of Pages: Choose 1-10 pages to scrape
               - Location: Can be city, state, country, or "Remote"
               - Search Terms: Use keywords like "Java Developer" or "Data Scientist"
               - Custom URL: Must be a direct link to job listings page
            
            4. Categories Available:
               - Software Development: Programming and software engineering roles
               - Data Science: Data analysis, machine learning, AI positions
               - DevOps: System administration, cloud, and operations roles
               - Design: UI/UX, graphic design, and creative positions
               - Marketing: Digital marketing, content, and SEO roles
               - Sales: Sales, business development, and account management
               - Management: Team lead, manager, and executive positions
               - Other: All other job categories
            
            5. Best Practices:
               - Use specific search terms for better results
               - Start with 1-2 pages and increase if needed
               - If blocked by a site, wait a few minutes before retrying
               - For custom URLs, ensure they're job listing pages
               - Check both List and Category views for different perspectives
            
            6. Troubleshooting:
               - No Results: Try different search terms or location
               - Access Denied: Site may be blocking automated access, try later
               - Error Messages: Note the error and try suggested solutions
               - Incomplete Data: Some job posts may have partial information
               - Loading Issues: Refresh the search or try fewer pages
            
            7. Tips for Better Results:
               - Combine related keywords (e.g., "Java Spring Developer")
               - Use location variations (city vs. state)
               - Check multiple job portals for comprehensive results
               - Save interesting jobs by opening them in your browser
               - Some sites may limit access to prevent automated scraping
            
            Need more help? Contact support or check documentation for detailed guides.
            """;

        // Create a styled text area for better formatting
        JTextArea textArea = new JTextArea(helpMessage);
        textArea.setFont(MainFrame.LABEL_FONT);
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // Create a scrollable panel
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Show the help dialog
        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Job Search Help",
                JOptionPane.PLAIN_MESSAGE);
    }
}