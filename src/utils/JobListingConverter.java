package utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jobanalysis.models.JobListing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for converting raw JSON data to JobListing objects and handling data transformations.
 */
public class JobListingConverter {

    /**
     * Converts raw JSON file to a list of JobListing objects and saves cleaned data.
     *
     * @param inputFilePath Path to the raw JSON file
     * @param outputFilePath Path to save the cleaned data
     * @return List of JobListing objects
     * @throws IOException If an error occurs during file operations
     */
    public static List<JobListing> convertAndCleanJobListings(String inputFilePath, String outputFilePath) throws IOException {
        // Read the JSON file
        ObjectMapper mapper = new ObjectMapper();
        List<JobListing> rawListings = mapper.readValue(new File(inputFilePath), 
                                            new TypeReference<List<JobListing>>() {});
        
        System.out.println("Read " + rawListings.size() + " raw job listings");
        
        // Clean and deduplicate listings
        List<JobListing> cleanedListings = deduplicateAndClean(rawListings);
        System.out.println("Processed to " + cleanedListings.size() + " cleaned job listings");
        
        // Save the cleaned data
        mapper.writeValue(new File(outputFilePath), cleanedListings);
        System.out.println("Saved cleaned job listings to: " + outputFilePath);
        
        return cleanedListings;
    }
    
    /**
     * Deduplicate and clean job listings by removing duplicates and sanitizing text.
     *
     * @param listings Raw job listings
     * @return Cleaned and deduplicated list of job listings
     */
    private static List<JobListing> deduplicateAndClean(List<JobListing> listings) {
        List<JobListing> cleaned = new ArrayList<>();
        Set<String> uniqueJobs = new HashSet<>();
        
        for (JobListing job : listings) {
            // Create a unique identifier for this job (title + company + first 100 chars of description)
            String uniqueId = (job.getTitle() + "_" + job.getCompany() + "_" + 
                              (job.getDescription() != null ? 
                               job.getDescription().substring(0, Math.min(100, job.getDescription().length())) : ""))
                              .toLowerCase();
            
            // Skip if we've seen this job before
            if (uniqueJobs.contains(uniqueId)) {
                continue;
            }
            
            // Clean the job listing
            JobListing cleanedJob = cleanJobListing(job);
            
            // Add to our cleaned list and mark as seen
            cleaned.add(cleanedJob);
            uniqueJobs.add(uniqueId);
        }
        
        return cleaned;
    }
    
    /**
     * Clean an individual job listing by removing problematic characters and truncated text.
     *
     * @param job Job listing to clean
     * @return Cleaned job listing
     */
    private static JobListing cleanJobListing(JobListing job) {
        // Remove the "+plus" suffix from descriptions
        if (job.getDescription() != null) {
            job.setDescription(job.getDescription().replaceAll("\\s*\\+plus\\s*$", ""));
        }
        
        // Remove any HTML tags
        if (job.getDescription() != null) {
            job.setDescription(job.getDescription().replaceAll("<[^>]*>", ""));
        }
        
        // Fix encoding issues
        if (job.getTitle() != null) {
            job.setTitle(fixEncoding(job.getTitle()));
        }
        
        if (job.getCompany() != null) {
            job.setCompany(fixEncoding(job.getCompany()));
        }
        
        if (job.getDescription() != null) {
            job.setDescription(fixEncoding(job.getDescription()));
        }
        
        return job;
    }
    
    /**
     * Fix common encoding issues in text.
     *
     * @param text Text to fix
     * @return Fixed text
     */
    private static String fixEncoding(String text) {
        // Replace common problematic character sequences
        return text.replaceAll("â€™", "'")
                  .replaceAll("â€œ", "\"")
                  .replaceAll("â€", "\"")
                  .replaceAll("Ã©", "é")
                  .replaceAll("Ã¨", "è")
                  .replaceAll("Ã", "à")
                  .replaceAll("Ã´", "ô")
                  .replaceAll("Ã®", "î")
                  .replaceAll("Ã¢", "â")
                  .replaceAll("Ã§", "ç");
    }
    
    /**
     * Main method to demonstrate usage.
     */
    public static void main(String[] args) {
        try {
            // Make sure data directory exists
            Files.createDirectories(Paths.get("data"));
            
            // Convert and clean the data
            List<JobListing> jobListings = convertAndCleanJobListings(
                "job_listings2.json", 
                "data/cleaned_job_listings.json"
            );
            
            System.out.println("Conversion complete! " + jobListings.size() + " job listings processed.");
            
        } catch (IOException e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}