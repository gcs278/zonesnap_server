package com.zonesnap.server;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// This servlet is for retrieving and uploading a picture to database
public class PictureUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Database database = new Database();
		int photoID = 0;
		
		// Get email and check
		try {
			photoID = Integer.parseInt(req.getParameter("photoID"));
		} catch(NumberFormatException e) {
			resp.getWriter().println("Error: Incorrect usage");
			return;
		}
		if (photoID == -1) {
			resp.getWriter().println("Error: Incorrect usage");
			return;
		} 
//		else if (!database.GetUsernames().contains(photoID)) {
//			resp.getWriter().println("Error: Invalid username");
//			return;
//		}
		System.out.println(photoID);
		String imageBase64 = null;
		try {
			// Retrieve the picture and convert to base64
			imageBase64 = new String(database.RetrievePicture(photoID),
					"UTF8");
		} catch (RuntimeException e) {
			resp.getWriter().println(e.getMessage());
			return;
		}
		
		resp.setContentType("text/plaintext");

		// String img =
		// "<img alt=\"250836_3676541106856_925123077_n.jpg\"src=\"data:image/jpeg;base64,";
		// img += ImageDatabase + "\"/>";

		resp.getWriter().println(imageBase64);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Get email & check
		String username = req.getParameter("username");
		float latitude = Float.parseFloat(req.getParameter("lat"));
		float longitude = Float.parseFloat(req.getParameter("long"));
		
		if (username == null || latitude == 0.0f || longitude == 0.0f) {
			resp.getWriter().println("Error: Incorrect usage");
			return;
		}

		// Get the image string that has been sent
		BufferedReader bin = new BufferedReader(req.getReader());
		String imageBase64 = bin.readLine();

		System.out.println(imageBase64);
		byte[] imageBytes = imageBase64.getBytes("UTF8");

		// Upload the image to the database
		Database database = new Database();
		if (database.UploadPicture(imageBytes, username, latitude, longitude))
			resp.getWriter().println("OK");
		else
			resp.getWriter().println("FAIL");

	}

}
