package it.controllers;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "LoginHandler", value = "/login")
public class LoginHandler extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //se la sessione non esiste ancora
        if(request.getSession(false)==null || request.getSession().getAttribute("user") == null) {
            String loginpage = "/WEB-INF/LoginFile.html";
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(loginpage);
            dispatcher.forward(request, response);
            System.out.println("LoginHandler: sessione o utente nulli -> dispatching LoginFile.html");
        } else {
            String homepage = "/areapersonale/home";
            response.sendRedirect(request.getContextPath()+homepage);
            System.out.println("LoginHandler: utente già loggato -> redirect to HomeHandler");
        }
    }

}
