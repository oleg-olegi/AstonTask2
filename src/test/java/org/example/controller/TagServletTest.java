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

    @Test
    public void testAddTagToPost_ValidRequest() throws IOException, ServletException, SQLException {
        // Prepare request data
        String json = "{\"postId\":1,\"tagId\":2}";
        BufferedReader reader = new BufferedReader(new StringReader(json));

        when(request.getPathInfo()).thenReturn("/addTagToPost");
        when(request.getReader()).thenReturn(reader);

        // Execute the method
        tagServlet.doPost(request, response);

        // Verify interactions and response
        verify(tagService, times(1)).addTagToPost(2L, 1L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoGet_GetTagsByPostId_Success() throws ServletException, IOException, SQLException {
        // Set up request
        when(request.getPathInfo()).thenReturn("/1/posts");

        // Mock the response writer
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // Set up the service response
        TagDTO tag1 = new TagDTO();
        tag1.setId(1L);
        tag1.setName("Tag1");

        TagDTO tag2 = new TagDTO();
        tag2.setId(2L);
        tag2.setName("Tag2");

        List<TagDTO> tags = Arrays.asList(tag1, tag2);
        when(tagService.getTagsByPostId(1L)).thenReturn(tags);

        // Call the method
        tagServlet.doGet(request, response);

        // Verify interactions and response
        verify(tagService, times(1)).getTagsByPostId(1L);
        verify(writer, times(1)).write(gson.toJson(tags));
    }

    @Test
    public void testDoGet_NumberFormatException() throws ServletException, IOException, SQLException {
        // Set up request
        when(request.getPathInfo()).thenReturn("/abc/posts");
        // Call the method
        tagServlet.doGet(request, response);
        // Verify interactions and response
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tag ID format.");
        verify(tagService, never()).getTagsByPostId(anyLong());
    }

    private void mockRequestReader(String json) throws IOException {
        BufferedReader reader = mock(BufferedReader.class);
        when(request.getReader()).thenReturn(reader);
        when(reader.readLine()).thenReturn(json, (String) null);
    }

    @Test
    public void testAddTagToPost_PostIdIsNull() throws IOException, ServletException, SQLException {
        // Mock request data with null postId
        String json = "{\"tagId\": 1}";
        mockRequestReader(json);
        when(request.getPathInfo()).thenReturn("/addTagToPost");
        // Call the method
        tagServlet.doPost(request, response);
        // Verify response
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID and Tag ID are required.");
        verify(tagService, never()).addTagToPost(anyLong(), anyLong());
    }

    @Test
    public void testAddTagToPost_TagIdIsNull() throws IOException, ServletException, SQLException {
        // Mock request data with null tagId
        String json = "{\"postId\": 1}";
        mockRequestReader(json);
        when(request.getPathInfo()).thenReturn("/addTagToPost");
        // Call the method
        tagServlet.doPost(request, response);
        // Verify response
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID and Tag ID are required.");
        verify(tagService, never()).addTagToPost(anyLong(), anyLong());
    }

    @Test
    public void testAddTagToPost_BothIdsArePresent() throws IOException, ServletException, SQLException {
        // Mock request data with both postId and tagId
        String json = "{\"postId\": 1, \"tagId\": 1}";
        mockRequestReader(json);
        when(request.getPathInfo()).thenReturn("/addTagToPost");
        // Call the method
        tagServlet.doPost(request, response);
        // Verify service call
        verify(tagService, times(1)).addTagToPost(1L, 1L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private void mockRequestPathInfo(String pathInfo) {
        when(request.getPathInfo()).thenReturn(pathInfo);
    }

    @Test
    public void testDoDelete_RemoveTagFromPost() throws IOException, ServletException, SQLException {
        // Set up path info to match the condition
        String pathInfo = "/1/posts/2";
        mockRequestPathInfo(pathInfo);
        // No exception expected, so just verify method call
        doNothing().when(tagService).removeTagFromPost(anyLong(), anyLong());
        // Call the method
        tagServlet.doDelete(request, response);
        // Verify that the service method was called with correct parameters
        verify(tagService, times(1)).removeTagFromPost(1L, 2L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDelete_NullPathInfo() throws IOException, ServletException {
        // Set up path info to be null
        mockRequestPathInfo(null);
        // Call the method
        tagServlet.doDelete(request, response);
        // Verify that the status is set to BAD_REQUEST
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tag ID is required.");
    }

    private void mockRequestBody(String body) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(body));
        when(request.getReader()).thenReturn(reader);
    }


    @Test
    public void testDoPut_MissingName() throws IOException, ServletException, SQLException {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName(null);
        // Name is not set here
        String requestBody = gson.toJson(tagDTO);
        mockRequestBody(requestBody);
        mockRequestPathInfo("/1");
        // Simulate failure
        tagServlet.doPut(request, response);
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
        verify(tagService, never()).update(any(TagDTO.class));
    }

    @Test
    public void testDoPut_EmptyName() throws IOException, ServletException, SQLException {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("");
        String requestBody = gson.toJson(tagDTO);
        mockRequestBody(requestBody);
        mockRequestPathInfo("/1");
        // Simulate failure
        tagServlet.doPut(request, response);
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
        verify(tagService, never()).update(any(TagDTO.class));
    }

    @Test
    public void testDoPut_SQLException() throws IOException, ServletException, SQLException {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("Valid Tag");
        String requestBody = gson.toJson(tagDTO);
        mockRequestBody(requestBody);
        mockRequestPathInfo("/1");
        // Simulate SQLException
        doThrow(new SQLException()).when(tagService).update(tagDTO);
        tagServlet.doPut(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testDoPost_EmptyName() throws IOException, ServletException, SQLException {
        // Prepare the JSON body for the request
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("");  // Empty name
        String requestBody = gson.toJson(tagDTO);
        // Mock the request and response
        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        // Call doPost
        tagServlet.doPost(request, response);
        // Verify response
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
        verify(tagService, never()).save(any(TagDTO.class));
    }

    @Test
    public void testDoPost_NullName() throws IOException, ServletException, SQLException {
        // Prepare the JSON body for the request
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName(null);  // Empty name
        String requestBody = gson.toJson(tagDTO);
        // Mock the request and response
        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        // Call doPost
        tagServlet.doPost(request, response);
        // Verify response
        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name is required.");
        verify(tagService, never()).save(any(TagDTO.class));
            }

    @Test
    public void testDoPost_SQLException() throws IOException, ServletException, SQLException {
        // Prepare the JSON body for the request
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName("Valid Name");
        String requestBody = gson.toJson(tagDTO);
        // Mock the request and response
        when(request.getPathInfo()).thenReturn("/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        // Configure the tagService to throw SQLException
        doThrow(new SQLException("Database error")).when(tagService).save(any(TagDTO.class));
        // Verify the behavior
        assertThrows(ServletException.class, () -> tagServlet.doPost(request, response));
    }
}
