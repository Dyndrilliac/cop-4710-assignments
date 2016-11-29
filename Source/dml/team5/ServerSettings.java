
package dml.team5;

/**
 * <p>
 * This helper class contains the configuration settings for the SQL relational
 * database server.
 * </p>
 * 
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 */
public final class ServerSettings {
	private static String database = "dworcl";
	private static String password = "ravenpuff";
	private static String port = "1521";
	private static String server = "olympia.unfcsd.unf.edu";
	private static String username = "hogwarts";

	/**
	 * <p>
	 * Returns the database name {@link java.lang.String}.
	 * </p>
	 * 
	 * @return the database name {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final String getDatabase() {
		return ServerSettings.database;
	}

	/**
	 * <p>
	 * Returns the password {@link java.lang.String}.
	 * </p>
	 * 
	 * @return the password {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final String getPassword() {
		return ServerSettings.password;
	}

	/**
	 * <p>
	 * Returns the port {@link java.lang.String}.
	 * </p>
	 * 
	 * @return the port {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final String getPort() {
		return ServerSettings.port;
	}

	/**
	 * <p>
	 * Returns the server name {@link java.lang.String}.
	 * </p>
	 * 
	 * @return the server name {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final String getServer() {
		return ServerSettings.server;
	}

	/**
	 * <p>
	 * Returns the username {@link java.lang.String}.
	 * </p>
	 * 
	 * @return the username {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final String getUsername() {
		return ServerSettings.username;
	}

	/**
	 * <p>
	 * Allows the database name {@link java.lang.String} to be altered.
	 * </p>
	 * 
	 * @param database
	 *            the new database name {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final void setDatabase(final String database) {
		ServerSettings.database = database;
	}

	/**
	 * <p>
	 * Allows the password {@link java.lang.String} to be altered.
	 * </p>
	 * 
	 * @param password
	 *            the new password {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final void setPassword(final String password) {
		ServerSettings.password = password;
	}

	/**
	 * <p>
	 * Allows the port {@link java.lang.String} to be altered.
	 * </p>
	 * 
	 * @param port
	 *            the new port {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final void setPort(final String port) {
		ServerSettings.port = port;
	}

	/**
	 * <p>
	 * Allows the server name {@link java.lang.String} to be altered.
	 * </p>
	 * 
	 * @param server
	 *            the new server name {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final void setServer(final String server) {
		ServerSettings.server = server;
	}

	/**
	 * <p>
	 * Allows the username {@link java.lang.String} to be altered.
	 * </p>
	 * 
	 * @param username
	 *            the new username {@link java.lang.String}.
	 * @since 1.1
	 */
	public static final void setUsername(final String username) {
		ServerSettings.username = username;
	}
}
