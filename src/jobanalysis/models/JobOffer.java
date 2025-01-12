package jobanalysis.models;

public class JobOffer {
    private final String title;
    private final String company;
    private final String location;
    private final String description;
    private final String requiredSkills;  // Added field
    private final String salary;
    private final String url;
    private final String employmentType;
    private final String experienceLevel;  // Added field
    private final String workplaceType;
    private final String postedDate;
    private final String applicationDeadline;
    private final String benefits;         // Added field
    private final String companyDescription; // Added field

    private JobOffer(Builder builder) {
        this.title = builder.title;
        this.company = builder.company;
        this.location = builder.location;
        this.description = builder.description;
        this.requiredSkills = builder.requiredSkills;
        this.salary = builder.salary;
        this.url = builder.url;
        this.employmentType = builder.employmentType;
        this.experienceLevel = builder.experienceLevel;
        this.workplaceType = builder.workplaceType;
        this.postedDate = builder.postedDate;
        this.applicationDeadline = builder.applicationDeadline;
        this.benefits = builder.benefits;
        this.companyDescription = builder.companyDescription;
    }

    // Getters for all fields
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getSalary() { return salary; }
    public String getUrl() { return url; }
    public String getEmploymentType() { return employmentType; }
    public String getExperienceLevel() { return experienceLevel; }
    public String getWorkplaceType() { return workplaceType; }
    public String getPostedDate() { return postedDate; }
    public String getApplicationDeadline() { return applicationDeadline; }
    public String getBenefits() { return benefits; }
    public String getCompanyDescription() { return companyDescription; }

    public static class Builder {
        private String title = "";
        private String company = "";
        private String location = "";
        private String description = "";
        private String requiredSkills = "";
        private String salary = "";
        private String url = "";
        private String employmentType = "";
        private String experienceLevel = "";
        private String workplaceType = "";
        private String postedDate = "";
        private String applicationDeadline = "";
        private String benefits = "";
        private String companyDescription = "";

        // Builder methods for all fields
        public Builder setTitle(String title) {
            this.title = title != null ? title : "";
            return this;
        }

        public Builder setCompany(String company) {
            this.company = company != null ? company : "";
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location != null ? location : "";
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description != null ? description : "";
            return this;
        }

        public Builder setRequiredSkills(String requiredSkills) {
            this.requiredSkills = requiredSkills != null ? requiredSkills : "";
            return this;
        }

        public Builder setSalary(String salary) {
            this.salary = salary != null ? salary : "";
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url != null ? url : "";
            return this;
        }

        public Builder setEmploymentType(String employmentType) {
            this.employmentType = employmentType != null ? employmentType : "";
            return this;
        }

        public Builder setExperienceLevel(String experienceLevel) {
            this.experienceLevel = experienceLevel != null ? experienceLevel : "";
            return this;
        }

        public Builder setWorkplaceType(String workplaceType) {
            this.workplaceType = workplaceType != null ? workplaceType : "";
            return this;
        }

        public Builder setPostedDate(String postedDate) {
            this.postedDate = postedDate != null ? postedDate : "";
            return this;
        }

        public Builder setApplicationDeadline(String applicationDeadline) {
            this.applicationDeadline = applicationDeadline != null ? applicationDeadline : "";
            return this;
        }

        public Builder setBenefits(String benefits) {
            this.benefits = benefits != null ? benefits : "";
            return this;
        }

        public Builder setCompanyDescription(String companyDescription) {
            this.companyDescription = companyDescription != null ? companyDescription : "";
            return this;
        }

        public JobOffer build() {
            return new JobOffer(this);
        }
    }
}