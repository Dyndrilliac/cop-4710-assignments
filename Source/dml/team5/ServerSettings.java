
package dml.team5;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This helper class contains the configuration settings for the SQL relational database server.
 */
public final class ServerSettings
{
    private static String database = "dworcl";
    private static String password = "ravenpuff";
    private static String port     = "1521";
    private static String server   = "olympia.unfcsd.unf.edu";
    private static String username = "hogwarts";

    /**
     * Returns the database name {@link java.lang.String}.
     * 
     * @return the database name {@link java.lang.String}.
     * @since 1.1
     */
    public static final String getDatabase()
    {
        return ServerSettings.database;
    }

    /**
     * Returns the password {@link java.lang.String}.
     * 
     * @return the password {@link java.lang.String}.
     * @since 1.1
     */
    public static final String getPassword()
    {
        return ServerSettings.password;
    }

    /**
     * Returns the port {@link java.lang.String}.
     * 
     * @return the port {@link java.lang.String}.
     * @since 1.1
     */
    public static final String getPort()
    {
        return ServerSettings.port;
    }

    /**
     * Returns the server name {@link java.lang.String}.
     * 
     * @return the server name {@link java.lang.String}.
     * @since 1.1
     */
    public static final String getServer()
    {
        return ServerSettings.server;
    }

    /**
     * Returns the username {@link java.lang.String}.
     * 
     * @return the username {@link java.lang.String}.
     * @since 1.1
     */
    public static final String getUsername()
    {
        return ServerSettings.username;
    }

    /**
     * Allows the database name {@link java.lang.String} to be altered.
     * 
     * @param database
     *            the new database name {@link java.lang.String}.
     * @since 1.1
     */
    public static final void setDatabase(final String database)
    {
        ServerSettings.database = database;
    }

    /**
     * Allows the password {@link java.lang.String} to be altered.
     * 
     * @param password
     *            the new password {@link java.lang.String}.
     * @since 1.1
     */
    public static final void setPassword(final String password)
    {
        ServerSettings.password = password;
    }

    /**
     * Allows the port {@link java.lang.String} to be altered.
     * 
     * @param port
     *            the new port {@link java.lang.String}.
     * @since 1.1
     */
    public static final void setPort(final String port)
    {
        ServerSettings.port = port;
    }

    /**
     * Allows the server name {@link java.lang.String} to be altered.
     * 
     * @param server
     *            the new server name {@link java.lang.String}.
     * @since 1.1
     */
    public static final void setServer(final String server)
    {
        ServerSettings.server = server;
    }

    /**
     * Allows the username {@link java.lang.String} to be altered.
     * 
     * @param username
     *            the new username {@link java.lang.String}.
     * @since 1.1
     */
    public static final void setUsername(final String username)
    {
        ServerSettings.username = username;
    }
}
