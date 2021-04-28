package it.dao;

import it.beans.TopicBean;
import it.exceptions.TopicNotFoundException;

import java.sql.*;
import java.util.ArrayList;

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
            if(parid != null) {
                p = c.prepareStatement(query);
                p.setInt(1, parid);
                res1 = p.executeQuery();
            }else {
                p = c.prepareStatement(query2);
                res1 = p.executeQuery();
            }

            while(res1.next()){
                children.add(res1.getInt("Id"));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if(res1 != null)
                    res1.close();
                if(p!=null)
                    p.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
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

    public int insertNewTopic(String name, Integer parid, int position) throws SQLException {
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
        return res;

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
}
