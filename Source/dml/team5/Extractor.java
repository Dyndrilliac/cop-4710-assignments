package dml.team5;

import java.sql.*;

public class Extractor {
	public void main(Connection connection, String input) throws SQLException {
		int getName = 1;
		String columnName[][];
		String recordInfo[][];

		Connection conn = connection;

		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(input);
		ResultSetMetaData rsmd = rset.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		columnName = new String[columnsNumber][3]; // creates an array with the
													// attribute names
		int count = 0;

		// while and if statement to figure out the row length
		while (rset.next()) {
			++count; // Get data from the current row and use it
		}

		if (count == 0) {
			System.out.println("No records found");
		}

		int newCount = 0;

		// prints out the attribute names and also add them to the columnName
		// array for later use for XML
		while (getName <= columnsNumber) {
			System.out.print(rsmd.getColumnLabel(getName) + " ");
			columnName[newCount][0] = rsmd.getColumnLabel(getName);
			int colType = rsmd.getColumnType(getName);
			columnName[newCount][1] = rsmd.getColumnLabel(getName);
			System.out.println("Column " + getName + " is type " + colType);
			colType = rsmd.getColumnDisplaySize(getName);
			columnName[newCount][2] = rsmd.getColumnLabel(getName);
			System.out.println(colType);
			getName++;
			newCount++;
		}
		System.out.println(" ");
		// creates the recordInfo array size
		recordInfo = new String[count][columnsNumber];
		rset = stmt.executeQuery(input);
		newCount = 0;

		// gets the records from the system call to oracle.
		// sets array with its the records information
		while (rset.next()) {
			int temp = 1;
			while (temp <= columnsNumber) {
				System.out.print(rset.getString(temp) + " ");
				recordInfo[newCount][temp - 1] = rset.getString(temp).trim();
				temp++;

			}
			System.out.println(" ");
			newCount++;

		}
		// closes statement
		stmt.close();

	}

}
