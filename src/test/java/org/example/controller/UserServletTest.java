package org.example.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dto.UserDTO;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServletTest {

    @Mock
    private UserService userService;


    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserServlet userServlet;

    private final Gson gson = new Gson();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDoGetAllUsers() throws ServletException, IOException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getPathInfo()).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        UserDTO user1 = new UserDTO(1L, "John Doe", "john.doe@example.com");
        UserDTO user2 = new UserDTO(2L, "Jane Doe", "jane.doe@example.com");
        List<UserDTO> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        userServlet.doGet(request, response);

        verify(userService, times(1)).getAllUsers();

        String expectedJson = gson.toJson(users);
        assertEquals(expectedJson, stringWriter.toString());
    }

    @Test
    public void testDoGetUserById() throws ServletException, IOException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenReturn(printWriter);

        UserDTO user = new UserDTO(1L, "John Doe", "john.doe@example.com");
        when(userService.getUserById(1L)).thenReturn(user);

        userServlet.doGet(request, response);

        verify(userService, times(1)).getUserById(1L);

        String expectedJson = gson.toJson(user);
        assertEquals(expectedJson, stringWriter.toString());
    }

    @Test
    public void testDoPost() throws ServletException, IOException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String userJson = "{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"}";

        BufferedReader reader = new BufferedReader(new StringReader(userJson));
        when(request.getReader()).thenReturn(reader);

        userServlet.doPost(request, response);

        verify(userService, times(1)).saveUser(any(UserDTO.class));
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoPut() throws ServletException, IOException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        String userJson = "{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"}";

        BufferedReader reader = new BufferedReader(new StringReader(userJson));
        when(request.getReader()).thenReturn(reader);
        when(request.getPathInfo()).thenReturn("/1");

        userServlet.doPut(request, response);

        verify(userService, times(1)).updateUser(any(UserDTO.class));
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    }

    @Test
    public void testDoDelete() throws ServletException, IOException, SQLException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/1");

        userServlet.doDelete(request, response);

        verify(userService, times(1)).deleteUser(1L);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteInvalidIdFormat() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getPathInfo()).thenReturn("/abc");

        userServlet.doDelete(request, response);

        verify(response, times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
    }

    @Test
    public void testDoDeleteUserIdEmpty() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");

        userServlet.doDelete(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID is required.");
    }

    @Test
    public void testDoDeleteSuccessful() throws ServletException, IOException, SQLException {
        Long userId = 1L;
        when(request.getPathInfo()).thenReturn("/" + userId);

        userServlet.doDelete(request, response);

        verify(userService).deleteUser(userId);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    public void testDoDeleteSQLException() throws SQLException {
        Long userId = 1L;
        when(request.getPathInfo()).thenReturn("/" + userId);
        doThrow(SQLException.class).when(userService).deleteUser(userId);

        assertThrows(ServletException.class, () -> userServlet.doDelete(request, response));
    }

    @Test
    public void testDoPutUserIdMissing() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn(null);

        userServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID is required.");
    }

    @Test
    public void testDoPutUserIdEmpty() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/");

        userServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID is required.");
    }

    @Test
    public void testDoPutInvalidIdFormat() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/abc");

        userServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
    }

    @Test
    public void testDoPutMissingNameAndEmail() throws ServletException, IOException {
        long userId = 1L;
        when(request.getPathInfo()).thenReturn("/" + userId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{\"id\":1}")));

        userServlet.doPut(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and Email are required.");
    }

    @Test
    public void testDoPutSQLException() throws IOException, SQLException {
        long userId = 1L;
        String requestBody = "{\"id\":1, \"name\":\"John Doe\", \"email\":\"john.doe@example.com\"}";

        when(request.getPathInfo()).thenReturn("/" + userId);
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));
        doThrow(SQLException.class).when(userService).updateUser(any(UserDTO.class));

        assertThrows(ServletException.class, () -> userServlet.doPut(request, response));
    }

    @Test
    public void testDoPostMissingNameAndEmail() throws ServletException, IOException {
        String requestBody = "{\"id\":1}";

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(requestBody)));

        userServlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Name and Email are required.");
    }
}
