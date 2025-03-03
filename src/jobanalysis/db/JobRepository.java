package jobanalysis.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import jobanalysis.models.JobOffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class JobRepository {
    private MongoCollection<Document> collection;

    public JobRepository() {
        this.collection = MongoDBConfig.getInstance()
                .getDatabase()
                .getCollection("jobOffers");
    }

    public void saveJobOffer(JobOffer offer) {
        try {
            Document doc = new Document()
                    .append("title", offer.getTitle())
                    .append("company", offer.getCompany())
                    .append("description", offer.getDescription())
                    .append("location", offer.getLocation())
                    .append("requiredSkills", offer.getRequiredSkills())
                    .append("salary", offer.getSalary())
                    .append("sourceUrl", offer.getUrl())  // Ensure key matches retrieval
                    .append("employmentType", offer.getEmploymentType())
                    .append("experienceLevel", offer.getExperienceLevel())
                    .append("workplaceType", offer.getWorkplaceType())
                    .append("postedDate", offer.getPostedDate())
                    .append("applicationDeadline", offer.getApplicationDeadline())
                    .append("benefits", offer.getBenefits())
                    .append("companyDescription", offer.getCompanyDescription())
                    .append("scrapedDate", new Date());

            collection.insertOne(doc);
            System.out.println("Successfully saved job offer: " + offer.getTitle());
        } catch (Exception e) {
            System.err.println("Error saving job offer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<JobOffer> getAllJobOffers() {
        List<JobOffer> offers = new ArrayList<>();
        collection.find().forEach(doc -> {
            JobOffer offer = new JobOffer.Builder()
                    .setTitle(doc.getString("title"))
                    .setCompany(doc.getString("company"))
                    .setDescription(doc.getString("description"))
                    .setLocation(doc.getString("location"))
                    .setUrl(doc.getString("sourceUrl"))
                    .build();
            offers.add(offer);
        });
        return offers;
    }
}