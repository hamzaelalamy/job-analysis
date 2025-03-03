package jobanalysis.ml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class DataPreprocessor {
    
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

    public static List<String[]> preprocessJSON(String filePath) {
        List<String[]> processedData = new ArrayList<>();
        int totalRows = 0;
        int skippedRows = 0;

        try {
            // Read the file directly using the provided path
            String jsonContent = Files.readString(Path.of(filePath));
            
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
            System.err.println("Error processing file: " + e.getMessage());
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

    public static void saveProcessedDataAsJSON(List<String[]> data, String outputPath) {
        System.out.println("Saving processed data as JSON. Total rows: " + data.size());
        if (data.isEmpty()) {
            System.out.println("No data to save. Exiting...");
            return;
        }

        try {
            // Convert processed data to JSON objects
            List<Map<String, String>> jsonData = new ArrayList<>();
            for (String[] line : data) {
                Map<String, String> jobObject = new HashMap<>();
                jobObject.put("Job Title", line[0]);
                jobObject.put("Company", line[1]);
                jobObject.put("Location", line[2]);
                jobObject.put("Description", line[3]);
                jobObject.put("Required Skills", line[4]);
                jsonData.add(jobObject);
            }

            // Write JSON to file using ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), jsonData);

            System.out.println("File saved successfully at: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}