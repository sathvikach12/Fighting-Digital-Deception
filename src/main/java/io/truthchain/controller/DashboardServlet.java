package io.truthchain.controller;

import io.truthchain.dao.*;
import io.truthchain.model.User;
import io.truthchain.service.NewsService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final NewsService news=new NewsService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{
        User user=(User)req.getSession().getAttribute("user");
        try{req.setAttribute("categories",new CategoryDao().all());req.setAttribute("articles",news.search("",null,"ADMIN".equals(user.role())));req.setAttribute("popular",news.popular(5));req.setAttribute("counts",news.countsByCategory());req.setAttribute("quantizationDate",new SettingsDao().quantizationDate());}
        catch(Exception ex){req.setAttribute("dataError","Database is not available. Follow the setup steps in README.md.");}
        view(req,resp,"dashboard");
    }
}
