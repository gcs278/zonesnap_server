package com.zonesnap.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// This servlet is for retrieving and uploading a picture to database
public class PictureUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private void PrintError(HttpServletResponse resp, String error)
			throws IOException {
		resp.getWriter().println("Error: Incorrect API usage");
		resp.getWriter().println("Message: " + error);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		Database database = new Database();

		// Determine type of request
		String requestType = req.getParameter("type");
		if (requestType == null) {
			PrintError(resp, "Type was not specified");
			return;
		}
		String sendData = null;

		// See what type of request
		if (requestType.equalsIgnoreCase(new String("list"))) {
			String order;
			double latitude, longitude;
			try {
				order = req.getParameter("order");
				if (order == null) {
					PrintError(resp, "Order was not specified");
					return;
				} else if (!order.equalsIgnoreCase(new String("date"))
						&& !order.equalsIgnoreCase(new String("likes"))) {
					PrintError(resp, "Order type is invalid");
					return;
				}
				latitude = Double.parseDouble(req.getParameter("lat"));
				longitude = Double.parseDouble(req.getParameter("long"));
			} catch (NumberFormatException e) {
				PrintError(resp, e.getMessage());
				return;
			} catch (NullPointerException e) {
				PrintError(resp, "No value for lat or long");
				return;
			}

			// Return list of photoIDs
			List<Integer> photoIDs = database.GetPhotoIDs(latitude, longitude,
					order);
			JSONObject json = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for (Iterator<Integer> i = photoIDs.iterator(); i.hasNext();) {
				String item = i.next().toString();
				jsonArray.add(item);
			}
			json.put("photoIDs", jsonArray);
			sendData = json.toString();

		} else if (requestType.equalsIgnoreCase(new String("get"))) {
			// Get a photo per ID
			int photoID = 0;
			try {
				photoID = Integer.parseInt(req.getParameter("photoID"));
			} catch (NumberFormatException e) {
				resp.getWriter().println("Error: Incorrect usage");
				return;
			}
			if (photoID == -1) {
				resp.getWriter().println("Error: Incorrect usage");
				return;
			}

			try {
				// Retrieve the picture and convert to base64
				sendData = database.RetrievePicture(photoID);
			} catch (RuntimeException e) {
				resp.getWriter().println(e.getMessage());
				return;
			}
			
		} else {
			resp.getWriter().println("Unknown Request");
			return;
		}

		// else if (!database.GetUsernames().contains(photoID)) {
		// resp.getWriter().println("Error: Invalid username");
		// return;
		// }

		resp.setContentType("text/plaintext");

		// String img =
		// "<img alt=\"250836_3676541106856_925123077_n.jpg\"src=\"data:image/jpeg;base64,";
		// img += ImageDatabase + "\"/>";

		resp.getWriter().println(sendData);
		database.Close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Get email & check

		// Get the image string that has been sent
		BufferedReader bin = new BufferedReader(req.getReader());
		String json = bin.readLine();
		String image = "";
		String title = "";
		double latitude = 0, longitude = 0;
		try {
			JSONParser j = new JSONParser();
			JSONObject o;

			o = (JSONObject) j.parse(json);
			image = (String) o.get("image");
			title = (String) o.get("title");
			latitude = Double.parseDouble(o.get("lat").toString());
			longitude = Double.parseDouble(o.get("long").toString());
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			resp.getWriter().println("FAIL");
			return;
		}
		
		byte[] imageBytes = image.getBytes("UTF8");

		// Upload the image to the database
		Database database = new Database();
		if (database.UploadPicture(imageBytes, title, 1, latitude, longitude))
			resp.getWriter().println("OK");
		else
			resp.getWriter().println("FAIL");
		database.Close();
	}
}
