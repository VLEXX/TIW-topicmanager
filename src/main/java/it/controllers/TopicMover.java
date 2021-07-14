package it.controllers;

import it.beans.TopicBean;
import it.beans.UserBean;
import it.dao.TopicDAO;
import org.thymeleaf.context.WebContext;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet(name = "TopicMover", value = "/areapersonale/move")
public class TopicMover extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TemplateEngineManager tem = new TemplateEngineManager(this.getServletContext());
        WebContext webctx = new WebContext(request, response, this.getServletContext(), request.getLocale());
        webctx.setVariable("username", ((UserBean)request.getSession().getAttribute("user")).getUsername());
        String topictbm = request.getParameter("topic");
        Integer id = null;
        ArrayList<TopicBean> root = new ArrayList<>();
        try{
            Connection c = DBConnectionSupplier.getConnection();
            TopicDAO td = new TopicDAO(c);
            root.addAll(td.treeGenerator());
            c.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            webctx.setVariable("DBerror","<br>Il servizio non è al momento disponibile, riprovare più tardi, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per tornare alla home.<br>");
            tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
        }
        if(topictbm==null||topictbm.isBlank()){
            webctx.setVariable("DBerror","<br>Richiesta non valida, topic vuoto o inesistente, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per tornare alla home.<br>");
            System.out.println("TopicMover: richiesta pervenuta senza topic o con topic vuoto");
        }else{
            try{
                Connection c = DBConnectionSupplier.getConnection();
                TopicDAO td = new TopicDAO(c);
                id = td.findIdByTopic(topictbm);
                c.close();
            } catch (SQLException e) {
                e.printStackTrace();
                webctx.setVariable("DBerror","<br>Il servizio non è al momento disponibile, riprovare più tardi<br>");
                tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
            }
            if(id==null){
                System.out.println("TopicMover: topic inesistente");
                webctx.setVariable("DBerror","<br>Il topic selezionato non esiste, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per tornare alla home.<br>");
            }else{
                System.out.println("TopicMover: il topic selezionato esiste");
                webctx.setVariable("rootTopicBeanAsList",root);
                webctx.setVariable("rootinsertion","<a href=\"/gruppo33/areapersonale/moveto?src="+topictbm+"\" >sposta nella radice</a>");
                webctx.setVariable("topictbm",topictbm);

            }

        }
        tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
    }

}
