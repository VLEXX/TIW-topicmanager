package it.controllers;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "LogoutHandler", value = "/logout")
public class LogoutHandler extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(request.getSession(false)!=null){
            request.getSession().invalidate();
        }
        String login = "/login";
        response.sendRedirect(request.getContextPath()+login);
        System.out.println("LogoutHandler: utente sloggato -> redirect to LoginHandler");
    }

}
