package io.truthchain.controller;

import io.truthchain.dao.SettingsDao;
import io.truthchain.util.Validation;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/admin/settings")
public class SettingsServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{req.setAttribute("date",new SettingsDao().quantizationDate());}catch(Exception ex){req.setAttribute("dataError","Could not load app settings.");}view(req,resp,"settings");}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(!csrf(req,resp))return;try{String date=clean(req.getParameter("date"));if(!Validation.date(date))throw new IllegalArgumentException("Enter a valid cutoff date.");new SettingsDao().setQuantizationDate(LocalDate.parse(date));Web.flash(req,"Quantization cutoff updated.");}catch(Exception ex){fail(req,ex);}resp.sendRedirect(req.getContextPath()+"/admin/settings");}
}
