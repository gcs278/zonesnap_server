package edu.vt.ece4564.example;

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
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Recieve JSON post data
		BufferedReader bin = new BufferedReader(req.getReader());
		String jsonString = bin.readLine();
		
		// Parse the JSON data from post
		String email = null, name = null, instrument = null, password = null;
		JSONParser j = new JSONParser();
		try {
			JSONObject o = (JSONObject) j.parse(jsonString);
			email = (String) o.get("username");
			name = (String) o.get("name");
			password = (String) o.get("password");
			instrument = (String) o.get("instrument");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Connect to database and attempt to register user
		Database database = new Database();
		if (database.RegisterUser(name, email, instrument, password)) {
			resp.getWriter().println("SUCCESS");
		} else {
			resp.getWriter().println("FAIL"); // Name Taken
		}
		
		resp.getWriter().close();
	}
}
