package io.truthchain.dao;

import io.truthchain.config.Database;
import io.truthchain.model.Article;
import io.truthchain.service.ArticleText;
import io.truthchain.service.Quantization;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewsDao {
    private final LedgerDao ledger=new LedgerDao();
    private final SettingsDao settings=new SettingsDao();

    public List<Article> search(String query,Long categoryId,boolean includeDeleted)throws SQLException{
        String sql="SELECT n.id,n.category_id,c.name category_name,n.title,n.description,n.publisher,n.published_at,n.view_count,n.version,COALESCE(m.status,'CLEAR') moderation_status,n.is_deleted FROM news n JOIN categories c ON c.id=n.category_id LEFT JOIN moderation_records m ON m.article_id=n.id WHERE (?='' OR n.title LIKE ? OR n.description LIKE ?) AND (? IS NULL OR n.category_id=?) AND (?=1 OR n.is_deleted=0) ORDER BY n.published_at DESC,n.id DESC";
        List<Article> out=new ArrayList<>();String term=query==null?"":query.trim();
        try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,term);p.setString(2,"%"+term+"%");p.setString(3,"%"+term+"%");if(categoryId==null){p.setNull(4,Types.BIGINT);p.setNull(5,Types.BIGINT);}else{p.setLong(4,categoryId);p.setLong(5,categoryId);}p.setInt(6,includeDeleted?1:0);try(ResultSet r=p.executeQuery()){LocalDate cutoff=settings.quantizationDate();while(r.next()){LocalDate date=r.getDate("published_at").toLocalDate();out.add(new Article(r.getLong("id"),r.getLong("category_id"),r.getString("category_name"),r.getString("title"),r.getString("description"),r.getString("publisher"),date,r.getLong("view_count"),r.getInt("version"),r.getString("moderation_status"),r.getBoolean("is_deleted"),Quantization.isTrending(date,cutoff)));}}}return out;
    }
    public Article find(long id)throws SQLException{if(id<=0)return null;for(Article a:search("",null,true))if(a.id()==id)return a;return null;}
    public long save(long id,long category,String title,String description,String publisher,LocalDate date,long editorId)throws SQLException{
        try(Connection c=Database.getConnection()){
            c.setAutoCommit(false);try{
                int version;boolean exists=id>0;
                if(category<=0)throw new SQLException("Invalid article category");
                if(exists){try(PreparedStatement q=c.prepareStatement("SELECT version FROM news WHERE id=? FOR UPDATE")){q.setLong(1,id);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("Article not found");version=r.getInt(1)+1;}}}
                else version=1;
                String hash=ArticleText.hash(category,title,description,publisher,date.toString());
                if(exists){
                    try(PreparedStatement q=c.prepareStatement("INSERT INTO news_versions(article_id,version,category_id,title,description,publisher,published_at,is_deleted,content_hash,edited_by) SELECT id,version,category_id,title,description,publisher,published_at,is_deleted,content_hash,? FROM news WHERE id=?")){q.setLong(1,editorId);q.setLong(2,id);q.executeUpdate();}
                    try(PreparedStatement q=c.prepareStatement("UPDATE news SET category_id=?,title=?,description=?,publisher=?,published_at=?,version=?,content_hash=?,is_deleted=0,updated_at=CURRENT_TIMESTAMP WHERE id=?")){q.setLong(1,category);q.setString(2,title);q.setString(3,description);q.setString(4,publisher);q.setDate(5,Date.valueOf(date));q.setInt(6,version);q.setString(7,hash);q.setLong(8,id);q.executeUpdate();}
                }else{
                    try(PreparedStatement q=c.prepareStatement("INSERT INTO news(category_id,title,description,publisher,published_at,version,content_hash,created_by) VALUES(?,?,?,?,?,1,?,?)",Statement.RETURN_GENERATED_KEYS)){q.setLong(1,category);q.setString(2,title);q.setString(3,description);q.setString(4,publisher);q.setDate(5,Date.valueOf(date));q.setString(6,hash);q.setLong(7,editorId);q.executeUpdate();try(ResultSet keys=q.getGeneratedKeys()){keys.next();id=keys.getLong(1);}}
                }
                ledger.append(c,id,version,hash);c.commit();return id;
            }catch(Exception ex){c.rollback();if(ex instanceof SQLException se)throw se;throw new SQLException("Unable to save article",ex);}finally{c.setAutoCommit(true);}
        }
    }
    public void softDelete(long id,long editorId)throws SQLException{
        try(Connection c=Database.getConnection()){c.setAutoCommit(false);try{
            if(id<=0)throw new SQLException("Invalid article id");
            int version;try(PreparedStatement q=c.prepareStatement("SELECT version FROM news WHERE id=? FOR UPDATE")){q.setLong(1,id);try(ResultSet r=q.executeQuery()){if(!r.next())throw new SQLException("Article not found");version=r.getInt(1)+1;}}
            try(PreparedStatement q=c.prepareStatement("INSERT INTO news_versions(article_id,version,category_id,title,description,publisher,published_at,is_deleted,content_hash,edited_by) SELECT id,version,category_id,title,description,publisher,published_at,is_deleted,content_hash,? FROM news WHERE id=?")){q.setLong(1,editorId);q.setLong(2,id);q.executeUpdate();}
            try(PreparedStatement q=c.prepareStatement("UPDATE news SET is_deleted=1,version=?,content_hash='DELETED',updated_at=CURRENT_TIMESTAMP WHERE id=?")){q.setInt(1,version);q.setLong(2,id);q.executeUpdate();}
            ledger.append(c,id,version,"DELETED");c.commit();
        }catch(Exception ex){c.rollback();if(ex instanceof SQLException se)throw se;throw new SQLException(ex);}finally{c.setAutoCommit(true);}}
    }
    public void incrementViews(long id)throws SQLException{try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("UPDATE news SET view_count=view_count+1 WHERE id=? AND is_deleted=0")){p.setLong(1,id);p.executeUpdate();}}
    public List<Article> popular(int limit)throws SQLException{
        List<Article> all=search("",null,false);all.sort((a,b)->Long.compare(b.views(),a.views()));return all.size()>limit?new ArrayList<>(all.subList(0,limit)):all;
    }
    public java.util.Map<String,Long> countsByCategory()throws SQLException{
        java.util.Map<String,Long> map=new java.util.LinkedHashMap<>();try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement("SELECT c.name,COUNT(n.id) total FROM categories c LEFT JOIN news n ON n.category_id=c.id AND n.is_deleted=0 GROUP BY c.id,c.name ORDER BY c.name");ResultSet r=p.executeQuery()){while(r.next())map.put(r.getString(1),r.getLong(2));}return map;
    }
}
