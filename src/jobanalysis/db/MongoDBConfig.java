package jobanalysis.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoException;

public class MongoDBConfig {
    private static MongoDBConfig instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    // Constantes pour la configuration
    private static final String HOST = "localhost";
    private static final int PORT = 27017;
    private static final String DATABASE_NAME = "jobsAnalysisDB";
    private static final String CONNECTION_STRING = "mongodb://" + HOST + ":" + PORT;

    private MongoDBConfig() {
        try {
            // Initialiser la connexion
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
        } catch (MongoException e) {
            System.err.println("Erreur de connexion à MongoDB: " + e.getMessage());
            throw e;
        }
    }

    public static MongoDBConfig getInstance() {
        if (instance == null) {
            instance = new MongoDBConfig();
        }
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}