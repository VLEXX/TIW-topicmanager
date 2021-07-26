package it.controllers;

import it.beans.UserBean;
import it.dao.TopicDAO;
import org.thymeleaf.context.WebContext;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "TopicSetter", value = "/areapersonale/moveto")
public class TopicSetter extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TemplateEngineManager tem = new TemplateEngineManager(this.getServletContext());
        WebContext webctx = new WebContext(request, response, this.getServletContext(), request.getLocale());
        webctx.setVariable("username", ((UserBean)request.getSession().getAttribute("user")).getUsername());
        //webctx.setVariable("backhome","<a href=\"/gruppo33/areapersonale/home\">Clicca qui</a> per annullare lo spostamento");
        String source = request.getParameter("src");
        String dest = request.getParameter("dest");
        if(source==null||source.isBlank() || source.length()>255){
            System.out.println("TopicSetter: la categoria da spostare non è stata definita correttamente");
            webctx.setVariable("DBerror","<br>Richiesta non valida, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a>per tornare alla home.<br>");
            tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
        }else{
            Integer src = null, dst = null;
            try{
                Connection c = DBConnectionSupplier.getConnection();
                TopicDAO td = new TopicDAO(c);
                src = td.findIdByTopic(source);
                if(dest!=null && dest.length()<256)
                    dst = td.findIdByTopic(dest);
                if(src == null || (dst==null && dest != null)){
                    System.out.println("TopicSetter: la categoria da spostare o la destinazione scelta non esistono");
                    webctx.setVariable("DBerror","<br>la categoria da spostare o la destinazione scelta non esistono, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per annullare lo spostamento.<br>");
                    tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
                }else {
                    if ((dest!=null && td.getFatherHierarcy(dst).contains(src)) || dst == src) {
                        System.out.println("TopicSetter: impossibile spostare, la destinazione scelta coincide con o e' sottocategoria della categoria da spostare");
                        webctx.setVariable("DBerror", "<br>la destinazione scelta non e' valida in quanto coincide con o è sottocategoria della categoria da spostare, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per annullare lo spostamento.<br>");
                        tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
                    }else{
                    int success = td.changeFatherTo(src, dst);
                    if (success == 1) {
                        System.out.println("TopicSetter: la destinazione ha gia' 9 sottocategorie");
                        webctx.setVariable("DBerror", "<br>la destinazione ha gia' 9 sottocategorie, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per annullare lo spostamento.<br>");
                        tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
                    } else {
                        System.out.println("TopicSetter: spostamento eseguito con successo -> redirect a home");
                        response.sendRedirect(request.getContextPath() + "/areapersonale/home");
                    }
                }
                }
                c.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                webctx.setVariable("DBerror","<br>Il servizio al momento non e' disponibile, <a href=\"/gruppo33/areapersonale/home\">clicca qui</a> per tornare alla home.<br>");
                tem.getTemplateEngine().process("HomeDestination", webctx, response.getWriter());
            }

        }
    }


}
