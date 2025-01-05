package jobanalysis.db;

import java.util.List;

import jobanalysis.models.JobOffer;

public class TestDatabaseConnection {
    public static void main(String[] args) {
        try {
            System.out.println("Test de connexion à MongoDB...");
            MongoDBConfig dbConfig = MongoDBConfig.getInstance();
            
            JobRepository repository = new JobRepository();
            
            JobOffer testOffer = new JobOffer(
                "Développeur Java",
                "Test Company",
                "Description du poste test",
                "Casablanca",
                "http://example.com"
            );
            
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