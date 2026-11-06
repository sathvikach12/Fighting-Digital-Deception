package io.truthchain.controller;

import io.truthchain.dao.LedgerDao;
import io.truthchain.util.Validation;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns={"/ledger","/verify"})
public class LedgerServlet extends BaseServlet {
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{try{LedgerDao dao=new LedgerDao();req.setAttribute("blocks",dao.all());req.setAttribute("issues",dao.verify());String id=req.getParameter("id");if(id!=null&&!id.isBlank()&&!Validation.positiveId(id)){resp.sendError(400,"Invalid article id");return;}if(Validation.positiveId(id)){req.setAttribute("verifyId",id);req.setAttribute("articleVerified",dao.verifyArticle(Long.parseLong(id)));}}catch(Exception ex){req.setAttribute("dataError","Ledger verification could not reach the database.");req.setAttribute("blocks",java.util.List.of());req.setAttribute("issues",java.util.List.of("Database unavailable"));}view(req,resp,"ledger");}
}
