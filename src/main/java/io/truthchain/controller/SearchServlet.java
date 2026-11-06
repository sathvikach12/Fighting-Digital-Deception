package io.truthchain.controller;

import io.truthchain.dao.*;
import io.truthchain.model.Article;
import io.truthchain.service.NewsService;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/search")
public class SearchServlet extends BaseServlet {
    private final NewsService service=new NewsService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{
        String q=clean(req.getParameter("q"));if(q.length()>255){resp.sendError(400,"Search terms must be 255 characters or fewer");return;}Long category=Web.nullableId(req.getParameter("category"));if(category==null&&!clean(req.getParameter("category")).isEmpty()){resp.sendError(400,"Invalid category filter");return;}
        try{List<Article> articles=service.search(q,category,false);if(req.getParameter("id")!=null){Long articleId=Web.nullableId(req.getParameter("id"));if(articleId!=null)service.viewed(articleId);}req.setAttribute("articles",articles);req.setAttribute("categories",new CategoryDao().all());req.setAttribute("query",q);req.setAttribute("selectedCategory",category);if(req.getParameter("q")!=null)new SearchDao().record(Web.userId(req),q,category,articles.size());}
        catch(Exception ex){req.setAttribute("dataError","Search is unavailable. Check the database connection.");}
        view(req,resp,"search");
    }
}
