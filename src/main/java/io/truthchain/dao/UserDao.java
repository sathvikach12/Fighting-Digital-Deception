package io.truthchain.dao;

import io.truthchain.config.Database;
import io.truthchain.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id,name,email,password_hash,role,status,created_at FROM users WHERE email=?";
        try (Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,email); try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }
    public User findById(long id) throws SQLException {
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,email,password_hash,role,status,created_at FROM users WHERE id=?")) {
            p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }
    public String passwordHash(User user) throws SQLException {
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT password_hash FROM users WHERE id=?")) {
            p.setLong(1,user.id());try(ResultSet r=p.executeQuery()){if(!r.next())return null;return r.getString(1);}
        }
    }
    public void create(String name,String email,String passwordHash) throws SQLException {
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO users(name,email,password_hash,role,status) VALUES(?,?,?,'USER','PENDING')")) {
            p.setString(1,name);p.setString(2,email);p.setString(3,passwordHash);p.executeUpdate();
        }
    }
    public List<User> all() throws SQLException {
        List<User> out=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,email,password_hash,role,status,created_at FROM users ORDER BY created_at DESC");ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return out;
    }
    public void setStatus(long id,String status) throws SQLException {
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET status=? WHERE id=? AND role='USER'")){p.setString(1,status);p.setLong(2,id);p.executeUpdate();}
    }
    public void updatePassword(long id,String passwordHash)throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")){p.setString(1,passwordHash);p.setLong(2,id);p.executeUpdate();}
    }
    private User map(ResultSet r)throws SQLException{return new User(r.getLong("id"),r.getString("name"),r.getString("email"),r.getString("role"),r.getString("status"),r.getTimestamp("created_at").toInstant());}
}
