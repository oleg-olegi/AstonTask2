package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.service.UserService;
import org.example.util.DataSourceUtil;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init() throws ServletException {
        System.out.println("00000000000000000000");
        this.userService = new UserService(new UserDAO(DataSourceUtil.getDataSource()));
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.getWriter().write("Hello, users!");
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<UserDTO> users = userService.getAllUsers();
                resp.getWriter().write(users.toString()); // Ideally, convert to JSON
            } else {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                UserDTO user = userService.getUserById(id);
                resp.getWriter().write(user.toString()); // Ideally, convert to JSON
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Deserialize JSON to UserDTO (using some library like Jackson/Gson)
        UserDTO userDTO = new UserDTO(); // Mock object, replace with deserialized object
        try {
            userService.saveUser(userDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Deserialize JSON to UserDTO (using some library like Jackson/Gson)
        UserDTO userDTO = new UserDTO(); // Mock object, replace with deserialized object
        try {
            userService.updateUser(userDTO);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            Long id = Long.parseLong(pathInfo.split("/")[1]);
            userService.deleteUser(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
