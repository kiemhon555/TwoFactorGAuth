/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.auth.model;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author K
 */
public class UserDAO {

    private Connection conn;
    private String sql;
    private Statement st;
    private PreparedStatement ps;
    private ResultSet rs;

    public boolean login(String username, String password) {
        boolean result = false;

        conn = DBUtil.getConnection();
        sql = "Select 1 from users where username = ? and password = ?";
        try {
            ps = conn.prepareCall(sql);
            ps.setString(1, username);
            ps.setString(2, sha256(password));
            rs = ps.executeQuery();

            if (rs.next()) {
                result = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public User findUser(String username) {
        User user = null;
        conn = DBUtil.getConnection();
        sql = "Select * from Users where username = ?";
        try {
            ps = conn.prepareCall(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUsername(rs.getString("UserName"));
                user.setPassword(rs.getString("Password"));
                user.setEmail(rs.getString("Email"));
                user.setAddress(rs.getString("Address"));
                user.setPhone(rs.getString("Phone"));
                user.setOtpSecret(rs.getString("otp_secret"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    public int register(User user) {
        int result = 0;

        conn = DBUtil.getConnection();
        sql = "Insert into Users values(?,?,?,?,?,?)";
        try {
            ps = conn.prepareCall(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, sha256(user.getPassword()));
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getAddress());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getOtpSecret());

            result = ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int editInfos(User user) {
        int result = 0;

        conn = DBUtil.getConnection();
        sql = "Update Users set email = ?, address = ?, phone = ? where username = ?";
        try {
            ps = conn.prepareCall(sql);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getAddress());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getUsername());

            result = ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int changePass(String uname, String pass, String newpass) {
        int result = 0;
        conn = DBUtil.getConnection();

        try {
            if (login(uname, pass)) {
                sql = "Update Users set password = ? where username = ?";
                ps = conn.prepareCall(sql);
                ps.setString(1, sha256(newpass));
                ps.setString(2, uname);
                result = ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String sha256(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(s.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return (hexString.toString());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, "Encoding sha256", ex);
            return (null);
        }
    }
}
