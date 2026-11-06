package io.truthchain.controller;

import io.truthchain.util.Csrf;
import io.truthchain.util.Web;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    protected void view(HttpServletRequest req,HttpServletResponse resp,String name)throws ServletException,IOException{
        req.setAttribute("csrf",Csrf.token(req.getSession(true)));req.setAttribute("flash",Web.takeFlash(req));req.setAttribute("ctx",req.getContextPath());
        req.setAttribute("pageTitle",switch(name){case "home"->"News integrity";case "login"->"Sign in";case "register"->"Create account";case "dashboard"->"Dashboard";case "search"->"Explore news";case "news-admin"->"Manage news";case "categories"->"Categories";case "users"->"User access";case "moderation"->"Moderation";case "settings"->"Settings";case "ledger"->"Ledger explorer";case "transactions"->"Search history";case "analytics"->"Analytics";case "profile"->"Profile";default->"Request unavailable";});
        req.getRequestDispatcher("/WEB-INF/views/"+name+".jsp").forward(req,resp);
    }
    protected boolean csrf(HttpServletRequest req,HttpServletResponse resp)throws IOException{
        if(!Csrf.valid(req)){resp.sendError(403,"The form expired. Please reload and try again.");return false;}return true;
    }
    protected String clean(String value){return value==null?"":value.trim();}
    protected void fail(HttpServletRequest req,Exception ex){Web.flash(req,ex instanceof IllegalArgumentException?ex.getMessage():"The request could not be completed. Check the input and try again.");}
}
