package com.zonesnap.server;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		
		if (requestType.equalsIgnoreCase(new String("profile"))) {
			// Return the user's profile data
			String email = req.getParameter("user");
			jsonData = database.RetrieveUserProfile(email);
		} else {
			resp.getWriter().println("Unknown Request");
		}

		resp.setContentType("text/plaintext");

		resp.getWriter().println(jsonData);
		resp.getWriter().close();
	}

}
