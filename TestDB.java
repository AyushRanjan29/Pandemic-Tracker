import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/pandemic_tracker?allowPublicKeyRetrieval=true&useSSL=false",
                "root",
                "ayush7781"
            );
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, type FROM location LIMIT 20;");
            System.out.println("--- Pandemic Tracker Location Table ---");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " - " + rs.getString("name") + " (" + rs.getString("type") + ")");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
