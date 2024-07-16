package org.example.dao;

import org.example.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDaoInterface {
    User getById(Long id) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    void save(User user) throws SQLException;

    void update(User user) throws SQLException;

    void delete(Long id) throws SQLException;
}
