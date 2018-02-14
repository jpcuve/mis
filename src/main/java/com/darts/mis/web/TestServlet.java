package com.darts.mis.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by jpc on 06-03-16.
 */
@WebServlet(urlPatterns = { "/test" })
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html; charset=" + Charset.defaultCharset().name());
        res.getOutputStream().print("<html><head/><body>Hello</body></html>");
    }
}
