package org.example.controller;

import com.google.gson.Gson;
import org.example.dao.PostDAO;
import org.example.dto.PostDTO;
import org.example.service.PostService;
import org.example.util.DataSourceUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/posts/*")
public class PostServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private PostService postService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        this.postService = new PostService(new PostDAO(DataSourceUtil.getDataSource()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (pathInfo == null || pathInfo.equals("/")) {
                List<PostDTO> posts = postService.getAllPosts();
                String postJson = gson.toJson(posts);
                resp.getWriter().write(postJson);
            } else {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                PostDTO post = postService.getPostById(id);
                String postJson = gson.toJson(post);
                resp.getWriter().write(postJson);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PostDTO postDTO = deserializeDTO(req);
        try {
            if (postDTO.getContent() == null || postDTO.getContent().isEmpty() || postDTO.getTitle() == null || postDTO.getTitle().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
                return;
            }
            postService.savePost(postDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
            }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
            return;
        }
        try {
            Long id = Long.parseLong(pathInfo.split("/")[1]);
            PostDTO postDTO = deserializeDTO(req);
            postDTO.setId(id);  // Установите ID из пути в объект UserDTO
            if (postDTO.getContent() == null || postDTO.getContent().isEmpty() || postDTO.getTitle() == null || postDTO.getTitle().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
                return;
            }
            postService.updatePost(postDTO);
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
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
            } else {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                postService.deletePost(id);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
        }
    }


    private PostDTO deserializeDTO(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), PostDTO.class);
    }
}
