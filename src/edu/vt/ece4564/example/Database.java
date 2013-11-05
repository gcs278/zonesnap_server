package edu.vt.ece4564.example;

import java.sql.Blob;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

public class Database {
	java.sql.Connection connection;

	Database() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager
					.getConnection("jdbc:mysql://localhost/assignment_1?"
							+ "user=grcosp&password=CheeseBurger2900?");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Get the list of usernames
	List<String> GetUsernames() {
		List<String> usernames = new ArrayList<String>();
		try {
			// Query Database for all usernames
			Statement statement = connection.createStatement();
			String query = "SELECT  `username` FROM  `accounts`";
			ResultSet rs = statement.executeQuery(query);

			// Get usernames
			while (rs.next())
				usernames.add(rs.getString("username"));

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return usernames;
	}

	// Authorize a user, return OK or FAIL
	String LoginUser(String username, String password, double latitude,
			double longitude) {
		// Authorize the user
		if (GetUsernames().contains(username)) {
			updateLocation(latitude, longitude, username);
			return "OK";
		} else {
			return "FAIL";
		}
	}

	private void updateLocation(double latitude, double longitude, String email) {
		try {
			Statement statement = connection.createStatement();
			String query = "UPDATE `accounts` SET `latitude_last`=" + latitude
					+ ",`longitude_last`=" + longitude + "WHERE `username`='"
					+ email + "'";
			statement.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean RegisterUser(String name, String email, String instrument,
			String password) {
		// Check if email already exist in database
		if (GetUsernames().contains(email)) {
			return false;
		}

		// Insert user into database
		try {
			String query = "INSERT INTO `accounts` (`name`,`username`,`instrument`,`password`) VALUES (?,?,?,?)";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setString(1, name);
			prepared.setString(2, email);
			prepared.setString(3, instrument);
			prepared.setString(4, password);
			prepared.execute(); // Insert

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return true;
	}

	// Upload a picture for the corresponding user
	boolean UploadPicture(byte[] image, String email) {
		// Check if the username exists
		if (!GetUsernames().contains(email)) {
			return false;
		}
		try {
			String query = "UPDATE `accounts` SET `picture`= ? WHERE `username` = '"
					+ email + "'";
			PreparedStatement prepared = connection.prepareStatement(query);
			// Insert image bytes into query
			prepared.setBytes(1, image);
			prepared.execute(); // Insert

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return false;
		}
		return true;
	}

	byte[] RetrievePicture(String email) {
		byte[] imageBytes = null;

		try {
			// Build query to get picture for current user
			String query = "SELECT  `picture` FROM  `accounts` WHERE `username` = ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setString(1, email);
			// Execute
			ResultSet rs = prepared.executeQuery();

			Blob ImageBlob = null;
			// Check if we have a result
			if (rs.next())
				ImageBlob = rs.getBlob("picture"); // Getting binary data
			else
				return null;
			
			if (ImageBlob == null)
				throw new RuntimeException("No picture available");
			
			// Converting blob to byte array
			imageBytes = ImageBlob.getBytes(1, (int) ImageBlob.length());

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}

		return imageBytes;
	}

	boolean UploadSound(byte[] sound, String email) {
		try {
			String query = "UPDATE `accounts` SET `recording`= ? WHERE `username` = '"
					+ email + "'";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setBytes(1, sound);
			prepared.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return false;
		}
		return true;
	}

	byte[] RetrieveSound(String email) {
		byte[] soundBytes = null;

		try {
			// Build query to get picture for current user
			String query = "SELECT  `recording` FROM  `accounts` WHERE `username` = ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setString(1, email);
			// Execute
			ResultSet rs = prepared.executeQuery();

			Blob soundBlob = null;
			// Check if we have a result
			if (rs.next())
				soundBlob = rs.getBlob("recording"); // Getting binary data
			else
				return null;

			// Converting blob to byte array
			soundBytes = soundBlob.getBytes(1, (int) soundBlob.length());
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}

		return soundBytes;
	}

	// Retrieve the user's profile data in JSON format
	@SuppressWarnings("unchecked")
	String RetrieveUserProfile(String email) {
		// Variables that we will retreiving
		String name = null, instrument = null;
		String latitude = null, longitude = null;
		JSONObject json = new JSONObject();
		
		// Query the database
		try {
			Statement statement = connection.createStatement();

			// Query and execute
			String query = "SELECT  `name`,`instrument`,`latitude_last`,`longitude_last` FROM  `accounts` WHERE `username` = '"
					+ email + "'";
			ResultSet rs = statement.executeQuery(query);

			// If user exists
			if (rs.next()) {
				name = rs.getString("name");
				instrument = rs.getString("instrument");
				longitude = String.valueOf(rs.getDouble("longitude_last"));
				latitude = String.valueOf(rs.getDouble("latitude_last"));
			} else {
				return null;
			}

			// Create the JSON object that we will return
			json.put("name", name);
			json.put("instrument", instrument);
			json.put("latitude", latitude);
			json.put("longitude", longitude);
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return json.toJSONString();
	}
}
