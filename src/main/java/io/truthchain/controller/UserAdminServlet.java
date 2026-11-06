package io.truthchain.controller;

import io.truthchain.dao.UserDao;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/users")
public class UserAdminServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{req.setAttribute("users",new UserDao().all());}catch(Exception ex){req.setAttribute("dataError","Could not load users.");}view(req,resp,"users");}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(!csrf(req,resp))return;try{String status=clean(req.getParameter("status"));if(!status.equals("ACTIVE")&&!status.equals("INACTIVE"))throw new IllegalArgumentException("Invalid account status.");new UserDao().setStatus(Long.parseLong(req.getParameter("id")),status);Web.flash(req,"User account status updated.");}catch(Exception ex){fail(req,ex);}resp.sendRedirect(req.getContextPath()+"/admin/users");}
}
