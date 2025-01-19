package jobanalysis.ml;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class DataPreprocessor {
    
    // Adding @JsonIgnoreProperties to handle any unknown fields in the JSON
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JobListing {
        public String title;
        public String company;
        public String location;
        public String description;
        public String salary;
        public String experienceLevel;
        public String employmentType;
        public String workplaceType;
        public String requiredSkills;
        public String url;
        public String postedDate;
        public String applicationDeadline;
        public String benefits;
        public String companyDescription;
    }

    public static List<String[]> preprocessJSON(String resourcePath) {
        List<String[]> processedData = new ArrayList<>();
        int totalRows = 0;
        int skippedRows = 0;

        try {
            
            InputStream inputStream = DataPreprocessor.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }

            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            ObjectMapper mapper = new ObjectMapper();
            List<JobListing> jobListings = mapper.readValue(jsonContent, 
                new TypeReference<List<JobListing>>() {});

            System.out.println("Total jobs found in JSON: " + jobListings.size());

            for (JobListing job : jobListings) {
                totalRows++;
                
                String[] cleanedLine = cleanJobListing(job);
                if (cleanedLine != null) {
                    processedData.add(cleanedLine);
                } else {
                    skippedRows++;
                }
            }

            System.out.println("Total rows processed: " + totalRows);
            System.out.println("Rows skipped: " + skippedRows);
            System.out.println("Rows kept: " + processedData.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return processedData;
    }

    private static String[] cleanJobListing(JobListing job) {
        
        if (job.title == null || job.description == null) {
            return null;
        }

        String jobTitle = cleanText(job.title);
        String jobDescription = cleanText(job.description);
        String location = cleanText(job.location);
        String companyName = cleanText(job.company);
        String skills = cleanText(job.requiredSkills);

        
        if (jobTitle.isEmpty() || jobDescription.isEmpty() || 
            jobTitle.matches("\\*+") || jobDescription.matches("\\*+") ||
            jobTitle.equalsIgnoreCase("sign in to create job alert")) {
            return null;
        }

        
        return new String[]{jobTitle, companyName, location, jobDescription, skills};
    }

    private static String cleanText(String text) {
        if (text == null || text.isEmpty()) return "";

        // Remove HTML tags
        text = text.replaceAll("<[^>]*>", "");

        // Remove "Show more Show less" from descriptions
        text = text.replaceAll("Show more Show less", "");

        // Keep Arabic characters, French accents, and basic punctuation
        text = text.replaceAll("[^a-zA-Z0-9\\s\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\u0980-\\u09FF\\u0100-\\u017F.,;()]", " ");

        // Replace multiple spaces with single space
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    public static void saveProcessedData(List<String[]> data, String outputFileName) {
        System.out.println("Saving processed data. Total rows: " + data.size());
        if (data.isEmpty()) {
            System.out.println("No data to save. Exiting...");
            return;
        }

        try {
            URL resourceUrl = DataPreprocessor.class.getClassLoader().getResource("");
            if (resourceUrl == null) {
                throw new FileNotFoundException("Could not locate resources directory.");
            }
            String outputPath = resourceUrl.getPath() + outputFileName;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
                
                writer.write("Job Title,Company,Location,Description,Required Skills\n");
                
                for (String[] line : data) {
                    // Properly escape CSV values and handle commas
                    String csvLine = String.join(",", Arrays.stream(line)
                        .map(DataPreprocessor::escapeCSV)
                        .toArray(String[]::new));
                    writer.write(csvLine + "\n");
                }
            }

            System.out.println("File saved successfully at: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static void main(String[] args) {
        String inputFileName = "job_listings.json";
        String outputFileName = "cleaned_job_listings.csv";

        List<String[]> cleanedData = preprocessJSON(inputFileName);
        saveProcessedData(cleanedData, outputFileName);

        System.out.println("Data preprocessing complete!");
    }
}