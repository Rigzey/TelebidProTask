package com.example.telebidpro.services;

import com.example.telebidpro.DatabaseConnection;
import com.example.telebidpro.MailSender;
import com.example.telebidpro.entities.User;
import com.example.telebidpro.exceptions.BadRequestException;

import javax.mail.MessagingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserService {
    public void registerUser(String firstName, String lastName, String email, String password){
        if(!validRegister(firstName, lastName, email, password)){
            throw new BadRequestException("Invalid register credentials!");
        }
        saveUser(firstName, lastName, email, password);
        sendRegisterEmail(email);
    }
    public boolean loginUser(String email, String password) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM users WHERE email = ? AND password = ?")) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong with the login");
            e.printStackTrace();
        }
        return false;
    }
    public void forgotPass(String email) {
        UUID randomToken = UUID.randomUUID();
        try(Connection connection = DatabaseConnection.getConnection();
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET forgot_password_token = ? WHERE email = ?")){
            MailSender.sendEmail(email, "Forgotten password",
                    "Please, click on this link to reset your password: localhost:7777/users/forgot-password/" + randomToken);
            ps.setString(1, randomToken.toString());
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (MessagingException e) {
            System.out.println("Something went wrong with the email");
            e.printStackTrace();
            forgotPass(email);
        } catch (SQLException e) {
            System.out.println("Something went wrong with the connection");
            e.printStackTrace();
        }
    }
    private void sendRegisterEmail(String email){
        UUID randomToken = UUID.randomUUID();
        try(Connection connection = DatabaseConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE users SET register_validation_token = ? WHERE email = ?")){
            MailSender.sendEmail(email, "Register validation",
                    "Please, click on this link to validate your registration: localhost:7777/users/validate/" + randomToken);
            ps.setString(1, randomToken.toString());
            ps.setString(2, email);
        } catch (SQLException e) {
            System.out.println("Something went wrong with the connection");
            e.printStackTrace();
        } catch(MessagingException e){
            System.out.println("Something went wrong while sending the message");
            e.printStackTrace();
            sendRegisterEmail(email);
        }
    }
    private boolean validRegister(String firstName, String lastName, String email, String password){
        if(firstName.isBlank()){
            return false;
        }
        if(lastName.isBlank()){
            return false;
        }
        if(!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d).+$")){
            return false;
        }
        if(!email.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")){
            return false;
        }
        return true;
    }
    private void saveUser(String firstName, String lastName, String email, String password){
        User user = new User(firstName, lastName, email, password);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO users (firstName, lastName, email, password) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int generatedId = resultSet.getInt(1);
                    user.setId(generatedId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong with the registration");
            e.printStackTrace();
        }
    }


}
