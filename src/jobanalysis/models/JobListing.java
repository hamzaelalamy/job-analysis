package jobanalysis.models;

public class JobListing {
    private String title;
    private String company;
    private String description;
    private String location;
    private String requiredSkills;
    
    // Default constructor needed for JSON mapping
    public JobListing() {}
    
    // Getters and setters needed for JSON mapping
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
}