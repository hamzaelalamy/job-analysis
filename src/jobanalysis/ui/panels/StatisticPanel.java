package jobanalysis.ui.panels;

import jobanalysis.models.JobOffer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticPanel extends JPanel {
    private List<JobOffer> jobs;

    public StatisticPanel() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
    }

    public void updateStatistics(List<JobOffer> jobs) {
        this.jobs = jobs;
        removeAll();

        add(createContractTypeChart());
        add(createLocationChart());
        add(createSalaryRangeChart());
        add(createWorkplaceTypeChart());

        revalidate();
        repaint();
    }

    private JPanel createContractTypeChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        Map<String, Long> contractTypes = jobs.stream()
                .collect(Collectors.groupingBy(
                        job -> job.getEmploymentType() != null ? job.getEmploymentType() : "Non spécifié",
                        Collectors.counting()
                ));

        contractTypes.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Types de Contrat",
                dataset,
                true,
                true,
                false
        );

        return createChartPanel(chart);
    }

    private JPanel createLocationChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Long> locations = jobs.stream()
                .collect(Collectors.groupingBy(
                        job -> job.getLocation() != null ? job.getLocation() : "Non spécifié",
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        locations.forEach((location, count) ->
                dataset.addValue(count, "Locations", location));

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Locations",
                "Location",
                "Nombre d'offres",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        return createChartPanel(chart);
    }

    private JPanel createSalaryRangeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Long> salaryRanges = jobs.stream()
                .collect(Collectors.groupingBy(
                        this::categorizeSalary,
                        Collectors.counting()
                ));

        salaryRanges.forEach((range, count) ->
                dataset.addValue(count, "Salaires", range));

        JFreeChart chart = ChartFactory.createBarChart(
                "Tranches de Salaire",
                "Tranche",
                "Nombre d'offres",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        return createChartPanel(chart);
    }

    private JPanel createWorkplaceTypeChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        Map<String, Long> workplaceTypes = jobs.stream()
                .collect(Collectors.groupingBy(
                        job -> job.getWorkplaceType() != null ? job.getWorkplaceType() : "Non spécifié",
                        Collectors.counting()
                ));

        workplaceTypes.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Types de Travail",
                dataset,
                true,
                true,
                false
        );

        return createChartPanel(chart);
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

    private JPanel createChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return chartPanel;
    }
}