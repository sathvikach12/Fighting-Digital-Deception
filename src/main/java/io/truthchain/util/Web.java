package io.truthchain.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Web {
    private Web() { }
    public static HttpSession session(HttpServletRequest req){return req.getSession(true);}
    public static void flash(HttpServletRequest req,String message){session(req).setAttribute("flash",message);}
    public static String takeFlash(HttpServletRequest req){Object value=session(req).getAttribute("flash");session(req).removeAttribute("flash");return value==null?null:value.toString();}
    public static long userId(HttpServletRequest req){io.truthchain.model.User user=(io.truthchain.model.User)session(req).getAttribute("user");return user.id();}
    public static Long nullableId(String value){try{return value==null||value.isBlank()?null:Long.valueOf(value);}catch(NumberFormatException ex){return null;}}
}
