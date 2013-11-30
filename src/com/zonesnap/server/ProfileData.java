package com.zonesnap.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ProfileData extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Database database = new Database();
		
		// Determine type of request
		String requestType = req.getParameter("type");
		String jsonData = null;
		
		if (requestType.equalsIgnoreCase(new String("profile"))) {
			// Return the user's profile data
			String email = req.getParameter("user");
			jsonData = database.RetrieveUserProfile(email);
		} else if (requestType.equalsIgnoreCase(new String("users"))) {
			// Return list of usernames
			List<String> usernames = database.GetUsernames();
			JSONObject json = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for( Iterator<String> i = usernames.iterator(); i.hasNext();) {
				String item = i.next();
				jsonArray.add(item);
			}
			json.put("users", jsonArray);
			jsonData = json.toString();
		} else {
			resp.getWriter().println("Unknown Request");
		}

		resp.setContentType("text/plaintext");

		System.out.println(jsonData);
		resp.getWriter().println(jsonData);
		resp.getWriter().close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String email = req.getParameter("email");
		
		// FUTURE USE: Update profile infomation
	}

}
