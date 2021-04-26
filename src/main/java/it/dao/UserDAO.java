package it.dao;

import it.beans.UserBean;
import it.exceptions.UserNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    private final Connection c;

    public UserDAO(Connection connection) {
        this.c = connection;
    }

    public UserBean findUserByUName(String username) throws SQLException{
        String query = "SELECT Username, UserPwHash FROM dbimagecat.userdata WHERE Username = ?";
        ResultSet res = null;
        PreparedStatement p = null;
        UserBean u;
        try {
            p = c.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            p.setString(1, username);
            res = p.executeQuery();
            if(!res.next())
                throw new UserNotFoundException();
            else {
                u = new UserBean(username,res.getString("UserPwHash"));
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        } catch (UserNotFoundException e1) {
            u = null;
        } finally {
            try {
                res.close();
            } catch (Exception e1) {
                throw new SQLException(e1);
            }
            try {
                p.close();
            } catch (Exception e2) {
                throw new SQLException(e2);
            }
        }
        return u;


    }

    public void createUser(String username, String password) throws SQLException {

    }
}
