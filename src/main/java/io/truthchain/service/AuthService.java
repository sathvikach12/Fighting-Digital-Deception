package io.truthchain.service;

import io.truthchain.dao.UserDao;
import io.truthchain.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class AuthService {
    private final UserDao users=new UserDao();
    public User login(String email,String password)throws SQLException{
        if(email==null||password==null||password.length()>72)return null;
        User user=users.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT));
        if(user==null||!BCrypt.checkpw(password,users.passwordHash(user)))return null;
        return user;
    }
    public void register(String name,String email,String password)throws SQLException{
        if(!io.truthchain.util.Validation.required(name,120)||!io.truthchain.util.Validation.email(email)||!io.truthchain.util.Validation.strongEnoughPassword(password))throw new IllegalArgumentException("Check your name, email, and password length (10–72 characters).");
        if(users.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))!=null)throw new IllegalArgumentException("An account with that email already exists.");
        users.create(name.trim(),email.trim().toLowerCase(java.util.Locale.ROOT),BCrypt.hashpw(password,BCrypt.gensalt(12)));
    }
    public void changePassword(long userId,String currentPassword,String newPassword)throws SQLException{
        User user=users.findById(userId);if(user==null)throw new IllegalArgumentException("Account not found.");String existing=users.passwordHash(user);
        if(currentPassword==null||!BCrypt.checkpw(currentPassword,existing))throw new IllegalArgumentException("Current password was not recognized.");
        if(!io.truthchain.util.Validation.strongEnoughPassword(newPassword))throw new IllegalArgumentException("New password must be 10–72 characters.");
        users.updatePassword(userId,BCrypt.hashpw(newPassword,BCrypt.gensalt(12)));
    }
}
