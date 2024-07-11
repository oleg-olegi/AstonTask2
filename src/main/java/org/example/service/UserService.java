package org.example.service;

import org.example.dao.UserDAO;
import org.example.dto.UserDTO;
import org.example.mapper.UserMapper;
import org.example.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserDTO getUserById(Long id) throws SQLException {
        User user = userDAO.getById(id);
        return UserMapper.INSTANCE.userToUserDTO(user);
    }

    public List<UserDTO> getAllUsers() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        return users.stream().map(UserMapper.INSTANCE::userToUserDTO).collect(Collectors.toList());
    }

    public void saveUser(UserDTO userDTO) throws SQLException {
        User user = UserMapper.INSTANCE.userDTOToUser(userDTO);
        userDAO.save(user);
    }

    public void updateUser(UserDTO userDTO) throws SQLException {
        User user = UserMapper.INSTANCE.userDTOToUser(userDTO);
        userDAO.update(user);
    }

    public void deleteUser(Long id) throws SQLException {
        userDAO.delete(id);
    }
}
