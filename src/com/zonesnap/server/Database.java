package com.zonesnap.server;

import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.sql.Blob;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Spring;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class Database {
	java.sql.Connection connection;

	Database() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager
					.getConnection("jdbc:mysql://localhost/zonesnap?"
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

	boolean RegisterUser(String username) {
		// Attempt to insert into database
		try {
			String query = "INSERT INTO `users` (`username`,`last_login`) VALUES (?,?)";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setString(1, username);

			// Set the current date
			java.util.Date today = new java.util.Date();
			Timestamp currentTimestamp = new Timestamp(today.getTime());
			prepared.setTimestamp(2, currentTimestamp);

			prepared.execute(); // Insert

		} catch (MySQLIntegrityConstraintViolationException e) {
			// Means that user is registered
			// Update his last login
			String query = "UPDATE `users` SET `last_login` = CURRENT_TIMESTAMP WHERE `username` = ?";
			java.sql.PreparedStatement prepared;
			try {
				prepared = connection.prepareStatement(query);
				prepared.setString(1, username);
				prepared.execute();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return true;
	}

	// Upload a picture for the corresponding user
	boolean UploadPicture(byte[] image, String title, int userID,
			double latitude, double longitude) {

		int zoneID = LocateZone(latitude, longitude);
		if (zoneID == -1) {
			CreateZone(latitude, longitude);
		}

		try {
			String query = "INSERT INTO `pictures` (`users_id`,`title`,`picture_blob`,`latitude`,`longitude`,`likes`,`zones_id`) VALUES (?,?,?,?,?,0,?)";
			PreparedStatement prepared = connection.prepareStatement(query);
			// Insert image bytes into query
			prepared.setInt(1, userID);
			prepared.setString(2, title);
			prepared.setBytes(3, image);
			prepared.setDouble(4, 80.38);
			prepared.setDouble(5, -80.2098);
			prepared.setInt(6, zoneID);
			prepared.execute(); // Insert

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Trys to find the zone that the picture was taken in
	int LocateZone(double latitude, double longitude) {
		int zoneID = -1;
		try {
			// Query for seeing if longitude and latitude are betwen the ranges
			String query = "SELECT  * FROM  `zones` WHERE ? BETWEEN `longitude_end` and `longitude_start` and ? BETWEEN `latitude_start` and `latitude_end`";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setDouble(1, longitude);
			prepared.setDouble(2, latitude);
			// Execute
			ResultSet rs = prepared.executeQuery();

			// Check if we have a result
			if (rs.next())
				zoneID = rs.getInt("idzones");
			else
				return zoneID;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return zoneID;
	}

	// If zone doesn't exist, create a new zone
	void CreateZone(double latitude, double longitude) {
		// Drop precision on latitude and long
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.DOWN);

		// Lower range
		double latitude_low = Double.parseDouble(df.format(latitude));
		double longitude_low = Double.parseDouble(df.format(longitude));

		// Upper range
		double latitude_high = latitude_low + 0.001;
		double longitude_high = longitude_low - 0.001;

		try {
			String query = "INSERT INTO `zones` (`latitude_start`,`latitude_end`,`longitude_start`,`longitude_end`) VALUES (?,?,?,?)";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setDouble(1, latitude_low);
			prepared.setDouble(2, latitude_high);
			prepared.setDouble(3, longitude_low);
			prepared.setDouble(4, longitude_high);

			prepared.execute(); // Insert

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
	}

	String RetrievePicture(int photoID) {
		byte[] imageBytes = null;
		String title = null;
		try {
			// Build query to get picture for current user
			String query = "SELECT  `picture_blob`,`title` FROM  `pictures` WHERE `idpictures` = ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setInt(1, photoID);
			// Execute
			ResultSet rs = prepared.executeQuery();

			Blob ImageBlob = null;

			// Check if we have a result
			if (rs.next()) {
				ImageBlob = rs.getBlob("picture_blob"); // Getting binary data
				title = rs.getString("title");
			} else
				return null;

			if (ImageBlob == null)
				throw new RuntimeException("No picture available");

			// Converting blob to byte array
			imageBytes = ImageBlob.getBytes(1, (int) ImageBlob.length());

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		JSONObject json = new JSONObject();
		try {
			json.put("image", new String(imageBytes, "UTF-8"));
			json.put("title", title);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return json.toJSONString();
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
	String RetrieveUserProfile(String username) {
		// Variables that we will retreiving
		int total_likes, zones_crossed;
		JSONObject json = new JSONObject();

		// Query the database
		try {
			// Query and execute
			String query = "SELECT  `total_likes`,`zones_crossed` FROM  `accounts` WHERE `username` = ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setString(0, username);

			ResultSet rs = prepared.executeQuery();

			// If user exists
			if (rs.next()) {
				total_likes = rs.getInt("total_likes");
				zones_crossed = rs.getInt("zones_crossed");
			} else {
				return null;
			}

			// Create the JSON object that we will return
			json.put("total_likes", total_likes);
			json.put("zones_crossed", zones_crossed);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return json.toJSONString();
	}

	// Plus +1 to the photo and add photo to user's favorites
	boolean LikePicture(String username, int photoID) {

		// Give the photo +1 likes
		try {
			String query = "UPDATE `pictures` SET `likes`= likes+1 WHERE `idpictures` = ?";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setInt(1, photoID);
			prepared.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return false;
		}

		// Add photo to User's likes
		try {
			int userId = GetUserID(username);

			String query = "INSERT INTO `user_favorites` (`pictures_id`,`users_id`,`date`) VALUES (?,?,?)";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setInt(1, photoID);
			prepared.setInt(2, userId);

			// Set the current date
			java.util.Date today = new java.util.Date();
			Timestamp currentTimestamp = new Timestamp(today.getTime());
			prepared.setTimestamp(3, currentTimestamp);

			prepared.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return false;
		}

		return true;
	}

	// Gets the user's ID from username string
	private int GetUserID(String username) {
		int userID = -1;
		try {
			String idQuery = "SELECT  `id` FROM  `users` WHERE `username` = ?";
			java.sql.PreparedStatement preparedId = connection
					.prepareStatement(idQuery);
			preparedId.setString(1, username);
			ResultSet rs = preparedId.executeQuery();

			// Check if we have a result
			if (rs.next())
				userID = rs.getInt("id"); // Getting ID

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return -1;
		}
		return userID;
	}

	// Get the list of usernames
	List<Integer> GetPhotoIDs(double latitude, double longitude, String order) {
		List<Integer> photoIDs = new ArrayList<Integer>();
		// Locate Zone
		int zoneID = LocateZone(latitude, longitude);

		// If no zone, no pictures
		if (zoneID == -1) {
			return photoIDs; //
		}

		try {
			String query = "SELECT  `idpictures` FROM  `pictures` WHERE `zones_id` = ? ";
			if (order.equalsIgnoreCase(new String("likes"))) {
				query += "ORDER BY `likes` DESC";
			} else {
				query += "ORDER BY `date` DESC";
			}

			PreparedStatement statement = connection.prepareStatement(query);
			statement.setInt(1, zoneID);

			ResultSet rs = statement.executeQuery();

			// Get usernames
			while (rs.next())
				photoIDs.add(rs.getInt("idpictures"));

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return photoIDs;
	}

	// Get the list of usernames
	List<Integer> GetLikedPhotos(String username) {
		List<Integer> photoIDs = new ArrayList<Integer>();

		try {
			String query = "SELECT  `pictures_id` FROM  `user_favorites` LEFT JOIN `users` ON user_favorites.users_id = users.id WHERE `username` = ? ORDER BY `date` DESC";

			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);

			ResultSet rs = statement.executeQuery();

			// Get usernames
			while (rs.next()) {
				int photo = rs.getInt("pictures_id");
				if (!photoIDs.contains(photo)) {
					photoIDs.add(photo);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return photoIDs;
	}
	
	private int getUserId(String username) {
		int userId = 0;
		try {
			String query = "SELECT  `id` FROM  `users` WHERE `username` = ?";

			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);

			ResultSet rs = statement.executeQuery();

			// Get usernames
			while (rs.next()) {
				userId = rs.getInt("id");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return userId;
	}

	boolean UpdateZonesCrossed(String username, double latitude, double longitude) {
		int zoneID = LocateZone(latitude, longitude);
		if (zoneID == -1) {
			CreateZone(latitude, longitude);
		}
		int userID = getUserId(username);
		if (userID == 0) {
			return false;
		}
		// Add photo to User's likes
		try {

			String query = "INSERT INTO `zones_crossed` (`users_id`,`zones_id`) VALUES (?,?)";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setInt(1, userID);
			prepared.setInt(2, zoneID);

			prepared.execute();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
			return false;
		}

		return true;

	}

	// Get the list of usernames
	List<Integer> GetPastPhotos(String username) {
		List<Integer> photoIDs = new ArrayList<Integer>();

		try {
			String query = "SELECT pictures.idpictures FROM `zones_crossed` "
					+ "LEFT JOIN `users` ON users.id = zones_crossed.users_id "
					+ "LEFT JOIN `pictures` ON pictures.zones_id = zones_crossed.zones_id "
					+ "WHERE zones_crossed.date >= now() - INTERVAL 1 DAY "
					+ "AND users.username = ?";

			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1, username);

			ResultSet rs = statement.executeQuery();

			// Get usernames
			while (rs.next()) {
				int photo = rs.getInt("idpictures");
				if (!photoIDs.contains(photo)) {
					photoIDs.add(photo);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return photoIDs;
	}

	// Close the database connection
	void Close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
