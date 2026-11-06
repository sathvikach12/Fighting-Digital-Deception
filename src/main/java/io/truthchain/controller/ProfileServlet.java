package io.truthchain.controller;

import io.truthchain.dao.UserDao;
import io.truthchain.model.User;
import io.truthchain.service.AuthService;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{User current=(User)req.getSession().getAttribute("user");req.setAttribute("profile",new UserDao().findById(current.id()));}catch(Exception ex){req.setAttribute("dataError","Could not load profile.");}view(req,resp,"profile");}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(!csrf(req,resp))return;try{new AuthService().changePassword(Web.userId(req),req.getParameter("currentPassword"),req.getParameter("newPassword"));Web.flash(req,"Password updated.");}catch(Exception ex){fail(req,ex);}resp.sendRedirect(req.getContextPath()+"/profile");}
}
