package com.zonesnap.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// This class if for register a user
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Register/Login user
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Recieve JSON post data
		BufferedReader bin = new BufferedReader(req.getReader());
		String jsonString = bin.readLine();

		// Parse the JSON data from post
		String username = null;
		JSONParser j = new JSONParser();
		try {
			JSONObject o = (JSONObject) j.parse(jsonString);
			username = (String) o.get("username");

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Connect to database and attempt to register user
		Database database = new Database();
		if (database.RegisterUser(username)) {
			resp.getWriter().println("SUCCESS");
		} else {
			resp.getWriter().println("FAIL"); // Name Taken
		}

		resp.getWriter().close();
	}
}
