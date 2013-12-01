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

// This servlet is for getting liked photos/ Liking photo
public class Like extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Get a list of user's liked photos
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Database database = new Database();
		
		String username = req.getParameter("user");
		
		// Check for username
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
		
		// Send data
		json.put("photoIDs", jsonArray);
		String sendData = json.toString();
	
		resp.getWriter().println(sendData);
		database.Close();
	}
	
	// Like a photo
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Recieve JSON post data
		BufferedReader bin = new BufferedReader(req.getReader());
		String jsonString = bin.readLine();

		// Parse the JSON data from post
		String username = null;
		int photoID = 0;
		JSONParser j = new JSONParser();
		try {
			JSONObject o = (JSONObject) j.parse(jsonString);
			username = (String) o.get("username");
			photoID = Integer.parseInt(o.get("photoID").toString());

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Connect to database and attempt to register user
		Database database = new Database();
		if (database.LikePicture(username, photoID)) {
			resp.getWriter().println("SUCCESS");
		} else {
			resp.getWriter().println("FAIL"); // Name Taken
		}

		resp.getWriter().close();
	}
	
}
