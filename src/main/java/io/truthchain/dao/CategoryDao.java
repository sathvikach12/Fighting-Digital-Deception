package io.truthchain.dao;

import io.truthchain.config.Database;
import io.truthchain.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public List<Category> all()throws SQLException{
        List<Category> out=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,description FROM categories ORDER BY name");ResultSet r=p.executeQuery()){while(r.next())out.add(new Category(r.getLong(1),r.getString(2),r.getString(3)));}return out;
    }
    public void create(String name,String description)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO categories(name,description) VALUES(?,?)")){p.setString(1,name);p.setString(2,description);p.executeUpdate();}}
    public void delete(long id)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM categories WHERE id=? AND NOT EXISTS(SELECT 1 FROM news WHERE category_id=?)")){p.setLong(1,id);p.setLong(2,id);p.executeUpdate();}}
}
