package io.truthchain.controller;

import io.truthchain.dao.ModerationDao;
import io.truthchain.util.Validation;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/moderation")
public class ModerationServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{ModerationDao dao=new ModerationDao();req.setAttribute("queue",dao.queue());req.setAttribute("keywords",dao.activeKeywords());}catch(Exception ex){req.setAttribute("dataError","Could not load moderation data.");}view(req,resp,"moderation");}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{if(!csrf(req,resp))return;try{ModerationDao dao=new ModerationDao();String action=clean(req.getParameter("action"));if(action.equals("review")){String decision=clean(req.getParameter("decision"));if(!decision.equals("REVIEWED")&&!decision.equals("CLEAR"))throw new IllegalArgumentException("Invalid review decision.");dao.review(Long.parseLong(req.getParameter("id")),Web.userId(req),decision,clean(req.getParameter("notes")));}else if(action.equals("add")){String word=clean(req.getParameter("keyword"));if(!Validation.required(word,100))throw new IllegalArgumentException("Keyword is required.");dao.addKeyword(word,clean(req.getParameter("reason")));}else if(action.equals("delete"))dao.deleteKeyword(Long.parseLong(req.getParameter("id")));Web.flash(req,"Moderation settings saved. Keyword matches are review signals, not proof.");}catch(Exception ex){fail(req,ex);}resp.sendRedirect(req.getContextPath()+"/admin/moderation");}
}
