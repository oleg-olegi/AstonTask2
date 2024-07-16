package org.example.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.PostDTO;
import org.example.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostServletTest {
    @Mock
    private PostService postService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private PostServlet postServlet;
    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws ServletException {
        ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        postServlet.init(servletConfig);
        assertNotNull(postService);
    }

    @Test
    public void testDoGetSinglePost() throws IOException, ServletException, SQLException {
        PostDTO post = new PostDTO(1L, "Title", "Content");
        when(postService.getPostById(1L)).thenReturn(post);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        postServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(writer).write(gson.toJson(post));
    }

    @Test
    public void testDoGetAllPosts() throws ServletException, IOException, SQLException {
        // Set up StringWriter and PrintWriter for capturing the response
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        // Mock the behavior of the request and response
        when(request.getPathInfo()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);
        // Prepare the list of posts to be returned by the service
        PostDTO post1 = new PostDTO(1L, "Title1", "Content1");
        PostDTO post2 = new PostDTO(2L, "Title2", "Content2");
        List<PostDTO> posts = Arrays.asList(post1, post2);
        // Mock the behavior of the postService
        when(postService.getAllPosts()).thenReturn(posts);
        // Call the doGet method of the servlet
        postServlet.doGet(request, response);
        // Verify that the service method was called once
        verify(postService, times(1)).getAllPosts();
        // Prepare the expected JSON response
        String expectedJson = gson.toJson(posts);
        // Verify the response headers and content
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(expectedJson, stringWriter.toString().trim());
    }

    @Test
    public void testDoGetAllPostsWithSlashPAthInfo() throws ServletException, IOException, SQLException {
        // Set up StringWriter and PrintWriter for capturing the response
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        // Mock the behavior of the request and response
        when(request.getPathInfo()).thenReturn("/");
        when(response.getWriter()).thenReturn(printWriter);
        // Prepare the list of posts to be returned by the service
        PostDTO post1 = new PostDTO(1L, "Title1", "Content1");
        PostDTO post2 = new PostDTO(2L, "Title2", "Content2");
        List<PostDTO> posts = Arrays.asList(post1, post2);
        // Mock the behavior of the postService
        when(postService.getAllPosts()).thenReturn(posts);
        // Call the doGet method of the servlet
        postServlet.doGet(request, response);
        // Verify that the service method was called once
        verify(postService, times(1)).getAllPosts();
        // Prepare the expected JSON response
        String expectedJson = gson.toJson(posts);
        // Verify the response headers and content
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(expectedJson, stringWriter.toString().trim());
    }


    @Test
    public void testDoPost() throws IOException, ServletException, SQLException {
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);
        when(reader.readLine()).thenReturn("{\"title\": \"Title\", \"content\": \"Content\"}", (String) null);
        ArgumentCaptor<PostDTO> postCaptor = ArgumentCaptor.forClass(PostDTO.class);
        postServlet.doPost(request, response);
        verify(postService).savePost(postCaptor.capture());
        assertEquals("Title", postCaptor.getValue().getTitle());
        assertEquals("Content", postCaptor.getValue().getContent());
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostNullContent() throws ServletException, IOException {
        String missingContentJson = "{\"title\": \"Title without content\",\"content\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPostEmptyContent() throws ServletException, IOException {
        String missingContentJson = "{\"title\": \"Title without content\",\"content\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPostNullTitle() throws ServletException, IOException {
        String missingTitleJson = "{\"title\":null, \"content\": \"Content without title\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingTitleJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPostEmptyTitle() throws ServletException, IOException {
        String missingTitleJson = "{\"title\":\"\", \"content\": \"Content without title\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingTitleJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPut() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(request.getPathInfo()).thenReturn("/1");
        when(reader.readLine()).thenReturn("{\"title\": \"Updated Title\", \"content\": \"Updated Content\"}", (String) null);

        ArgumentCaptor<PostDTO> postCaptor = ArgumentCaptor.forClass(PostDTO.class);

        postServlet.doPut(request, response);

        verify(postService).updatePost(postCaptor.capture());
        assertEquals(1L, postCaptor.getValue().getId());
        assertEquals("Updated Title", postCaptor.getValue().getTitle());
        assertEquals("Updated Content", postCaptor.getValue().getContent());

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPutWithNullPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is null
        when(request.getPathInfo()).thenReturn(null);
        // Call doPut method
        postServlet.doPut(request, response);
        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoPutWithSlashPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is null
        when(request.getPathInfo()).thenReturn("/");
        // Call doPut method
        postServlet.doPut(request, response);
        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoPutMissingPostId() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        postServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
    }

    @Test
    public void testDoPutInvalidPostIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/invalidId");
        postServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
    }

    @Test
    public void testDoPutNullContent() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"title\": \"Title without content\",\"content\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPutEmptyContent() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"title\": \"Title without content\",\"content\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPutNullTitle() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingTitleJson = "{\"title\":null, \"content\": \"Content without title\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingTitleJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPutEmptyTitle() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingTitleJson = "{\"title\":\"\", \"content\": \"Content without title\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingTitleJson));
        when(request.getReader()).thenReturn(reader);
        postServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Content or title are required.");
    }

    @Test
    public void testDoPutSuccess() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        String validPostJson = "{\"title\": \"Updated Title\", \"content\": \"Updated Content\"}";
        BufferedReader reader = new BufferedReader(new StringReader(validPostJson));
        when(request.getReader()).thenReturn(reader);

        postServlet.doPut(request, response);

        verify(postService).updatePost(argThat(post -> post.getId().equals(1L) && post.getTitle().equals("Updated Title") && post.getContent().equals("Updated Content")));
        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPutSQLException() throws IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        String validPostJson = "{\"title\": \"Updated Title\", \"content\": \"Updated Content\"}";
        BufferedReader reader = new BufferedReader(new StringReader(validPostJson));
        when(request.getReader()).thenReturn(reader);
        doThrow(new SQLException("Database error")).when(postService).updatePost(any(PostDTO.class));
        assertThrows(ServletException.class, () -> postServlet.doPut(request, response));
    }


    @Test
    public void testDoDelete() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");
        postServlet.doDelete(request, response);
        verify(postService).deletePost(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteWithNullPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is null
        when(request.getPathInfo()).thenReturn(null);
        // Call doDelete method
        postServlet.doDelete(request, response);
        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoDeleteWithSlashPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is slash
        when(request.getPathInfo()).thenReturn("/");
        // Call doDelete method
        postServlet.doDelete(request, response);
        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoDeleteMissingPostId() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);
        postServlet.doDelete(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required.");
    }

    @Test
    public void testDoDeleteInvalidPostIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/invalidId");
        postServlet.doDelete(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
    }

    @Test
    public void testDoDeleteSuccess() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        postServlet.doDelete(request, response);
        verify(postService).deletePost(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteSQLException() throws SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        doThrow(new SQLException("Database error")).when(postService).deletePost(1L);
        assertThrows(ServletException.class, () -> postServlet.doDelete(request, response));
    }
}
