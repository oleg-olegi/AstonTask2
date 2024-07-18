package org.example.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.TagDTO;
import org.example.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

public class TagServletTest {
    @Mock
    private TagService tagService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TagServlet tagServlet;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testInit() throws ServletException {
        ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        tagServlet.init(servletConfig);
        assertNotNull(tagService);
    }

    @Test
    public void testDoGetSingleTag() throws IOException, ServletException, SQLException {
        TagDTO tag = new TagDTO(1L, "Tag");
        when(tagService.getTagById(1L)).thenReturn(tag);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        tagServlet.doGet(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(writer).write(gson.toJson(tag));
    }

    @Test
    public void testDoGetAllTags() throws ServletException, IOException, SQLException {

        // Set up StringWriter and PrintWriter for capturing the response
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // Mock the behavior of the request and response
        when(request.getPathInfo()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        // Prepare the list of tags to be returned by the service
        TagDTO tag1 = new TagDTO(1L, "Tag1");
        TagDTO tag2 = new TagDTO(2L, "Tag2");
        List<TagDTO> tags = Arrays.asList(tag1, tag2);

        // Mock the behavior of the tagService
        when(tagService.getAllTags()).thenReturn(tags);

        // Call the doGet method of the servlet
        tagServlet.doGet(request, response);

        // Verify that the service method was called once
        verify(tagService, times(1)).getAllTags();

        // Prepare the expected JSON response
        String expectedJson = gson.toJson(tags);

        // Verify the response headers and content
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(expectedJson, stringWriter.toString().trim());
    }

    @Test
    public void testDoGetAllTagsWithSlashPAthInfo() throws ServletException, IOException, SQLException {
        // Set up StringWriter and PrintWriter for capturing the response
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        // Mock the behavior of the request and response
        when(request.getPathInfo()).thenReturn("/");
        when(response.getWriter()).thenReturn(printWriter);
        // Prepare the list of tags to be returned by the service
        TagDTO tag1 = new TagDTO(1L, "Tag1");
        TagDTO tag2 = new TagDTO(2L, "Tag2");
        List<TagDTO> tags = Arrays.asList(tag1, tag2);
        // Mock the behavior of the tagService
        when(tagService.getAllTags()).thenReturn(tags);
        // Call the doGet method of the servlet
        tagServlet.doGet(request, response);
        // Verify that the service method was called once
        verify(tagService, times(1)).getAllTags();
        // Prepare the expected JSON response
        String expectedJson = gson.toJson(tags);
        // Verify the response headers and content
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        assertEquals(expectedJson, stringWriter.toString().trim());
    }


    @Test
    public void testDoPost() throws IOException, ServletException, SQLException {
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(reader.readLine()).thenReturn("{\"name\": \"Name\"}", (String) null);

        ArgumentCaptor<TagDTO> tagCaptor = ArgumentCaptor.forClass(TagDTO.class);

        tagServlet.doPost(request, response);

        verify(tagService).save(tagCaptor.capture());
        assertEquals("Name", tagCaptor.getValue().getName());


        verify(response).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPostNullName() throws ServletException, IOException {
        String missingContentJson = "{\"title\": \"null\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        tagServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
    }

    @Test
    public void testDoPostEmptyName() throws ServletException, IOException {
        String missingContentJson = "{\"title\":\"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        tagServlet.doPost(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
    }

    @Test
    public void testDoPut() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);

        when(request.getPathInfo()).thenReturn("/1");
        when(reader.readLine()).thenReturn("{\"name\": \"Updated Name\"}", (String) null);

        ArgumentCaptor<TagDTO> tagCaptor = ArgumentCaptor.forClass(TagDTO.class);

        tagServlet.doPut(request, response);

        verify(tagService).update(tagCaptor.capture());
        assertEquals(1L, tagCaptor.getValue().getId());
        assertEquals("Updated Name", tagCaptor.getValue().getName());

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoPutWithNullPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is null
        when(request.getPathInfo()).thenReturn(null);

        // Call doDelete method
        tagServlet.doPut(request, response);

        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoPutWithSlashPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is null
        when(request.getPathInfo()).thenReturn("/");

        // Call doDelete method
        tagServlet.doPut(request, response);

        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }


    @Test
    public void testDoPutMissingTagId() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        tagServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
    }

    @Test
    public void testDoPutInvalidTagIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/invalidId");

        tagServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format.");
    }

    @Test
    public void testDoPutNullContent() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"title\": \"Title without content\",\"content\": null}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        tagServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
    }

    @Test
    public void testDoPutEmptyContent() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingContentJson = "{\"title\": \"Title without content\",\"content\": \"\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingContentJson));
        when(request.getReader()).thenReturn(reader);
        tagServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
    }

    @Test
    public void testDoPutNullTitle() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingTitleJson = "{\"title\":null, \"content\": \"Content without title\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingTitleJson));
        when(request.getReader()).thenReturn(reader);
        tagServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
    }

    @Test
    public void testDoPutEmptyTitle() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/1");
        String missingTitleJson = "{\"title\":\"\", \"content\": \"Content without title\"}";
        BufferedReader reader = new BufferedReader(new StringReader(missingTitleJson));
        when(request.getReader()).thenReturn(reader);
        tagServlet.doPut(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
    }

    @Test
    public void testDoPutSuccess() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        String validTagJson = "{\"name\": \"Updated Name\"}";
        BufferedReader reader = new BufferedReader(new StringReader(validTagJson));
        when(request.getReader()).thenReturn(reader);

        tagServlet.doPut(request, response);

        verify(tagService).update(argThat(tag -> tag.getId().equals(1L) && tag.getName().equals("Updated Name")));
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoPutSQLException() throws IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        String validTagJson = "{\"name\": \"Updated Name\"}";
        BufferedReader reader = new BufferedReader(new StringReader(validTagJson));
        when(request.getReader()).thenReturn(reader);
        doThrow(new SQLException("Database error")).when(tagService).update(any(TagDTO.class));

        assertThrows(ServletException.class, () -> tagServlet.doPut(request, response));
    }


    @Test
    public void testDoDelete() throws IOException, ServletException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn("/1");

        tagServlet.doDelete(request, response);

        verify(tagService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteWithNullPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is null
        when(request.getPathInfo()).thenReturn(null);

        // Call doDelete method
        tagServlet.doDelete(request, response);

        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoDeleteWithSlashPathInfo() throws ServletException, IOException {
        // Set up the scenario where pathInfo is slash
        when(request.getPathInfo()).thenReturn("/");

        // Call doDelete method
        tagServlet.doDelete(request, response);

        // Verify that sendError was called with SC_BAD_REQUEST and the correct message
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
        // Verify that setStatus was not called
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void testDoDeleteMissingTagId() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        tagServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
    }

    @Test
    public void testDoDeleteInvalidTagIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/invalidId");

        tagServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag or post ID format.");
    }

    @Test
    public void testDoDeleteSuccess() throws ServletException, IOException, SQLException {
        when(request.getPathInfo()).thenReturn("/1");

        tagServlet.doDelete(request, response);

        verify(tagService).delete(1L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteSQLException() throws SQLException {
        when(request.getPathInfo()).thenReturn("/1");
        doThrow(new SQLException("Database error")).when(tagService).delete(1L);

        assertThrows(ServletException.class, () -> tagServlet.doDelete(request, response));
    }
}
