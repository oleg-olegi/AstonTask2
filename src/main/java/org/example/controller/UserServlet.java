package org.example.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.service.UserService;
import org.example.util.DataSourceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


@WebServlet("/users/*")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserService userService;
    private final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);

    @Override
    public void init() throws ServletException {
        this.userService = new UserService(new UserDAO(DataSourceUtil.getDataSource()));
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (pathInfo == null || pathInfo.equals("/")) {
                List<UserDTO> users = userService.getAllUsers();
                String usersJson = gson.toJson(users);
                resp.getWriter().write(usersJson);
            } else {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                UserDTO user = userService.getUserById(id);
                String userJson = gson.toJson(user);
                resp.getWriter().write(userJson);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Deserialize JSON to UserDTO
        UserDTO userDTO = deserializeUserDTO(req);
        logger.info("Received name: {}, email: {}", userDTO.getName(), userDTO.getEmail());
        try {
            if (userDTO.getName() == null || userDTO.getName().isEmpty() || userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
                logger.error("Name or Email is missing");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and Email are required.");
                return;
            }
            userService.saveUser(userDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID is required.");
            return;
        }
        try {
            Long id = Long.parseLong(pathInfo.split("/")[1]);
            UserDTO userDTO = deserializeUserDTO(req);
            userDTO.setId(id);  // Установите ID из пути в объект UserDTO
            if (userDTO.getName() == null || userDTO.getName().isEmpty() || userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and Email are required.");
                return;
            }
            userService.updateUser(userDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("Received DELETE request for path: {}", pathInfo);
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.error("User ID is missing in the request");
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID is required.");
            } else {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                logger.info("Deleting user with ID: {}", id);
                userService.deleteUser(id);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (SQLException e) {
            logger.error("SQLException while deleting user", e);
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
        }
    }


    private UserDTO deserializeUserDTO(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), UserDTO.class);
    }
}
