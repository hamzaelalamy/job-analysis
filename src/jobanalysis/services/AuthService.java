package jobanalysis.services;

import jobanalysis.db.UserDAO;
import jobanalysis.models.User;

public class AuthService {
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public boolean register(String username, String email, String password) {
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        User newUser = new User(username, email, password);
        return userDAO.createUser(newUser);
    }

    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        return userDAO.validateUser(username, password);
    }
}