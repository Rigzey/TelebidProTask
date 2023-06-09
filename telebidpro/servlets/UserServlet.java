package com.example.telebidpro.servlets;

import com.example.telebidpro.services.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private final UserService userService;
    public UserServlet(){
        this.userService = new UserService();
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String requestPath = req.getRequestURI();
        if (requestPath.endsWith("/register")) {
            handleRegister(req, resp);
        } else if (requestPath.endsWith("/login")) {
            handleLogin(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("Invalid endpoint.");
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.getSession().invalidate();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("Logout successful.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp){
        String email = req.getParameter("email");
        userService.forgotPass(email);
    }

    public void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String firstName = req.getParameter("first_name");
        String lastName = req.getParameter("last_name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        userService.registerUser(firstName, lastName, email, password);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("User registered successfully.");
    }
    public void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        boolean loggedIn = userService.loginUser(email, password);
        if (loggedIn) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println("Login successful.");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println("Invalid email or password.");
        }
    }
}
