package org.nomanspace.currencyexchange.servlet;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println("Get currencies request received");
        resp.setContentType("text/html");
        try {
            PrintWriter printWriter = resp.getWriter();
            printWriter.println("Hellow w rot!!1");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
