package io.truthchain.controller;

import io.truthchain.service.NewsService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/analytics")
public class AnalyticsServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{NewsService service=new NewsService();req.setAttribute("counts",service.countsByCategory());req.setAttribute("popular",service.popular(10));}catch(Exception ex){req.setAttribute("dataError","Analytics could not load.");}view(req,resp,"analytics");}
}
