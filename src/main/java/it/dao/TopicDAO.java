package it.dao;

import it.beans.TopicBean;
import it.exceptions.TopicNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class TopicDAO {
    private final Connection c;

    public TopicDAO(Connection connection) {
        this.c = connection;
    }

    public String findTopicById(int id) throws SQLException {
        String query = "SELECT name FROM dbimagecat.categories WHERE Id = ?";
        ResultSet res = null;
        PreparedStatement p = null;
        String topic = null;
        try {
            p = c.prepareStatement(query);
            p.setInt(1, id);
            res = p.executeQuery();
            if(!res.next())
                throw new TopicNotFoundException();
            else
                topic = res.getString("name");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TopicNotFoundException e) {
            e.printStackTrace();
            topic = null;
        } finally {
            try {
                if(res!=null)
                    res.close();
                if(p!=null)
                    p.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
            return topic;
        }

    }

    public ArrayList<Integer> findChildrenIdById(Integer parid) throws SQLException {
        String query = "SELECT Id FROM dbimagecat.categories WHERE parentId = ? ORDER BY ChildrenOrder";
        String query2 = "SELECT Id FROM dbimagecat.categories WHERE parentId IS NULL ORDER BY ChildrenOrder";
        ResultSet res1 = null;
        PreparedStatement p=null;
        ArrayList<Integer> children = new ArrayList<>();
        try{
            if(parid != null && parid !=0) {
                p = c.prepareStatement(query);
                p.setInt(1, parid);
                res1 = p.executeQuery();
            }else {
                System.out.println("TopicDAO: ricerca id alla radice");
                p = c.prepareStatement(query2);
                res1 = p.executeQuery();
            }

            while(res1.next()){
                children.add(res1.getInt("Id"));
            }
            System.out.println("La ricerca ha prodotto "+children.size()+" risultati");
            res1.close();
            p.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return children;
    }

    public Integer findIdByTopic(String name) throws SQLException {
        String query = "SELECT Id FROM dbimagecat.categories WHERE name = ?";
        ResultSet res = null;
        PreparedStatement p = null;
        Integer id = null;
        try {
            p = c.prepareStatement(query);
            p.setString(1,name);
            res = p.executeQuery();
            if(!res.next())
                id = null;
            else
                id = res.getInt("Id");
        } catch (SQLException e) {
            e.printStackTrace();
            id = null;
        }  finally {
            try {
                if(res!=null)
                    res.close();
                if(p!=null)
                    p.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
            return id;
        }

    }

    public void insertNewTopic(String name, Integer parid, int position) throws SQLException {
        PreparedStatement p = null;
        if(parid!=null) {
            String query = "INSERT INTO dbimagecat.categories (name,parentId,childrenOrder) VALUES (?,?,?)";
            p = c.prepareStatement(query);
            p.setString(1, name);
            p.setInt(2, parid);
            p.setInt(3, position);
        }else{
            String query = "INSERT INTO dbimagecat.categories (name,childrenOrder) VALUES (?,?)";
            p = c.prepareStatement(query);
            p.setString(1, name);
            p.setInt(2, position);
        }
        int res = p.executeUpdate();
        p.close();

    }

    public ArrayList<TopicBean> treeGenerator() throws SQLException {
        ArrayList<Integer> rootlist = new ArrayList<>(this.findChildrenIdById(null));
        ArrayList<TopicBean> roottopiclist = new ArrayList<>();
        for(Integer i : rootlist){
            roottopiclist.add(new TopicBean(this,i,null,Integer.toString(rootlist.indexOf(i)+1)));
        }
        if(rootlist.size()==roottopiclist.size())
            System.out.println("TopicDAO: elenco dei topic letto correttamente dal DB, l'elenco radice contiene "+roottopiclist.size()+" elementi");
        else
            System.out.println("TopicDAO: errore durante la costruzione dell'elenco, "+roottopiclist.size() +"elementi aggiunti in radice su "+rootlist.size()+" elementi letti");
        return new ArrayList<>(roottopiclist);
    }

    public int changeFatherTo(int id, Integer f) throws SQLException {
        System.out.println("TopicDAO: l'id da spostare è "+id);

        //cerco il padre vecchio (attuale)
        String query2 = "SELECT parentId FROM dbimagecat.categories WHERE Id = ?";
        PreparedStatement p2 = c.prepareStatement(query2);
        p2.setInt(1, id);
        ResultSet r2 = p2.executeQuery();
        Integer oldfather = null;
        r2.next();
        oldfather = r2.getInt("parentId");
        if(oldfather == 0)
            oldfather = null;
        System.out.println("TopicDAO: l'id del padre vecchio è "+oldfather);

        //controllo che il nuovo padre non abbia già 9 figli
        ArrayList<Integer> tb = this.findChildrenIdById(f);
        for(Integer i : tb){
            System.out.println("TopicDAO: "+i+ " id presente nel nuovo padre avente ordine "+(tb.indexOf(i)+1));
        }
        if(tb.size()>=9)
            return 1;

        //ottengo i vecchi fratelli se esistono
        ArrayList<Integer> oldbrothers = new ArrayList<>(this.findChildrenIdById(oldfather));
        System.out.println("TopicDAO: il padre vecchio di id ha "+oldbrothers.size()+" figli(o)");
        for (Integer o : oldbrothers)
            System.out.println(o+" id presente nel vecchio padre avente ordine "+(oldbrothers.indexOf(o)+1));
        if(oldbrothers.contains(id)){
            oldbrothers.remove((Integer) id);
            System.out.println("TopicDAO: id ha "+oldbrothers.size()+" fratelli");
        }
        for (Integer o : oldbrothers)
            System.out.println(o+" id presente nel vecchio padre aggiornato avente ordine "+(oldbrothers.indexOf(o)+1));

        String query1 = "UPDATE dbimagecat.categories SET parentId = ?, childrenOrder = ? WHERE Id = ?";
        String query3 = "UPDATE dbimagecat.categories SET childrenOrder = ? WHERE Id = ?";
        c.setAutoCommit(false);
        try {
            PreparedStatement p1 = c.prepareStatement(query1);
            PreparedStatement p3 = c.prepareStatement(query3);

            //modifico l'ordine dei vecchi fratelli
            for (Integer o : oldbrothers) {
                System.out.println("TopicDAO: "+o+" id vecchio fratello aggiornato con ordine "+(oldbrothers.indexOf(o)+1));
                p3.setInt(2, o);
                p3.setInt(1, (oldbrothers.indexOf(o) + 1));
                p3.executeUpdate();
                p3.clearParameters();
            }

            if (f == null)
                p1.setNull(1, Types.INTEGER);
            else
                p1.setInt(1, f);
            p1.setInt(2,tb.size()+ (tb.contains(id) ? 0 : 1));
            p1.setInt(3, id);
            p1.executeUpdate();
            c.commit();
            r2.close();
            p1.close();
            p2.close();
            p3.close();
            c.setAutoCommit(true);

        } catch (SQLException e){
            c.rollback();
            c.setAutoCommit(true);
        }
        return 0;




    }

    public ArrayList<Integer> getFatherHierarcy(Integer id) throws SQLException {
        String query = "SELECT parentId FROM dbimagecat.categories WHERE Id = ?";
        PreparedStatement p1 = c.prepareStatement(query);
        ArrayList<Integer> result = new ArrayList<>();
        int index;
        p1.setInt(1,id);
        ResultSet r1 = p1.executeQuery();
        r1.next();
        index = r1.getInt("parentId");
        if(index!=0)
            result.add(index);
        r1.close();
        while(index != 0){
            p1.clearParameters();
            p1.setInt(1,index);
            r1 = p1.executeQuery();
            r1.next();
            index = r1.getInt("parentId");
            result.add(index);
            r1.close();
        }
        p1.close();
        return result;
    }


}
