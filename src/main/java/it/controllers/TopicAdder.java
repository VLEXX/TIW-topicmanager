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

@WebServlet(name = "TopicAdder", value = "/areapersonale/addtopic")
public class TopicAdder extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String topicname = request.getParameter("topicname");
        String ftopicname = request.getParameter("fathertopicname");
        TemplateEngineManager tem = new TemplateEngineManager(this.getServletContext());
        WebContext webctx = new WebContext(request, response, this.getServletContext(), request.getLocale());
        webctx.setVariable("username", ((UserBean)request.getSession().getAttribute("user")).getUsername());


    //caso 1a: topicname nullo o vuoto
        if(topicname == null || topicname.isBlank() || !topicname.matches("^[a-zA-Z0-9 ]*$") || topicname.contains("  ") || topicname.length() > 255){
            System.out.println("TopicAdder: i dati non sono validi -> dispatching edited Home.html");
            webctx.setVariable("errore", "<p style=\"color:red;\">E' obbligatorio specificare la nuova categoria e deve essere alfanumerica (spazi ammessi ma non consecutivi) e puo' avere un massimo di 255 caratteri</p>");
        }else{
            Connection c = null;
            try {
                c = DBConnectionSupplier.getConnection();
                TopicDAO td = new TopicDAO(c);
                Integer resultid = td.findIdByTopic(topicname);
                //caso 1b/2a: topicname valido ma esiste già nel db
                if(resultid!=null){
                    System.out.println("TopicAdder: categoria già presente nel DB -> dispatching edited Home.html");
                    webctx.setVariable("errore", "<p style=\"color:red;\">La categoria inserita esiste gia'</p>");
                }else{
                    //caso 1b/2b/3a: categoria padre non nulla
                    if(ftopicname!=null && !ftopicname.isBlank()){
                        resultid = td.findIdByTopic(ftopicname);
                        //caso 1b/2b/3a/4a: categoria padre inesistente
                        if(resultid==null){
                            System.out.println("TopicAdder: la categoria padre specificata non esiste -> dispatching edited Home.html");
                            webctx.setVariable("errore", "<p style=\"color:red;\">Categoria padre specificata inesistente</p>");
                        }else{

                            ArrayList<Integer> children = td.findChildrenIdById(resultid);
                            //caso 1b/2b/3a/4b/5a: categoria padre esistente ma limite figli superato
                            if(children.size()>=9){
                                System.out.println("TopicAdder: limite massimo di 9 sottocategorie superato -> dispatching edited Home.html");
                                webctx.setVariable("errore", "<p style=\"color:red;\">Impossibile aggiungere piu' di 9 sottocategorie</p>");
                            }else{
                                System.out.println("TopicAdder: inserimento nuova categorie consentito");
                                td.insertNewTopic(topicname,resultid,children.size()+1);
                                webctx.setVariable("errore", "<p style=\"color:green;\">Categoria '"+topicname+"' inserita con successo.</p>");
                            }

                        }

                    //caso 1b/2b/3b: categoria padre nulla (radice)
                    }else{
                        ArrayList<Integer> children = td.findChildrenIdById(resultid);
                        //caso 1b/2b/3b/6a:  limite figli alla radice superato
                        if(children.size()>=9){
                            System.out.println("TopicAdder: limite massimo di 9 sottocategorie superato (radice) -> dispatching edited Home.html");
                            webctx.setVariable("errore", "<p style=\"color:red;\">Impossibile aggiungere piu' di 9 sottocategorie</p>");
                        }else{
                            System.out.println("TopicAdder: inserimento nuova categorie consentito (radice)");
                            td.insertNewTopic(topicname,resultid,children.size()+1);
                            webctx.setVariable("errore", "<p style=\"color:green;\">Categoria '"+topicname+"' inserita con successo.</p>");
                        }

                    }
                }
                c.close();


            } catch (SQLException throwables) {
                throwables.printStackTrace();
                webctx.setVariable("DBerror","<br>Il servizio non è al momento disponibile, riprovare più tardi<br>");
                tem.getTemplateEngine().process("Home", webctx, response.getWriter());
            }


        }
        TopicDAO td2 = null;
        try {
            Connection c2 = DBConnectionSupplier.getConnection();
            td2 = new TopicDAO(c2);
            ArrayList<TopicBean> roottopiclist = td2.treeGenerator();
            c2.close();
            webctx.setVariable("rootTopicBeanAsList",roottopiclist);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            webctx.setVariable("DBerror","<br>Il servizio non è al momento disponibile, riprovare più tardi<br>");

        } finally{
            tem.getTemplateEngine().process("Home", webctx, response.getWriter());
        }
    }
}
