package com.pandemictracker.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/pandemic_tracker?useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "ayush7781"; // User's password from application.yml

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, type FROM location ORDER BY id ASC")) {

            System.out.println("=========================================");
            System.out.println("DATA IN pandemic_tracker.location TABLE:");
            System.out.println("=========================================");
            int count = 0;
            while (rs.next()) {
                System.out.printf("ID: %-4d | Name: %-25s | Type: %s%n", 
                    rs.getInt("id"), rs.getString("name"), rs.getString("type"));
                count++;
            }
            System.out.println("=========================================");
            System.out.println("Total locations found: " + count);
            System.out.println("=========================================");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
