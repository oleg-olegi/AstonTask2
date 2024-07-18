package org.example.controller;

import com.google.gson.Gson;
import org.example.dao.TagDAO;
import org.example.dto.TagDTO;
import org.example.service.TagService;
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

@WebServlet("/tags/*")
public class TagServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TagService tagService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        this.tagService = new TagService(new TagDAO(DataSourceUtil.getDataSource()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            if (pathInfo == null || pathInfo.equals("/")) {
                List<TagDTO> tags = tagService.getAllTags();
                String tagJson = gson.toJson(tags);
                resp.getWriter().write(tagJson);
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long id = Long.parseLong(pathParts[1]);
                    TagDTO tag = tagService.getTagById(id);
                    String tagJson = gson.toJson(tag);
                    resp.getWriter().write(tagJson);
                } else if (pathParts.length == 3 && pathParts[2].equals("posts")) {
                    Long id = Long.parseLong(pathParts[1]);
                    List<TagDTO> tags = tagService.getTagsByPostId(id);
                    String tagJson = gson.toJson(tags);
                    resp.getWriter().write(tagJson);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            TagDTO tagDTO = deserializeTagDTO(req);
            try {
                if (tagDTO.getName() == null || tagDTO.getName().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
                    return;
                }
                tagService.save(tagDTO);
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } catch (SQLException e) {
                throw new ServletException(e);
            }
        } else if (pathInfo.equals("/addTagToPost")) {
            AddTagToPostRequest addTagToPostRequest = deserializeAddTagToPostRequest(req);
            try {
                if (addTagToPostRequest.getPostId() == null || addTagToPostRequest.getTagId() == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID and Tag ID are required.");
                    return;
                }
                tagService.addTagToPost(addTagToPostRequest.getTagId(), addTagToPostRequest.getPostId());
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (SQLException e) {
                throw new ServletException(e);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid post or tag ID format.");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
            return;
        }
        try {
            Long id = Long.parseLong(pathInfo.split("/")[1]);
            TagDTO tagDTO = deserializeTagDTO(req);
            tagDTO.setId(id);
            if (tagDTO.getName() == null || tagDTO.getName().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
                return;
            }
            tagService.update(tagDTO);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long id = Long.parseLong(pathParts[1]);
                    tagService.delete(id);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else if (pathParts.length == 4 && pathParts[2].equals("posts")) {
                    Long postId = Long.parseLong(pathParts[3]);
                    Long tagId = Long.parseLong(pathParts[1]);
                    tagService.removeTagFromPost(tagId, postId);
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag or post ID format.");
        }
    }

    private TagDTO deserializeTagDTO(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), TagDTO.class);
    }

    private AddTagToPostRequest deserializeAddTagToPostRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return gson.fromJson(sb.toString(), AddTagToPostRequest.class);
    }

    private static class AddTagToPostRequest {
        private Long postId;
        private Long tagId;

        public Long getPostId() {
            return postId;
        }

        public Long getTagId() {
            return tagId;
        }
    }
}
