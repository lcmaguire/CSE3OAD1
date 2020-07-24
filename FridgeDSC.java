import java.sql.*;
import java.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/*
	Liam Maguire 18344533
	18344533@students.latrobe.edu.au
	CSE3OAD
*/

public class FridgeDSC {

	// the date format we will be using across the application
	public static final String DATE_FORMAT = "dd/MM/yyyy";

	/*
		FREEZER, // freezing cold
		MEAT, // MEAT cold
		COOLING, // general fridge area
		CRISPER // veg and fruits section

		note: Enums are implicitly public static final
	*/
	public enum SECTION {
		FREEZER,
		MEAT,
		COOLING,
		CRISPER
	};

	private static Connection connection;
	private static Statement statement;
	private static PreparedStatement preparedStatement;

	public static void connect() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");


			/* TODO 1-01 - TO COMPLETE ****************************************
			 * change the value of the string for the following 3 lines:
			 * - url jdbc:mysql://latcs7.cs.latrobe.edu.au:3306/userid
			 * - user
			 * - password
			 */			
			
			String url = "jdbc:mysql://latcs7.cs.latrobe.edu.au:3306/putuseridhere";
			String user = "put user id";
			String password = "put password here";

			connection = DriverManager.getConnection(url, user, password);
			statement = connection.createStatement();
			//System.out.println("test");
  		} catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}		
	}

	public static void disconnect() throws SQLException {
		if(preparedStatement != null) preparedStatement.close();
		if(statement != null) statement.close();
		if(connection != null) connection.close();
	}



	public Item searchItem(String name) throws Exception {
		
		//connect();
		String queryString = "SELECT * FROM item WHERE name = ?";


		//String queryString = "select * from music_album where id = ?";
        preparedStatement = connection.prepareStatement(queryString);
        preparedStatement.setString(1, name);
        ResultSet rs = preparedStatement.executeQuery();

		Item item = null;

		if (rs.next()) { // i.e. the item exists
		
			 item = new Item(
				rs.getString("name"),
				rs.getBoolean("expires")
                
			 );

		}	
		return item;
	}

	public Grocery searchGrocery(int id) throws Exception {
		
		//connect();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
		
		String queryString = "SELECT * FROM grocery WHERE id = ?";
		
		preparedStatement = connection.prepareStatement(queryString);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();

		Grocery grocery = null;

		if (rs.next()) { // i.e. the grocery exists

			 String name = rs.getString("itemName");
			 Item temp = searchItem(name);
			 
			 if (temp == null) {
				temp = new Item(name);
				}
			 LocalDate date = LocalDate.parse(rs.getString("date"), dtf);
			 int quantity = rs.getInt("quantity");
			 SECTION section = SECTION.valueOf(rs.getString("section"));
			 
			 grocery = new Grocery(id, temp, date, quantity, section);

		}

		return grocery;
	}

	public List<Item> getAllItems() throws Exception {
		String queryString = "SELECT * FROM item";

		preparedStatement = connection.prepareStatement(queryString);
        ResultSet rs = preparedStatement.executeQuery();

		List<Item> items = new ArrayList<Item>();
		Item item;
		 
		while (rs.next()) { // i.e. the grocery exists
			item = new Item(
				rs.getString("name"),
				rs.getBoolean("expires")
                
			 );
			
			items.add(item);
		}
		return items;
	}

	public List<Grocery> getAllGroceries() throws Exception {
		
		//connect();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
		String queryString = "SELECT * FROM grocery";
		
		preparedStatement = connection.prepareStatement(queryString);
        ResultSet rs = preparedStatement.executeQuery();
		
		List<Grocery> groceries = new ArrayList<Grocery>();
		Grocery grocery;
		
		while (rs.next()) { // i.e. the grocery exists

			 int id = rs.getInt("id");
			 String name = rs.getString("itemName");
			 Item temp = searchItem(name);
			 // making a new item if null as in samples from assignment handout ice cream is included when not part of items table
			 if (temp == null) {
				temp = new Item(name);
				}
			 LocalDate date = LocalDate.parse(rs.getString("date"), dtf);
			 int quantity = rs.getInt("quantity");
			 SECTION section = SECTION.valueOf(rs.getString("section"));
			 
			 grocery = new Grocery(id, temp, date, quantity, section);
			
			groceries.add(grocery);

		}
		return groceries;
	}


	public int addGrocery(String name, int quantity, SECTION section) throws Exception {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_FORMAT);
		LocalDate date = LocalDate.now();
		String dateStr = date.format(dtf);
		
		Item temp = searchItem(name);
		
		if(temp == null){
			String msg = "Item name " + name + " doesn't exist in item table";
            System.out.println("\nERROR: " + msg);
            throw new Exception(msg);
		}
		
		if(quantity < 1){
			String msg = "Less Than Minimum Quantity 1 ";
            System.out.println("\nERROR: " + msg);
            throw new Exception(msg);
		}

		String command = "INSERT INTO grocery VALUES(?, ?, ?, ?, ?)";
		
		preparedStatement = connection.prepareStatement(command);
        preparedStatement.setInt(1, 0);
		preparedStatement.setString(2, name);
        preparedStatement.setString(3, dateStr);
        preparedStatement.setInt(4, quantity);
        preparedStatement.setString(5, section.toString());
        
        preparedStatement.executeUpdate();

		// retrieving & returning last inserted record id
		ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ID()");
		rs.next();
		int newId = rs.getInt(1);

		//disconnect();
		return newId;		
	}

	public Grocery useGrocery(int id) throws Exception {
		 Grocery temp = searchGrocery(id);
		 
		 // if null doesnt exist
		 if(temp == null)
		 {
			throw new Exception(temp.getItemName() + " doesnt exist");
		 }
		 
		 // check if above quantity
		 if(temp.getQuantity() <=1){
			 throw new Exception(temp.getItemName() + " is below minimum quantity");
		 }


		
		String queryString = 
			"UPDATE grocery " +
			"SET quantity = quantity - 1 " +
			"WHERE quantity > 1 " + 
			"AND id = " + id + ";";
			
		preparedStatement = connection.prepareStatement(queryString);
		
		
		if(preparedStatement.executeUpdate() > 0){
			return searchGrocery(id);
		}else{
			throw new Exception("Something went wrong with update of  " + temp.getItemName());
		}

	}

	public int removeGrocery(int id) throws Exception {
		String queryString = "DELETE FROM grocery WHERE id = " + id + ";";

		 Grocery temp = searchGrocery(id);
		 
		 // if null doesnt exist CHANGE TO EXCEPTION
		 if(temp == null)
		 {
			 return -1;
		 }
		 
		 preparedStatement = connection.prepareStatement(queryString);
		// returns 1 if successful and -1 if fail
		 return preparedStatement.executeUpdate();
	}

	// STATIC HELPERS -------------------------------------------------------

	public static long calcDaysAgo(LocalDate date) {
    	return Math.abs(Duration.between(LocalDate.now().atStartOfDay(), date.atStartOfDay()).toDays());
	}

	public static String calcDaysAgoStr(LocalDate date) {
    	String formattedDaysAgo;
    	long diff = calcDaysAgo(date);

    	if (diff == 0)
    		formattedDaysAgo = "today";
    	else if (diff == 1)
    		formattedDaysAgo = "yesterday";
    	else formattedDaysAgo = diff + " days ago";	

    	return formattedDaysAgo;			
	}

	// To perform some quick tests	
	public static void main(String[] args) throws Exception {
		FridgeDSC myFridgeDSC = new FridgeDSC();

		myFridgeDSC.connect();

		System.out.println("\nSYSTEM:\n");

		System.out.println("\n\nshowing all of each:");
		System.out.println(myFridgeDSC.getAllItems());
		System.out.println(myFridgeDSC.getAllGroceries());

		myFridgeDSC.disconnect();
	}
}