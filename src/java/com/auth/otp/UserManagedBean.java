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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.codec.binary.Base64;

public class UserManagedBean {

    private UserDAO userDAO;

    private String passwordNew;
    private String password;
    private String passwordAgain;
    private String email;
    private String address;
    private String phone;
    private String qrUrl;

    public String getPasswordNew() {
        return passwordNew;
    }

    public void setPasswordNew(String passwordNew) {
        this.passwordNew = passwordNew;
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
            Logger.getLogger(UserManagedBean.class.getName()).log(Level.SEVERE, "getBytes", ex);
        }
        return "/qrcode/" + base64.encodeAsString(qrUrl.getBytes());
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public UserManagedBean() {
        userDAO = new UserDAO();

        ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
        String username = ext.getSessionMap()
                .get(AuthenticationManagedBean.AUTH_KEY).toString();
        User user = userDAO.findUser(username);
        this.email = user.getEmail();
        this.address = user.getAddress();
        this.phone = user.getPhone();

        Map<String, String> result = OneTimePassword.generateQR(user.getUsername(), user.getEmail(), user.getOtpSecret());
        this.qrUrl = result.get(OneTimePassword.OTPAUTH);
    }

    public String doEdit() {

        ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();

        userDAO = new UserDAO();
        User user = new User();
        user.setUsername(ext.getSessionMap()
                .get(AuthenticationManagedBean.AUTH_KEY).toString());
        user.setEmail(email);
        user.setAddress(address);
        user.setPhone(phone);

        userDAO.editInfos(user);
        return "success";
    }

    public String doChange() {

        FacesContext ctx = FacesContext.getCurrentInstance();

        if (!password.equals(passwordAgain)) {
            FacesMessage msg = new FacesMessage("Password do not match");
            ctx.addMessage("form:passwordAgain", msg);
            return null;
        }

        userDAO = new UserDAO();
        String username = ctx.getExternalContext().getSessionMap()
                .get(AuthenticationManagedBean.AUTH_KEY).toString();
        userDAO.changePass(username, password, passwordNew);

        return "success";
    }
}
