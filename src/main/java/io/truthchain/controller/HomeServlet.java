package io.truthchain.controller;

import io.truthchain.dao.NewsDao;
import io.truthchain.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet(urlPatterns={"/","/health"})
public class HomeServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException, jakarta.servlet.ServletException{
        if(req.getRequestURI().endsWith("/health")){resp.setContentType("text/plain");resp.getWriter().print("ok");return;}
        HttpSession session=req.getSession(false);User user=session==null?null:(User)session.getAttribute("user");
        if(user!=null){resp.sendRedirect(req.getContextPath()+"/dashboard");return;}
        try{req.setAttribute("articles",new NewsDao().search("",null,false).stream().limit(6).toList());}catch(Exception ignored){req.setAttribute("articles",java.util.List.of());}req.setAttribute("quantizationDate",LocalDate.now().minusDays(7));
        view(req,resp,"home");
    }
}
