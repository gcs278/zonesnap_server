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
public class ZoneLookup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		
		Double latitude;
		Double longitude;
		
		// Determine type of request
		try {
			latitude = Double.parseDouble(req.getParameter("lat"));
			longitude = Double.parseDouble(req.getParameter("long"));
		} catch (NullPointerException e) {
			resp.getWriter().println(
					"Error: No latitude or longitude specified");
			return;
		} catch( NumberFormatException e) {
			resp.getWriter().println(
					"Error: Incorrect format for lat or long");
			return;
		}

		Database database = new Database();

		JSONObject json = new JSONObject();
		int zone = database.LocateZone(latitude, longitude);
		if ( zone == -1 ) {
			database.CreateZone(latitude, longitude);
			zone = database.LocateZone(latitude, longitude);
		}
		
		json.put("zone", zone);

		resp.getWriter().println(json.toJSONString());
	}
}
