package com.comisiones.controller;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Le decimos a Tomcat que esta clase responde a la URL /test
@WebServlet(name = "TestServlet", urlPatterns = {"/test"})
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Esto simplemente escribirá un mensaje en la página
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Prueba de Servlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>¡El Servlet de prueba FUNCIONA!</h1>");
            out.println("<p>Si ves esto, el despliegue y el servidor están bien.</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}