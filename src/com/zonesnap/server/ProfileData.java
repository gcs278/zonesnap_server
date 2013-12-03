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

	// Get Profile data
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Database database = new Database();

		// Determine type of request
		String requestType = req.getParameter("type");
		String jsonData = null;
		// Return the user's profile data
		String username = req.getParameter("user");
		if (username == null) {
			resp.getWriter().println("Unknown Request");
			return;
		}
		if (requestType.equalsIgnoreCase(new String("profile"))) {
			jsonData = database.RetrieveUserProfile(username);
		} else if (requestType.equalsIgnoreCase(new String("pics"))) {
			// Return list of photoIDs
			List<Integer> photoIDs = database.GetPersonalPhotos(username);
			JSONObject json = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for (Iterator<Integer> i = photoIDs.iterator(); i.hasNext();) {
				String item = i.next().toString();
				jsonArray.add(item);
			}
			
			// Send data
			json.put("photoIDs", jsonArray);
			jsonData = json.toString();
		} else {
			resp.getWriter().println("Unknown Request");
			return;
		}

		resp.setContentType("text/plaintext");

		resp.getWriter().println(jsonData);
		resp.getWriter().close();
	}

}
