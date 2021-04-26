package it.beans;

import it.dao.TopicDAO;

import java.sql.SQLException;
import java.util.ArrayList;

public class TopicBean {
    int id;
    String topic;
    ArrayList<TopicBean> children;
    Integer parid;
    String treeindex;

    public TopicBean(TopicDAO td, int id, Integer parid, String treeindex) throws SQLException {
        this.id = id;
        this.parid = parid;
        this.topic = td.findTopicById(id);
        this.children = new ArrayList<>();
        this.treeindex = treeindex;

        ArrayList<Integer> childrenids = td.findChildrenIdById(id);
        for(Integer c : childrenids){
            this.children.add(new TopicBean(td,c,id,treeindex+ (childrenids.indexOf(c) + 1)));
        }

    }

    public ArrayList<TopicBean> getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    public int getParid() {
        return parid;
    }

    public String getTopic() {
        return topic;
    }

    public String getTreeindex() {
        return treeindex;
    }
}
