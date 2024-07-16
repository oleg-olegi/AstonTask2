package org.example.service;

import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.model.User;
import org.junit.jupiter.api.*;

import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserDAO userDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userDAO = Mockito.mock(UserDAO.class);
        userService = new UserService(userDAO);
    }

    @Test
    public void testGetUserById() throws SQLException {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Mockito.when(userDAO.getById(1L)).thenReturn(user);

        UserDTO userDTO = userService.getUserById(1L);
        assertEquals("Test User", userDTO.getName());
    }

    @Test
    public void testGetAllUsers() throws SQLException {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Mockito.when(userDAO.getAllUsers()).thenReturn(Collections.singletonList(user));

        List<UserDTO> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("Test User", users.get(0).getName());
    }

    @Test
    public void testSaveUser() throws SQLException {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");

        userService.saveUser(userDTO);

        Mockito.verify(userDAO, Mockito.times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser() throws SQLException {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Updated User");
        userDTO.setEmail("updated@example.com");

        userService.updateUser(userDTO);

        Mockito.verify(userDAO, Mockito.times(1)).update(any(User.class));
    }

    @Test
    public void testDeleteUser() throws SQLException {
        userService.deleteUser(1L);

        Mockito.verify(userDAO, Mockito.times(1)).delete(1L);
    }
}
