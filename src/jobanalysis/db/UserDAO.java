package jobanalysis.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import jobanalysis.models.User;

public class UserDAO {
    private MongoCollection<Document> collection;

    public UserDAO() {
        MongoDatabase database = MongoDBConfig.getInstance().getDatabase();
        this.collection = database.getCollection("users");
    }

    public boolean createUser(User user) {
        try {
            
            if (findByUsername(user.getUsername()) != null) {
                return false;
            }

            Document doc = new Document()
                .append("username", user.getUsername())
                .append("email", user.getEmail())
                .append("password", user.getPassword());

            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findByUsername(String username) {
        Document doc = collection.find(Filters.eq("username", username)).first();
        if (doc == null) return null;
        
        return new User(
            doc.getString("username"),
            doc.getString("email"),
            doc.getString("password")
        );
    }

    public boolean validateUser(String username, String password) {
        User user = findByUsername(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }
} 	