package jobanalysis.ui.panels;

import jobanalysis.ml.DataPreprocessor;
import jobanalysis.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileUploadPanel extends JPanel {
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JTextArea previewArea;
    private JButton uploadButton;
    private JButton preprocessButton;
    private File uploadedFile;
    private MainFrame parent;
    private JButton analyzeButton;

    public FileUploadPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBackground(MainFrame.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel with Preview
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel with Buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Data Preprocessing");
        titleLabel.setFont(MainFrame.TITLE_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        statusLabel = new JLabel("Ready to upload file");
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

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Preview Area
        previewArea = new JTextArea();
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        previewArea.setEditable(false);
        previewArea.setText("File preview will appear here...");

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(MainFrame.BACKGROUND_COLOR);

        // Upload Button
        uploadButton = MainFrame.createStyledButton("Upload File", "primary");
        uploadButton.addActionListener(e -> handleFileUpload());

        // Preprocess Button
        preprocessButton = MainFrame.createStyledButton("Preprocess Data", "success");
        preprocessButton.setEnabled(false);
        preprocessButton.addActionListener(e -> handlePreprocessing());
        
        // Analyze Button 
        JButton analyzeButton = MainFrame.createStyledButton("Analyze Data", "secondary");
        analyzeButton.setEnabled(false);
        analyzeButton.addActionListener(e -> parent.showClassification());

        bottomPanel.add(uploadButton);
        bottomPanel.add(preprocessButton);
        bottomPanel.add(analyzeButton);

        return bottomPanel;
    }

    private void handleFileUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Data File");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "JSON Files (*.json)", "json"
        ));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            uploadedFile = fileChooser.getSelectedFile();
            try {
                // Copy file to resources directory
                String resourcePath = "src/main/resources/";
                File resourceDir = new File(resourcePath);
                if (!resourceDir.exists()) {
                    resourceDir.mkdirs();
                }

                Path destination = Path.of(resourcePath + "job_listings.json");
                Files.copy(uploadedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Update UI
                String preview = new String(Files.readAllBytes(uploadedFile.toPath()));
                previewArea.setText(preview.substring(0, Math.min(preview.length(), 1000)) + "...");
                statusLabel.setText("File uploaded: " + uploadedFile.getName());
                preprocessButton.setEnabled(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error uploading file: " + ex.getMessage(),
                    "Upload Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void handlePreprocessing() {
        if (uploadedFile == null) {
            JOptionPane.showMessageDialog(this,
                "Please upload a file first",
                "Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Start preprocessing in background
        progressBar.setVisible(true);
        preprocessButton.setEnabled(false);
        uploadButton.setEnabled(false);
        statusLabel.setText("Preprocessing data...");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Create data directory if it doesn't exist
                File dataDir = new File("data");
                if (!dataDir.exists()) {
                    dataDir.mkdirs();
                }

                // Run preprocessing using full file paths
                String inputPath = uploadedFile.getAbsolutePath();
                String outputPath = new File(dataDir, "cleaned_job_listings.json").getAbsolutePath();
                
                List<String[]> processedData = DataPreprocessor.preprocessJSON(inputPath);
                DataPreprocessor.saveProcessedDataAsJSON(processedData, outputPath);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    JOptionPane.showMessageDialog(FileUploadPanel.this,
                        "Preprocessing completed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    statusLabel.setText("Preprocessing completed");
                    
                    // Update preview with processed data from data directory
                    File processedFile = new File("data/cleaned_job_listings.json");
                    String processedContent = Files.readString(processedFile.toPath());
                    previewArea.setText(processedContent.substring(0, Math.min(processedContent.length(), 1000)) + "...");

                    // Enable the analyze button
                    analyzeButton.setEnabled(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FileUploadPanel.this,
                        "Error during preprocessing: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Preprocessing failed");
                    ex.printStackTrace();
                } finally {
                    progressBar.setVisible(false);
                    preprocessButton.setEnabled(true);
                    uploadButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
}