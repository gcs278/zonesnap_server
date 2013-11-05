package edu.vt.ece4564.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Blob;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// This class is for sound retrieval and upload
public class SoundUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// NOT IN USE
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String email = req.getParameter("email");

		// Get the sound
		Database database = new Database();
		String soundBase64 = new String(database.RetrieveSound(email), "UTF8");

		resp.setContentType("text/plaintext");
		resp.getWriter().println(soundBase64);
	}

	// Upload a sound byte to database
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Get username
		String email = req.getParameter("email");

		// Read sound bytes
		BufferedReader bin = new BufferedReader(req.getReader());
		String soundString = bin.readLine();

		// Decode to byte array
		System.out.println(soundString);
		byte[] decodedSound = soundString.getBytes("UTF8");

		// Insert in database
		Database database = new Database();
		database.UploadSound(decodedSound, email);

	}

}
