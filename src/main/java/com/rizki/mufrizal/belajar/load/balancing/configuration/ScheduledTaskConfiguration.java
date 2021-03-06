package com.rizki.mufrizal.belajar.load.balancing.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @Author Rizki Mufrizal <mufrizalrizki@gmail.com> <https://RizkiMufrizal.github.io>
 * @Since 10 August 2016
 * @Time 21:47
 * @Project Spring-Boot-Load-Balancing
 * @Package com.rizki.mufrizal.belajar.load.balancing.configuration
 * @File ScheduledTaskConfiguration
 */
@EnableScheduling
@Component
public class ScheduledTaskConfiguration {

    @Autowired
    private DataSource dataSource;

    private Connection connection;

    private PreparedStatement preparedStatementSelectUser;
    private PreparedStatement preparedStatementSelectUserRole;

    private PreparedStatement preparedStatementInsertUser;
    private PreparedStatement preparedStatementInsertUserRole;

    private final String selectTableUserSQL = "SELECT * FROM tb_user WHERE username = ?";
    private final String selectTableUserRoleSQL = "SELECT * FROM tb_user_role WHERE username = ?";

    private final String insertTableUserSQL = "INSERT INTO tb_user(username, password, enable) values(?, ?, ?)";
    private final String insertTableUserRoleSQL = "INSERT INTO tb_user_role(id_user_role, role, username) values(?, ?, ?)";

    @Scheduled(fixedRate = 3600000)
    public void insertDefaultUserAdministrator() throws SQLException {

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        connection = dataSource.getConnection();

        preparedStatementSelectUser = connection.prepareStatement(selectTableUserSQL);
        preparedStatementSelectUser.setString(1, "administrator");
        ResultSet resultSetUser = preparedStatementSelectUser.executeQuery();

        preparedStatementSelectUserRole = connection.prepareStatement(selectTableUserRoleSQL);
        preparedStatementSelectUserRole.setString(1, "administrator");
        ResultSet resultSetUserRole = preparedStatementSelectUserRole.executeQuery();

        if (!resultSetUser.next() && !resultSetUserRole.next()) {
            preparedStatementInsertUser = connection.prepareStatement(insertTableUserSQL);
            preparedStatementInsertUser.setString(1, "administrator");
            preparedStatementInsertUser.setString(2, bCryptPasswordEncoder.encode("administrator"));
            preparedStatementInsertUser.setBoolean(3, Boolean.TRUE);
            preparedStatementInsertUser.executeUpdate();

            preparedStatementInsertUserRole = connection.prepareStatement(insertTableUserRoleSQL);
            preparedStatementInsertUserRole.setString(1, UUID.randomUUID().toString());
            preparedStatementInsertUserRole.setString(2, "ROLE_ADMINISTRATOR");
            preparedStatementInsertUserRole.setString(3, "administrator");
            preparedStatementInsertUserRole.executeUpdate();

            preparedStatementInsertUser.close();
            preparedStatementInsertUserRole.close();
        }

        preparedStatementSelectUser.close();
        preparedStatementSelectUserRole.close();
        connection.close();

    }
}
