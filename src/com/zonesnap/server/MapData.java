package com.zonesnap.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaFileManager.Location;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.Connection;

// This servlet is for authenticating a user
public class MapData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// NOT IN USE
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		Database database = new Database();
		// Determine type of request
		String requestType = req.getParameter("type");
		String jsonData = null;

		if (requestType.equalsIgnoreCase(new String("pics"))) {
			// Return the user's profile data
			String email = req.getParameter("email");
			System.out.println(database.GetPhotoLocations().toString());
			
		} else if (requestType.equalsIgnoreCase(new String("zones"))) {

		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Post Variables for login
		String email = null, password = null;
		double latitude = 0, longitude = 0;

		// Read the JSON data from POST
		BufferedReader bin = new BufferedReader(req.getReader());
		String json = bin.readLine();

		try {
			// Parse the JSON data
			JSONParser j = new JSONParser();
			JSONObject o = (JSONObject) j.parse(json);
			email = (String) o.get("username");
			latitude = (double) o.get("latitude");
			longitude = (double) o.get("longitude");
			password = (String) o.get("password");
			System.out.println("Email:" + email);
			// Connect to database, and check creds. of user
			Database database = new Database();
			String response = database.LoginUser(email, password, latitude,
					longitude);

			// Send response to app
			resp.getWriter().println(response);

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
}
