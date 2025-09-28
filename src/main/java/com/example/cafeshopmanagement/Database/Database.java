package com.example.cafeshopmanagement.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Database {
    private static final String URL = "jdbc:postgresql:///Cafe_Management_System";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Uyya555$";
    public static Connection connectionDB(){
        try{
            Class.forName("org.postgresql.Driver");

            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}