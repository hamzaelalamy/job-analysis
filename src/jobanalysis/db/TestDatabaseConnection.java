package jobanalysis.db;

import java.util.List;
import jobanalysis.models.JobOffer;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        try {
            System.out.println("Test de connexion à MongoDB...");
            MongoDBConfig dbConfig = MongoDBConfig.getInstance();

            JobRepository repository = new JobRepository();

            // Using Builder pattern instead of constructor
            JobOffer testOffer = new JobOffer.Builder()
                    .setTitle("Développeur Java")
                    .setCompany("Test Company")
                    .setDescription("Description du poste test")
                    .setLocation("Casablanca")
                    .setUrl("http://example.com")
                    .build();

            repository.saveJobOffer(testOffer);
            System.out.println("Offre test sauvegardée avec succès");

            List<JobOffer> offers = repository.getAllJobOffers();
            System.out.println("Nombre d'offres dans la base: " + offers.size());

            System.out.println("Test terminé avec succès!");

        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}