package io.truthchain.service;

import io.truthchain.dao.ModerationDao;
import io.truthchain.dao.NewsDao;
import io.truthchain.model.Article;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NewsService {
    private final NewsDao news=new NewsDao();private final ModerationDao moderation=new ModerationDao();
    public List<Article> search(String query,Long category,boolean includeDeleted)throws SQLException{return news.search(query,category,includeDeleted);}
    public long save(long id,long category,String title,String description,String publisher,LocalDate date,long editor)throws SQLException{
        if(!io.truthchain.util.Validation.required(title,240)||!io.truthchain.util.Validation.required(description,12000)||!io.truthchain.util.Validation.required(publisher,180)||category<=0)throw new IllegalArgumentException("Complete all article fields within the listed limits.");
        long saved=news.save(id,category,title.trim(),description.trim(),publisher.trim(),date,editor);moderation.refreshForArticle(saved,title,description);return saved;
    }
    public void delete(long id,long editor)throws SQLException{news.softDelete(id,editor);}
    public void viewed(long id)throws SQLException{news.incrementViews(id);}
    public List<Article> popular(int limit)throws SQLException{return news.popular(limit);}
    public java.util.Map<String,Long> countsByCategory()throws SQLException{return news.countsByCategory();}
}
