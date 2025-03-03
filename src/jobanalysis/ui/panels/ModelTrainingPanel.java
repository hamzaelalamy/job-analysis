package jobanalysis.ui.panels;

import jobanalysis.ml.JobModelTrainer;
import jobanalysis.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Panel for triggering and monitoring the job model training process.
 * This panel provides a user interface to start the training process,
 * monitor its progress, and visualize results.
 */
public class ModelTrainingPanel extends JPanel {
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton trainButton;
    private JButton viewModelsButton;
    private MainFrame parent;
    private JobModelTrainer modelTrainer;
    private File dataDirectory;
    private File modelDirectory;

    // Training configuration components
    private JCheckBox useCrossValidationCheckbox;
    private JSpinner iterationsSpinner;
    private JSlider trainingDataSlider;

    /**
     * Create a new ModelTrainingPanel.
     *
     * @param parent The parent MainFrame
     */
    public ModelTrainingPanel(MainFrame parent) {
        this.parent = parent;
        this.modelTrainer = new JobModelTrainer();

        // Set up directories
        this.dataDirectory = new File("data");
        this.modelDirectory = new File("data/job_models");

        // Check/create directories
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        if (!modelDirectory.exists()) {
            modelDirectory.mkdirs();
        }

        setLayout(new BorderLayout(10, 10));
        setBackground(MainFrame.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    /**
     * Initialize all UI components for the panel.
     */
    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel with Configuration and Log
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel with Buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the header panel with title and status.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Job Model Training");
        titleLabel.setFont(MainFrame.TITLE_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        statusLabel = new JLabel("Ready to train models");
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

    /**
     * Create the center panel with configuration and log area.
     */
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        // Configuration Panel
        JPanel configPanel = createConfigPanel();
        centerPanel.add(configPanel, BorderLayout.NORTH);

        // Log Area
        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setText("Training log output will appear here...\n");
        logArea.setBackground(new Color(250, 250, 250));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setPreferredSize(new Dimension(800, 400));

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    /**
     * Create the configuration panel with training settings.
     */
    private JPanel createConfigPanel() {
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBackground(Color.WHITE);
        configPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel configTitle = new JLabel("Training Configuration");
        configTitle.setFont(MainFrame.HEADING_FONT);
        configTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        configPanel.add(configTitle);
        configPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Config options panel with grid layout
        JPanel optionsPanel = new JPanel(new GridLayout(3, 2, 20, 10));
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Cross-validation option
        useCrossValidationCheckbox = new JCheckBox("Use cross-validation");
        useCrossValidationCheckbox.setFont(MainFrame.LABEL_FONT);
        useCrossValidationCheckbox.setSelected(true);

        // Training iterations
        JLabel iterationsLabel = new JLabel("Training Iterations:");
        iterationsLabel.setFont(MainFrame.LABEL_FONT);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5, 1, 20, 1);
        iterationsSpinner = new JSpinner(spinnerModel);
        iterationsSpinner.setFont(MainFrame.LABEL_FONT);

        // Training/Test split
        JLabel splitLabel = new JLabel("Training Data Percentage:");
        splitLabel.setFont(MainFrame.LABEL_FONT);

        trainingDataSlider = new JSlider(JSlider.HORIZONTAL, 50, 90, 80);
        trainingDataSlider.setBackground(Color.WHITE);
        trainingDataSlider.setMajorTickSpacing(10);
        trainingDataSlider.setMinorTickSpacing(5);
        trainingDataSlider.setPaintTicks(true);
        trainingDataSlider.setPaintLabels(true);

        // Add components to options panel
        optionsPanel.add(useCrossValidationCheckbox);
        optionsPanel.add(new JLabel()); // Empty cell
        optionsPanel.add(iterationsLabel);
        optionsPanel.add(iterationsSpinner);
        optionsPanel.add(splitLabel);
        optionsPanel.add(trainingDataSlider);

        configPanel.add(optionsPanel);

        // Dataset info panel
        JPanel datasetPanel = new JPanel(new GridLayout(2, 2, 20, 5));
        datasetPanel.setBackground(Color.WHITE);
        datasetPanel.setBorder(BorderFactory.createTitledBorder("Dataset Information"));
        datasetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add dataset info
        JLabel dataFileLabel = new JLabel("Data File:");
        dataFileLabel.setFont(MainFrame.LABEL_FONT);

        JLabel dataFileValueLabel = new JLabel("data/cleaned_job_listings.json");
        dataFileValueLabel.setFont(MainFrame.LABEL_FONT);
        dataFileValueLabel.setForeground(Color.GRAY);

        JLabel dataStatusLabel = new JLabel("Status:");
        dataStatusLabel.setFont(MainFrame.LABEL_FONT);

        JLabel dataStatusValueLabel = new JLabel(checkDataFileStatus());
        dataStatusValueLabel.setFont(MainFrame.LABEL_FONT);
        dataStatusValueLabel.setForeground(isDataFileReady() ? new Color(0, 150, 0) : Color.RED);

        datasetPanel.add(dataFileLabel);
        datasetPanel.add(dataFileValueLabel);
        datasetPanel.add(dataStatusLabel);
        datasetPanel.add(dataStatusValueLabel);

        configPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        configPanel.add(datasetPanel);

        return configPanel;
    }

    /**
     * Create the bottom panel with action buttons.
     */
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        trainButton = MainFrame.createStyledButton("Start Training", "success");
        trainButton.setEnabled(isDataFileReady());
        trainButton.addActionListener(e -> startTraining());

        JButton backButton = MainFrame.createStyledButton("Back to Analysis", "outline");
        backButton.addActionListener(e -> parent.showClassification());

        viewModelsButton = MainFrame.createStyledButton("View Trained Models", "primary");
        viewModelsButton.setEnabled(areModelsAvailable());
        viewModelsButton.addActionListener(e -> viewTrainedModels());

        bottomPanel.add(backButton);
        bottomPanel.add(trainButton);
        bottomPanel.add(viewModelsButton);

        return bottomPanel;
    }

    /**
     * Check if the data file is ready for training.
     */
    private boolean isDataFileReady() {
        File dataFile = new File("data/cleaned_job_listings.json");
        return dataFile.exists() && dataFile.length() > 0;
    }

    /**
     * Get a status message for the data file.
     */
    private String checkDataFileStatus() {
        File dataFile = new File("data/cleaned_job_listings.json");
        if (!dataFile.exists()) {
            return "File not found! Please process data first.";
        } else if (dataFile.length() == 0) {
            return "File exists but is empty.";
        } else {
            return "Ready (" + (dataFile.length() / 1024) + " KB)";
        }
    }

    /**
     * Check if trained models are available.
     */
    private boolean areModelsAvailable() {
        File modelDir = new File("data/job_models");
        if (modelDir.exists() && modelDir.isDirectory()) {
            File[] modelFiles = modelDir.listFiles((dir, name) -> name.endsWith(".model"));
            return modelFiles != null && modelFiles.length > 0;
        }
        return false;
    }

    /**
     * Start the model training process.
     */
    private void startTraining() {
        if (!isDataFileReady()) {
            JOptionPane.showMessageDialog(this,
                    "No cleaned data file found. Please process job data first.",
                    "Missing Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        trainButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setText("Training in progress...");
        logArea.setText(""); // Clear log

        // Create a custom PrintStream that redirects to the log area
        TextAreaOutputStream taos = new TextAreaOutputStream(logArea);
        PrintStream printStream = new PrintStream(taos);

        // Start training in a background thread
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Redirect System.out to our log area
                PrintStream originalOut = System.out;
                PrintStream originalErr = System.err;

                try {
                    System.setOut(printStream);
                    System.setErr(printStream);

                    // Log training configuration
                    publish("Starting model training with configuration:");
                    publish("- Cross-validation: " + useCrossValidationCheckbox.isSelected());
                    publish("- Training iterations: " + iterationsSpinner.getValue());
                    publish("- Training data split: " + trainingDataSlider.getValue() + "%");
                    publish("----------------------------------------");

                    // Execute the model training
                    modelTrainer.trainModel();

                } catch (Exception e) {
                    publish("\nError during training: " + e.getMessage());
                    e.printStackTrace(printStream);
                    throw e;
                } finally {
                    // Restore standard output streams
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    logArea.append(chunk + "\n");
                    // Auto-scroll to bottom
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    statusLabel.setText("Training completed successfully");
                    logArea.append("\nTraining completed successfully.\n");
                    JOptionPane.showMessageDialog(ModelTrainingPanel.this,
                            "Model training completed successfully!",
                            "Training Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    statusLabel.setText("Training failed");
                    logArea.append("\nTraining failed: " + e.getMessage() + "\n");
                    JOptionPane.showMessageDialog(ModelTrainingPanel.this,
                            "Error during model training: " + e.getMessage(),
                            "Training Failed",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    trainButton.setEnabled(true);
                    viewModelsButton.setEnabled(areModelsAvailable());
                }
            }
        };

        worker.execute();
    }

    /**
     * View details of trained models.
     */
    private void viewTrainedModels() {
        if (!areModelsAvailable()) {
            JOptionPane.showMessageDialog(this,
                    "No trained models available. Please train models first.",
                    "No Models",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create model info dialog
        JDialog modelDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Trained Models", true);
        modelDialog.setLayout(new BorderLayout());
        modelDialog.setSize(600, 400);
        modelDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);

        // Model list
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> modelList = new JList<>(listModel);
        modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelList.setFont(MainFrame.LABEL_FONT);

        JScrollPane listScroller = new JScrollPane(modelList);
        listScroller.setPreferredSize(new Dimension(200, 300));

        // Model details
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane detailsScroller = new JScrollPane(detailsArea);

        // Load model list
        File modelDir = new File("data/job_models");
        File[] modelFiles = modelDir.listFiles((dir, name) -> name.endsWith(".model"));
        if (modelFiles != null) {
            for (File modelFile : modelFiles) {
                listModel.addElement(modelFile.getName().replace(".model", ""));
            }
        }

        // Add selection listener
        modelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && modelList.getSelectedValue() != null) {
                String selectedModel = modelList.getSelectedValue();
                try {
                    File modelFile = new File(modelDir, selectedModel + ".model");
                    String stats = "Model: " + selectedModel + "\n";
                    stats += "File path: " + modelFile.getAbsolutePath() + "\n";
                    stats += "Size: " + (modelFile.length() / 1024) + " KB\n";
                    stats += "Last modified: " + new java.util.Date(modelFile.lastModified()) + "\n\n";

                    // Try to get some basic info about the model
                    stats += "This model is used for classifying job listings into the " +
                            selectedModel + " category.\n\n";

                    stats += "To use this model for classification, the JobListingClassifier\n";
                    stats += "will load it and compare job listing features against it\n";
                    stats += "to determine the probability that a job belongs to this category.";

                    detailsArea.setText(stats);
                } catch (Exception ex) {
                    detailsArea.setText("Error loading model information: " + ex.getMessage());
                }
            }
        });

        // Add to panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroller, detailsScroller);
        splitPane.setDividerLocation(200);
        contentPanel.add(splitPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> modelDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        modelDialog.add(contentPanel);
        modelDialog.setVisible(true);
    }

    /**
     * Custom OutputStream that redirects to a JTextArea.
     */
    private static class TextAreaOutputStream extends java.io.OutputStream {
        private final JTextArea textArea;
        private final StringBuilder buffer;

        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
            this.buffer = new StringBuilder();
        }

        @Override
        public void write(int b) {
            if (b == '\n') {
                final String text = buffer.toString();
                SwingUtilities.invokeLater(() -> {
                    textArea.append(text + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
                buffer.setLength(0);
            } else {
                buffer.append((char) b);
            }
        }
    }
}