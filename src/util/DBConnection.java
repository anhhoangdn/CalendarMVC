package util;

import java.sql.*;

public class DBConnection {
	private static final String URL = 
		    "jdbc:mysql://localhost:3306/ooad" +
		    "?useSSL=false" +
		    "&serverTimezone=UTC" +
		    "&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "sql123!";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { e.printStackTrace(); }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
