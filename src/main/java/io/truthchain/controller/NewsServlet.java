package io.truthchain.controller;

import io.truthchain.dao.CategoryDao;
import io.truthchain.dao.NewsDao;
import io.truthchain.model.Article;
import io.truthchain.service.NewsService;
import io.truthchain.util.Validation;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/admin/news")
public class NewsServlet extends BaseServlet {
    private final NewsService news=new NewsService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{
        try{req.setAttribute("categories",new CategoryDao().all());req.setAttribute("articles",news.search("",null,true));String edit=req.getParameter("edit");if(edit!=null&&!edit.isBlank()&&!Validation.positiveId(edit)){resp.sendError(400,"Invalid article id");return;}if(Validation.positiveId(edit)){Article a=new NewsDao().find(Long.parseLong(edit));if(a==null){resp.sendError(404);return;}req.setAttribute("editArticle",a);}}
        catch(Exception ex){req.setAttribute("dataError","Could not load news management data.");}view(req,resp,"news-admin");
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        if(!csrf(req,resp))return;String action=clean(req.getParameter("action"));
        try{long admin=Web.userId(req);if(action.equals("delete")){String idValue=clean(req.getParameter("id"));if(!Validation.positiveId(idValue))throw new IllegalArgumentException("Select a valid article.");long id=Long.parseLong(idValue);news.delete(id,admin);Web.flash(req,"Article soft-deleted. Its ledger history is retained.");}else{
            String idValue=clean(req.getParameter("id"));long id=idValue.isBlank()?0:Long.parseLong(idValue);long category=Long.parseLong(req.getParameter("category"));String title=clean(req.getParameter("title")),description=clean(req.getParameter("description")),publisher=clean(req.getParameter("publisher")),date=clean(req.getParameter("publishedAt"));if(!Validation.date(date))throw new IllegalArgumentException("Enter a valid publication date.");
            news.save(id,category,title,description,publisher,LocalDate.parse(date),admin);Web.flash(req,"Article saved and recorded as a new ledger version.");}
        }catch(Exception ex){fail(req,ex);}resp.sendRedirect(req.getContextPath()+"/admin/news");
    }
}
