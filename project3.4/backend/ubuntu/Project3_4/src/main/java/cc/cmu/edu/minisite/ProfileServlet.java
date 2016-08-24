package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


import org.json.JSONArray;

public class ProfileServlet extends HttpServlet {

    public ProfileServlet() {
        /*
         * Your initialization code goes here
         */
    }

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private String URL = "jdbc:mysql://mydbinstance.ckbemsncikro.us-east-1.rds.amazonaws.com:3306/" + DB_NAME;
    private static final String DB_NAME = "mydb";
    private static final String DB_USER = "root";
    private static final String DB_PWD = "db15319root";

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject result = new JSONObject();

        String id = request.getParameter("id");
        String pwd = request.getParameter("pwd");

        /*
         * Task 1: This query simulates the login process of a user, and tests
         * whether your backend system is functioning properly. Your web
         * application will receive a pair of UserID and Password, and you need
         * to check in your backend database to see if the UserID and Password
         * is a valid pair. You should construct your response accordingly:
         * 
         * If YES, send back the user's Name and Profile Image URL. If NOT, set
         * Name as "Unauthorized" and Profile Image URL as "#".
         */
        ResultSet rs = null;
        String profileUrl = null;
        String userName=null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(URL,
                    DB_USER, DB_PWD);
            String stringsql = String.format(
                    "select password,profileurl,name from userinfo,users where userinfo.userid=%s AND users.userid=%s", id,
                    id);
            Statement stmt = conn.createStatement();

            rs = stmt.executeQuery(stringsql);
            while (rs.next()) {
                if (rs.getString("password").equals(pwd)) {
                    profileUrl = rs.getString("profileurl");
                    userName=rs.getString("name");
                }
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (profileUrl == null) {
            JSONObject result1 = new JSONObject();
            result1.put("name", "Unauthorized");
            result1.put("profile", "#");
            PrintWriter writer = response.getWriter();
            System.out.println(String.format("returnRes(%s)", result1.toString()));
            writer.write(String.format("returnRes(%s)", result1.toString()));
            writer.close();
        } else {
            JSONObject result1 = new JSONObject();
            result1.put("name", userName);
            result1.put("profile", profileUrl);
            PrintWriter writer = response.getWriter();
            System.out.println(String.format("returnRes(%s)", result1.toString()));
            writer.write(String.format("returnRes(%s)", result1.toString()));
            writer.close();
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
