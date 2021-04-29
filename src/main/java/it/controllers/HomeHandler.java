package it.controllers;

import it.beans.TopicBean;
import it.beans.UserBean;
import it.dao.TopicDAO;
import org.thymeleaf.context.WebContext;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "HomeHandler", value = "/areapersonale/home")
public class HomeHandler extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TemplateEngineManager tem = new TemplateEngineManager(this.getServletContext());
        WebContext webctx = new WebContext(request, response, this.getServletContext(), request.getLocale());
        webctx.setVariable("username", ((UserBean)request.getSession().getAttribute("user")).getUsername());
        try {
            Connection c = DBConnectionSupplier.getConnection();
            TopicDAO td = new TopicDAO(c);
            ArrayList<TopicBean> roottopiclist = td.treeGenerator();
            c.close();
            webctx.setVariable("rootTopicBeanAsList",roottopiclist);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            webctx.setVariable("DBerror","<br>Il servizio non è al momento disponibile, riprovare più tardi<br>");
        } finally {
            tem.getTemplateEngine().process("Home", webctx, response.getWriter());
        }

    }

}
