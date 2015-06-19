/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.auth.otp;

import com.auth.model.User;
import com.auth.model.UserDAO;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.apache.commons.codec.binary.Base64;

public class RegisterManagedBean {

    private UserDAO userDAO;

    private String username;
    private String password;
    private String passwordAgain;
    private String email;
    private String address;
    private String phone;
    private String qrUrl;

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

    public String getPasswordAgain() {
        return passwordAgain;
    }

    public void setPasswordAgain(String passwordAgain) {
        this.passwordAgain = passwordAgain;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQrUrl() {
        Base64 base64 = new Base64();  
        try {                  
            return ("/qrcode/" + base64.encodeAsString(qrUrl.getBytes("utf-8")));
        } catch (Exception ex) {
            Logger.getLogger(RegisterManagedBean.class.getName()).log(Level.SEVERE, "getBytes", ex);                       
        }
        return ("/qrcode/" + base64.encodeAsString(qrUrl.getBytes()));
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public RegisterManagedBean() {
    }

    public String doRegister() {
        userDAO = new UserDAO();
        if (null != userDAO.findUser(username)) {
            FacesMessage msg = new FacesMessage(username
                    + " has been taken. Please choose another");
            FacesContext.getCurrentInstance().addMessage("form:username", msg);
            return null;
        }

        if (!password.equals(passwordAgain)) {
            FacesMessage msg = new FacesMessage("Password do not match");
            FacesContext.getCurrentInstance().addMessage("form:passwordAgain", msg);
            return null;
        }

        Map<String, String> result = OneTimePassword.generateQR(username, email);
        User user = new User(username, password, email, address, phone, result.get(OneTimePassword.SECRET));
        userDAO.register(user);

        qrUrl = result.get(OneTimePassword.OTPAUTH);

        return "qrcode";
    }
}
