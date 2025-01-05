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
        Document doc = new Document()
            .append("title", offer.getTitle())
            .append("company", offer.getCompany())
            .append("description", offer.getDescription())
            .append("location", offer.getLocation())
            .append("scrapedDate", new Date())
            .append("sourceUrl", offer.getSourceUrl());

        collection.insertOne(doc);
    }

    public List<JobOffer> getAllJobOffers() {
        List<JobOffer> offers = new ArrayList<>();
        collection.find().forEach(doc -> {
            JobOffer offer = new JobOffer(
                doc.getString("title"),
                doc.getString("company"),
                doc.getString("description"),
                doc.getString("location"),
                doc.getString("sourceUrl")
            );
            offers.add(offer);
        });
        return offers;
    }
}