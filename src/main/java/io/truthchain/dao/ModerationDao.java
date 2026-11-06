package io.truthchain.dao;

import io.truthchain.config.Database;
import io.truthchain.model.ModerationMatch;
import io.truthchain.service.SuspiciousMatcher;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModerationDao {
    public List<String> activeKeywords()throws SQLException{
        List<String> out=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT keyword FROM suspicious_keywords WHERE active=1 ORDER BY keyword");ResultSet r=p.executeQuery()){while(r.next())out.add(r.getString(1));}return out;
    }
    public List<String> detect(String title,String description)throws SQLException{return SuspiciousMatcher.matches(title+" "+description,activeKeywords());}
    public void refreshForArticle(long articleId,String title,String description)throws SQLException{
        List<String> hits=detect(title,description);try(Connection c=Database.getConnection()){
            c.setAutoCommit(false);try{
            try(PreparedStatement del=c.prepareStatement("DELETE FROM moderation_matches WHERE article_id=?")){del.setLong(1,articleId);del.executeUpdate();}
            String status=hits.isEmpty()?"CLEAR":"NEEDS_REVIEW";
            try(PreparedStatement up=c.prepareStatement("INSERT INTO moderation_records(article_id,status,reviewed_by,reviewed_at) VALUES(?,?,NULL,NULL) ON DUPLICATE KEY UPDATE status=IF(status='REVIEWED',status,VALUES(status)),reviewed_by=IF(status='REVIEWED',reviewed_by,NULL),reviewed_at=IF(status='REVIEWED',reviewed_at,NULL)")){up.setLong(1,articleId);up.setString(2,status);up.executeUpdate();}
            if(!hits.isEmpty())try(PreparedStatement ins=c.prepareStatement("INSERT INTO moderation_matches(article_id,keyword) VALUES(?,?)")){for(String hit:hits){ins.setLong(1,articleId);ins.setString(2,hit);ins.addBatch();}ins.executeBatch();}
            c.commit();
            }catch(SQLException ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }
    }
    public void review(long articleId,long adminId,String status,String notes)throws SQLException{
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO moderation_records(article_id,status,reviewed_by,reviewed_at,notes) VALUES(?,?,?,CURRENT_TIMESTAMP,?) ON DUPLICATE KEY UPDATE status=VALUES(status),reviewed_by=VALUES(reviewed_by),reviewed_at=VALUES(reviewed_at),notes=VALUES(notes)")){p.setLong(1,articleId);p.setString(2,status);p.setLong(3,adminId);p.setString(4,notes);p.executeUpdate();}
    }
    public List<ModerationMatch> queue()throws SQLException{
        String sql="SELECT n.id,n.title,COALESCE(GROUP_CONCAT(mm.keyword ORDER BY mm.keyword SEPARATOR ', '),'') matched,m.status,m.created_at FROM moderation_records m JOIN news n ON n.id=m.article_id LEFT JOIN moderation_matches mm ON mm.article_id=n.id WHERE m.status='NEEDS_REVIEW' GROUP BY n.id,n.title,m.status,m.created_at ORDER BY m.created_at DESC";
        List<ModerationMatch> out=new ArrayList<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next())out.add(new ModerationMatch(r.getLong(1),r.getString(2),r.getString(3),r.getString(4),r.getTimestamp(5).toInstant()));}return out;
    }
    public void addKeyword(String keyword,String reason)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO suspicious_keywords(keyword,reason,active) VALUES(?,?,1)")){p.setString(1,keyword);p.setString(2,reason);p.executeUpdate();}}
    public void deleteKeyword(long id)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM suspicious_keywords WHERE id=?")){p.setLong(1,id);p.executeUpdate();}}
}
