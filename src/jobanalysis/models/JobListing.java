package jobanalysis.models;

import java.util.Date;
import java.util.Map;

/**
 * Represents a job listing with all relevant information.
 */
public class JobListing {
    private String title;
    private String company;
    private String location;
    private String description;
    private String requiredSkills;
    private String salary;
    private String url;
    private String employmentType;
    private String experienceLevel;
    private String workplaceType;
    private String postedDate;
    private String applicationDeadline;
    private String benefits;
    private String companyDescription;
    
    private Map<String, Object> analysisData;

    public void setAnalysisData(Map<String, Object> analysisData) {
        this.analysisData = analysisData;
    }

    public Map<String, Object> getAnalysisData() {
        return analysisData;
    }
    
    // Default constructor needed for Jackson deserialization
    public JobListing() {
    }
    
    /**
     * Constructor with essential fields.
     */
    public JobListing(String title, String company, String description) {
        this.title = title;
        this.company = company;
        this.description = description;
    }
    
    /**
     * Full constructor with all fields.
     */
    public JobListing(String title, String company, String location, String description,
                      String requiredSkills, String salary, String url, String employmentType,
                      String experienceLevel, String workplaceType, String postedDate,
                      String applicationDeadline, String benefits, String companyDescription) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.salary = salary;
        this.url = url;
        this.employmentType = employmentType;
        this.experienceLevel = experienceLevel;
        this.workplaceType = workplaceType;
        this.postedDate = postedDate;
        this.applicationDeadline = applicationDeadline;
        this.benefits = benefits;
        this.companyDescription = companyDescription;
    }
    
    // Getters and setters
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRequiredSkills() {
        return requiredSkills;
    }
    
    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
    
    public String getSalary() {
        return salary;
    }
    
    public void setSalary(String salary) {
        this.salary = salary;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getEmploymentType() {
        return employmentType;
    }
    
    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }
    
    public String getExperienceLevel() {
        return experienceLevel;
    }
    
    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    
    public String getWorkplaceType() {
        return workplaceType;
    }
    
    public void setWorkplaceType(String workplaceType) {
        this.workplaceType = workplaceType;
    }
    
    public String getPostedDate() {
        return postedDate;
    }
    
    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }
    
    public String getApplicationDeadline() {
        return applicationDeadline;
    }
    
    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }
    
    public String getBenefits() {
        return benefits;
    }
    
    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }
    
    public String getCompanyDescription() {
        return companyDescription;
    }
    
    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }
    
    @Override
    public String toString() {
        return "JobListing{" +
               "title='" + title + '\'' +
               ", company='" + company + '\'' +
               ", location='" + location + '\'' +
               ", description='" + (description != null ? description.substring(0, Math.min(50, description.length())) + "..." : "null") + '\'' +
               ", postedDate='" + postedDate + '\'' +
               '}';
    }}