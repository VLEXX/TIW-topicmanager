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
        //TODO aggiungere funzioni per modificare posizione o aggiungere topic
        try {
            Connection c = DBConnectionSupplier.getConnection();
            TopicDAO td = new TopicDAO(c);
            ArrayList<Integer> rootlist = new ArrayList<>(td.findChildrenIdById(null));
            ArrayList<TopicBean> roottopiclist = new ArrayList<>();
            for(Integer i : rootlist){
                roottopiclist.add(new TopicBean(td,i,null,Integer.toString(rootlist.indexOf(i)+1)));
            }
            c.close();
            if(rootlist.size()==roottopiclist.size())
                System.out.println("HomeHandler: elenco dei topic letto correttamente dal DB, l'elenco radice contiene "+roottopiclist.size()+" elementi");
            else
                System.out.println("HomeHandler: errore durante la costruzione dell'elenco, "+roottopiclist.size() +"elementi aggiunti in radice su "+rootlist.size()+" elementi letti");
            TemplateEngineManager tem = new TemplateEngineManager(this.getServletContext());
            WebContext webctx = new WebContext(request, response, this.getServletContext(), request.getLocale());
            webctx.setVariable("username", ((UserBean)request.getSession().getAttribute("user")).getUsername());
            webctx.setVariable("rootTopicBeanAsList",roottopiclist);
            tem.getTemplateEngine().process("Home", webctx, response.getWriter());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

}
