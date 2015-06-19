/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.auth.otp;

import com.auth.model.User;
import com.auth.model.UserDAO;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationManagedBean {

    public static final String AUTH_KEY = "user";

    private UserDAO userDAO;

    private String username;
    private String password;
    private Integer otpCode;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(Integer otpCode) {
        this.otpCode = otpCode;
    }

    public AuthenticationManagedBean() {
    }

    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(AUTH_KEY) != null;
    }

    public String doLogin() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        userDAO = new UserDAO();

        User user = userDAO.findUser(username);
        if (null == user) {
            ctx.addMessage(null, new FacesMessage("Cannot find user."));
            return "error";
        }

        try {
            if (userDAO.login(username, password)) {
                if (!OneTimePassword.checkCode(user.getOtpSecret(), otpCode, System.currentTimeMillis())) {
                    ctx.addMessage(null, new FacesMessage("Incorrect code"));
                    return "error";
                } else {
                    ctx.getExternalContext().getSessionMap().put(AUTH_KEY, username);
                }
            } else {
                ctx.addMessage(null, new FacesMessage("Wrong password"));
                return "error";
            }

        } catch (InvalidKeyException | NoSuchAlgorithmException | NumberFormatException ex) {
            ctx.addMessage(null, new FacesMessage("Exception: " + ex.getMessage()));
            return "error";
        }
        return "success";
    }

    public String doLogout() {
        ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
        ext.getSessionMap().remove(AUTH_KEY);
        HttpServletResponse resp = (HttpServletResponse) ext.getResponse();
        try {
            resp.sendRedirect("../login/login.xhtml");
        } catch (IOException ex) {
            Logger.getLogger(AuthenticationManagedBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
