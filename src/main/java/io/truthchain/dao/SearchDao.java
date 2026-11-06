package io.truthchain.dao;

import io.truthchain.config.Database;
import io.truthchain.model.SearchRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchDao {
    public void record(long userId,String query,Long categoryId,int results)throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO search_history(user_id,query_text,category_id,result_count) VALUES(?,?,?,?)")){p.setLong(1,userId);p.setString(2,query);if(categoryId==null)p.setNull(3,Types.BIGINT);else p.setLong(3,categoryId);p.setInt(4,results);p.executeUpdate();}
    }
    public List<SearchRecord> recent(long userId,boolean all)throws SQLException{
        String sql="SELECT h.id,h.user_id,u.name,h.query_text,COALESCE(c.name,'All categories') category_name,h.result_count,h.searched_at FROM search_history h JOIN users u ON u.id=h.user_id LEFT JOIN categories c ON c.id=h.category_id "+(all?"":"WHERE h.user_id=? ")+"ORDER BY h.searched_at DESC LIMIT 250";
        List<SearchRecord> rows=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(!all)p.setLong(1,userId);try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new SearchRecord(r.getLong(1),r.getLong(2),r.getString(3),r.getString(4),r.getString(5),r.getInt(6),r.getTimestamp(7).toInstant()));}}return rows;
    }
}
