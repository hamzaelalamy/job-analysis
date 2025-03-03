package jobanalysis.ui.panels;

import jobanalysis.models.JobOffer;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {
    private final JPanel contentPanel;
    private final JPanel chartsPanel;
    private List<JobOffer> currentJobs;
    private final Color THEME_COLOR = new Color(70, 130, 180);

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // Panel des graphiques
        chartsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Bouton d'actualisation
        JButton refreshButton = createStyledButton("Actualiser", THEME_COLOR);
        refreshButton.addActionListener(e -> refreshCharts());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);

        contentPanel.add(buttonPanel, BorderLayout.NORTH);
        contentPanel.add(chartsPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        chartsPanel.removeAll();
        JLabel welcomeLabel = new JLabel("Bienvenue dans le Dashboard d'Analyse", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(THEME_COLOR);
        chartsPanel.add(welcomeLabel);
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    public void updateDashboard(List<JobOffer> jobs) {
        this.currentJobs = jobs;
        if (jobs != null && !jobs.isEmpty()) {
            refreshCharts();
        }
    }

    private void refreshCharts() {
        if (currentJobs == null || currentJobs.isEmpty()) {
            showError("Aucune donnée disponible pour l'analyse");
            return;
        }

        chartsPanel.removeAll();

        // Création des panneaux d'analyse
        chartsPanel.add(createCompanyDistributionPanel());
        chartsPanel.add(createLocationDistributionPanel());
        chartsPanel.add(createEmploymentTypePanel());
        chartsPanel.add(createSalaryDistributionPanel());

        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private JPanel createCompanyDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledBorder("Top Entreprises"));

        Map<String, Long> companyDistribution = currentJobs.stream()
                .collect(Collectors.groupingBy(
                        JobOffer::getCompany,
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> topCompanies = companyDistribution.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        JPanel barsPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        barsPanel.setBackground(Color.WHITE);

        for (Map.Entry<String, Long> entry : topCompanies) {
            barsPanel.add(createBarComponent(
                    entry.getKey(),
                    entry.getValue(),
                    companyDistribution.values().stream().mapToLong(l -> l).max().orElse(1),
                    new Color(70, 130, 180)
            ));
        }

        panel.add(barsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLocationDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledBorder("Top Localisations"));

        Map<String, Long> locationDistribution = currentJobs.stream()
                .collect(Collectors.groupingBy(
                        JobOffer::getLocation,
                        Collectors.counting()
                ));

        List<Map.Entry<String, Long>> topLocations = locationDistribution.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        JPanel barsPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        barsPanel.setBackground(Color.WHITE);

        for (Map.Entry<String, Long> entry : topLocations) {
            barsPanel.add(createBarComponent(
                    entry.getKey(),
                    entry.getValue(),
                    locationDistribution.values().stream().mapToLong(l -> l).max().orElse(1),
                    new Color(60, 179, 113)
            ));
        }

        panel.add(barsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEmploymentTypePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledBorder("Types de Contrat"));

        Map<String, Long> typeDistribution = currentJobs.stream()
                .collect(Collectors.groupingBy(
                        job -> job.getEmploymentType() != null ? job.getEmploymentType() : "Non spécifié",
                        Collectors.counting()
                ));

        JPanel barsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        barsPanel.setBackground(Color.WHITE);

        long maxValue = typeDistribution.values().stream().mapToLong(l -> l).max().orElse(1);

        typeDistribution.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> barsPanel.add(createBarComponent(
                        entry.getKey(),
                        entry.getValue(),
                        maxValue,
                        new Color(106, 90, 205)
                )));

        panel.add(barsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSalaryDistributionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(createTitledBorder("Tranches de Salaire"));

        Map<String, Long> salaryRanges = currentJobs.stream()
                .collect(Collectors.groupingBy(
                        this::categorizeSalary,
                        Collectors.counting()
                ));

        JPanel barsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        barsPanel.setBackground(Color.WHITE);

        long maxValue = salaryRanges.values().stream().mapToLong(l -> l).max().orElse(1);

        salaryRanges.entrySet().stream()
                .forEach(entry -> barsPanel.add(createBarComponent(
                        entry.getKey(),
                        entry.getValue(),
                        maxValue,
                        new Color(218, 112, 214)
                )));

        panel.add(barsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBarComponent(String label, long value, long maxValue, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);

        JLabel labelComponent = new JLabel(label + ": " + value);
        labelComponent.setPreferredSize(new Dimension(150, 25));

        JPanel barComponent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int width = (int) ((getWidth() * value) / maxValue);
                g.setColor(color);
                g.fillRect(0, 0, width, getHeight());
            }
        };
        barComponent.setBackground(new Color(240, 240, 240));
        barComponent.setPreferredSize(new Dimension(200, 25));

        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(barComponent, BorderLayout.CENTER);

        return panel;
    }

    private String categorizeSalary(JobOffer job) {
        String salary = job.getSalary();
        if (salary == null || salary.isEmpty()) return "Non spécifié";

        if (salary.toLowerCase().contains("k")) {
            try {
                int value = Integer.parseInt(salary.toLowerCase()
                        .replace("k", "")
                        .replaceAll("[^0-9]", ""));

                if (value < 50) return "< 50K";
                if (value < 80) return "50K-80K";
                if (value < 100) return "80K-100K";
                if (value < 150) return "100K-150K";
                return "150K+";
            } catch (NumberFormatException e) {
                return "Non spécifié";
            }
        }
        return "Non spécifié";
    }

    private javax.swing.border.Border createTitledBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 200, 200)),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Segoe UI", Font.BOLD, 14),
                        THEME_COLOR
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Erreur Dashboard",
                    JOptionPane.ERROR_MESSAGE);
        });
    }
}