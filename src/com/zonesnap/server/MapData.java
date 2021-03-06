package com.zonesnap.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

// This servlet is for getting map data
public class MapData extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
			// Return list of photoIDs
			List<String> coors = database.GetPhotoLocations();
			JSONObject json = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for (Iterator<String> i = coors.iterator(); i.hasNext();) {
				String item = i.next().toString();
				jsonArray.add(item);
			}
			json.put("pics", jsonArray);
			jsonData = json.toString();
		} else if (requestType.equalsIgnoreCase(new String("zones"))) {

		} else {
			return;
		}
		
		resp.getWriter().println(jsonData);
		database.Close();
	}
}
