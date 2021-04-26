package it.controllers;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccessFilter implements Filter {
    public void init(FilterConfig config) throws ServletException {
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if(request.getSession(false)==null || request.getSession().getAttribute("user") == null) {
            RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/AccessDenied.html");
            rd.forward(request, response);
            System.out.println("AccessFilter: accesso bloccato -> dispatching AccessDenied.html");
        }else{
            chain.doFilter(req,res);
        }
    }
}
