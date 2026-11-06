package io.truthchain.controller;

import io.truthchain.dao.SearchDao;
import io.truthchain.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns={"/transactions","/transactions/system"})
public class TransactionsServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{User user=(User)req.getSession().getAttribute("user");req.setAttribute("records",new SearchDao().recent(user.id(),"/transactions/system".equals(req.getServletPath())));}catch(Exception ex){req.setAttribute("dataError","Could not load search history.");}view(req,resp,"transactions");}
}
