/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.auth.otp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;

@WebServlet("/qrcode/*")
public class QRCodeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Base64 base64 = new Base64();

        String url = req.getPathInfo().substring(1);

        url = new String(base64.decode(url), "utf-8");
        URL qrURL = new URL(OneTimePassword.GURL + url);
        URLConnection conn = qrURL.openConnection();

        resp.setContentType("image/png");
        resp.setStatus(HttpServletResponse.SC_OK);        
        
        try (InputStream is = conn.getInputStream()) {
            BufferedInputStream bis = new BufferedInputStream(is);
            try (OutputStream os = resp.getOutputStream()) {
                BufferedOutputStream bos = new BufferedOutputStream(os);
                byte[] buff = new byte[8192];
                int sz = 0;
                while ((sz = bis.read(buff)) != -1)
                    bos.write(buff, 0, sz);                
                bos.flush();
            }
        }
    }

}
