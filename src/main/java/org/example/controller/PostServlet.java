package org.example.controller;

import org.example.dao.PostDAO;
import org.example.dto.PostDTO;
import org.example.service.PostService;
import org.example.util.DataSourceUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/posts")
public class PostServlet extends HttpServlet {
    private PostService postService;

    @Override
    public void init() throws ServletException {
        this.postService = new PostService(new PostDAO(DataSourceUtil.getDataSource()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<PostDTO> posts = postService.getAllPosts();
                resp.getWriter().write(posts.toString()); // Ideally, convert to JSON
            } else {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                PostDTO post = postService.getPostById(id);
                resp.getWriter().write(post.toString()); // Ideally, convert to JSON
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Deserialize JSON to PostDTO (using some library like Jackson/Gson)
        PostDTO postDTO = new PostDTO(); // Mock object, replace with deserialized object
        try {
            postService.savePost(postDTO);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Deserialize JSON to PostDTO (using some library like Jackson/Gson)
        PostDTO postDTO = new PostDTO(); // Mock object, replace with deserialized object
        try {
            postService.updatePost(postDTO);
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
            postService.deletePost(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
