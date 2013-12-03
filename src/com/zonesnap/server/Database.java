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
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Register/Login a user
	boolean RegisterUser(String username) {
		if (UserExists(username)) {
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
		} else {
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

			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Query Failed: " + e.getMessage());
			}
		}
		return true;
	}

	// Function to determine if user exists
	boolean UserExists(String username) {
		boolean exists = false;
		// See if user exists
		try {
			String query = "SELECT * FROM `users` WHERE `username` = ?";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setString(1, username);

			prepared.execute();

			// Execute
			ResultSet rs = prepared.executeQuery();

			// Check if we have a result
			if (rs.next())
				return true;
			else
				return false;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}

		return exists;
	}

	// Upload a picture for the corresponding user
	boolean UploadPicture(byte[] image, String title, int userID,
			double latitude, double longitude) {

		// See if zone exists, create zone if doesn't
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
			prepared.setDouble(4, latitude);
			prepared.setDouble(5, longitude);
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

	// Retrieve a certain picture
	String RetrievePicture(int photoID) {
		byte[] imageBytes = null;
		String title = null;
		int likes = 0;
		try {
			// Build query to get picture for current user
			String query = "SELECT  `picture_blob`,`title`,`likes` FROM  `pictures` WHERE `idpictures` = ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setInt(1, photoID);
			// Execute
			ResultSet rs = prepared.executeQuery();

			Blob ImageBlob = null;

			// Check if we have a result
			if (rs.next()) {
				ImageBlob = rs.getBlob("picture_blob"); // Getting binary data
				title = rs.getString("title");
				likes = rs.getInt("likes");
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

		// Put the data as JSON
		JSONObject json = new JSONObject();
		try {
			json.put("image", new String(imageBytes, "UTF-8"));
			json.put("title", title);
			json.put("likes", likes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return json.toJSONString();
	}

	// Retrieve the user's profile data in JSON format
	@SuppressWarnings("unchecked")
	String RetrieveUserProfile(String username) {
		// Variables that we will retreiving
		int total_likes = 0, zones_crossed, ranking = 0;
		JSONObject json = new JSONObject();

		// Query the database
		try {
			// Query and execute
			String query = "SELECT  `total_likes`,`zones_crossed` FROM  `users` WHERE `username` = ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setString(1, username);

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

		// Query the database for ranking
		try {
			// Query and execute
			String query = "SELECT COUNT(*)+1 AS rank FROM `pictures` where `likes` > ?";
			PreparedStatement prepared = connection.prepareStatement(query);
			prepared.setInt(1, total_likes);

			ResultSet rs = prepared.executeQuery();

			// If user exists
			if (rs.next())
				ranking = rs.getInt("rank");

			json.put("ranking", ranking);

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
		int userId = GetUserID(username);
		// Add photo to User's likes
		try {
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

		// Give the photo +1 likes
		try {
			String query = "UPDATE `users` SET `total_likes`= total_likes+1 WHERE `id` = ?";
			java.sql.PreparedStatement prepared = connection
					.prepareStatement(query);
			prepared.setInt(1, userId);
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
	public int GetUserID(String username) {
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

	// Get locations of photos for google map
	List<String> GetPhotoLocations() {
		List<String> coorList = new ArrayList<String>();
		try {
			String query = "SELECT  `idpictures`,`latitude`,`longitude`,`title` FROM  `pictures`";

			PreparedStatement statement = connection.prepareStatement(query);

			ResultSet rs = statement.executeQuery();

			// Get usernames
			while (rs.next()) {
				int id = rs.getInt("idpictures");
				double latitude = rs.getDouble("latitude");
				double longitude = rs.getDouble("longitude");
				String title = rs.getString("title");
				String data = id + "," + String.valueOf(latitude) + ","
						+ String.valueOf(longitude) + "," + title;
				coorList.add(data);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return coorList;
	}

	// Get the list of usernames
	List<Integer> GetPhotoIDs(double latitude, double longitude, String order) {
		List<Integer> photoIDs = new ArrayList<Integer>();
		// Locate Zone
		int zoneID = LocateZone(latitude, longitude);

		// If no zone, no pictures
		if (zoneID == -1) {
			return photoIDs;
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
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return photoIDs;
	}

	// Get the list of personal pictures
	List<Integer> GetPersonalPhotos(String username) {
		List<Integer> photoIDs = new ArrayList<Integer>();

		try {
			String query = "SELECT  `idpictures` FROM  `pictures` LEFT JOIN `users` ON pictures.users_id = users.id WHERE `username` = ? ORDER BY `date` DESC";

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
			e.printStackTrace();
			System.out.println("Query Failed: " + e.getMessage());
		}
		return photoIDs;
	}

	// Get User ID from string username
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

	// Update that user has crossed more zones
	boolean UpdateZonesCrossed(String username, double latitude,
			double longitude) {
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
