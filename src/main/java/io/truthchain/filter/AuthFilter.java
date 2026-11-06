package io.truthchain.filter;

import io.truthchain.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest req=(HttpServletRequest)request;HttpServletResponse res=(HttpServletResponse)response;
        req.setCharacterEncoding("UTF-8");res.setCharacterEncoding("UTF-8");res.setHeader("X-Content-Type-Options","nosniff");res.setHeader("X-Frame-Options","DENY");res.setHeader("Referrer-Policy","strict-origin-when-cross-origin");res.setHeader("Content-Security-Policy","default-src 'self' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; script-src 'self' https://cdn.jsdelivr.net https://cdn.jsdelivr.net/npm/chart.js; img-src 'self' data:; frame-ancestors 'none'");
        String path=req.getRequestURI().substring(req.getContextPath().length());
        if(path.startsWith("/assets/")||path.equals("/login")||path.equals("/register")||path.equals("/logout")||path.equals("/")||path.equals("/health")){chain.doFilter(request,response);return;}
        if("POST".equalsIgnoreCase(req.getMethod())&&!io.truthchain.util.Csrf.valid(req)){res.sendError(HttpServletResponse.SC_FORBIDDEN,"The form expired. Reload and try again.");return;}
        jakarta.servlet.http.HttpSession session=req.getSession(false);
        User user=session!=null&&session.getAttribute("user") instanceof User u?u:null;
        boolean adminOnly=path.startsWith("/admin/")||path.equals("/ledger")||path.equals("/transactions/system")||path.equals("/analytics");
        boolean protectedPath=adminOnly||path.equals("/dashboard")||path.equals("/search")||path.equals("/transactions")||path.equals("/profile")||path.equals("/verify");
        if(!protectedPath){chain.doFilter(request,response);return;}
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if("PENDING".equals(user.status())&&!(path.equals("/dashboard")||path.equals("/profile"))){req.getSession().setAttribute("flash","Your account is awaiting administrator approval.");res.sendRedirect(req.getContextPath()+"/dashboard");return;}
        if(adminOnly&&(!"ADMIN".equals(user.role())||!"ACTIVE".equals(user.status()))){res.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        if(!"ACTIVE".equals(user.status())&&!(path.equals("/dashboard")||path.equals("/profile"))){res.sendError(HttpServletResponse.SC_FORBIDDEN);return;}
        chain.doFilter(request,response);
    }
}
