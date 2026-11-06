package io.truthchain.controller;

import io.truthchain.dao.CategoryDao;
import io.truthchain.util.Validation;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/categories")
public class CategoryServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{req.setAttribute("categories",new CategoryDao().all());}catch(Exception ex){req.setAttribute("dataError","Could not load categories.");}view(req,resp,"categories");}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(!csrf(req,resp))return;try{CategoryDao dao=new CategoryDao();if("delete".equals(req.getParameter("action")))dao.delete(Long.parseLong(req.getParameter("id")));else{String name=clean(req.getParameter("name"));if(!Validation.required(name,80))throw new IllegalArgumentException("Category name is required (maximum 80 characters).");dao.create(name,clean(req.getParameter("description")));}Web.flash(req,"Category change saved. Categories used by news cannot be removed.");}catch(Exception ex){fail(req,ex);}resp.sendRedirect(req.getContextPath()+"/admin/categories");}
}
