package jobanalysis.models;

public class JobOffer {
    private String title;
    private String company;
    private String description;
    private String location;
    private String sourceUrl;

    public JobOffer(String title, String company, String description, String location, String sourceUrl) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.location = location;
        this.sourceUrl = sourceUrl;
    }

    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}