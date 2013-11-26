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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.tools.JavaFileManager.Location;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.Connection;

// This servlet is for authenticating a user
public class Like extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Database database = new Database();
		
		String username = req.getParameter("user");
		
		if (username == null) {
			resp.getWriter().println("Error: User not specified");
			return;
		}
		
		// Return list of photoIDs
		List<Integer> photoIDs = database.GetLikedPhotos(username);
		JSONObject json = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (Iterator<Integer> i = photoIDs.iterator(); i.hasNext();) {
			String item = i.next().toString();
			jsonArray.add(item);
		}
		json.put("photoIDs", jsonArray);
		String sendData = json.toString();
	
		resp.getWriter().println(sendData);
		database.Close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Post Variables for login
		String username;
		int photoID;

		// Read the JSON data from POST
		BufferedReader bin = new BufferedReader(req.getReader());
		String json = bin.readLine();

		try {
			// Parse the JSON data
			JSONParser j = new JSONParser();
			JSONObject o = (JSONObject) j.parse(json);
			username = (String) o.get("username");
			photoID = Integer.parseInt(o.get("photoID").toString());

			// Connect to database, and check creds. of user
			Database database = new Database();
			database.LikePicture(username, photoID);

			// Send response to app
			// resp.getWriter().println(response);

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
}
