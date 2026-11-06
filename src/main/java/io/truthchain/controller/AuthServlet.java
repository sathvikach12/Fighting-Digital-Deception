package io.truthchain.controller;

import io.truthchain.model.User;
import io.truthchain.service.AuthService;
import io.truthchain.util.Csrf;
import io.truthchain.util.Web;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns={"/login","/register","/logout"})
public class AuthServlet extends BaseServlet {
    private final AuthService auth=new AuthService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{
        if(req.getServletPath().equals("/logout")){resp.sendError(405);return;}
        if(req.getSession(false)!=null&&req.getSession(false).getAttribute("user")!=null){resp.sendRedirect(req.getContextPath()+"/dashboard");return;}
        view(req,resp,req.getServletPath().equals("/login")?"login":"register");
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException,jakarta.servlet.ServletException{
        if(!csrf(req,resp))return;
        String path=req.getServletPath();
        try{
            if(path.equals("/logout")){req.getSession().invalidate();resp.sendRedirect(req.getContextPath()+"/");return;}
            if(path.equals("/register")){auth.register(clean(req.getParameter("name")),clean(req.getParameter("email")),req.getParameter("password"));req.getSession().removeAttribute("csrfToken");Web.flash(req,"Registration received. Sign in after an administrator approves your account.");resp.sendRedirect(req.getContextPath()+"/login");return;}
            User user=auth.login(clean(req.getParameter("email")),req.getParameter("password"));
            if(user==null||"INACTIVE".equals(user.status())){Web.flash(req,"Email or password was not recognized.");resp.sendRedirect(req.getContextPath()+"/login");return;}
            req.changeSessionId();req.getSession().setAttribute("user",user);Csrf.token(req.getSession());resp.sendRedirect(req.getContextPath()+"/dashboard");
        }catch(Exception ex){fail(req,ex);resp.sendRedirect(req.getContextPath()+path);}
    }
}
