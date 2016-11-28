
package dml.team5;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sql.rowset.CachedRowSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.sun.rowset.CachedRowSetImpl;
import dml.team5.antlr.ModifiedPLSQLLexer;
import dml.team5.antlr.ModifiedPLSQLParser;
import dml.team5.antlr.PLSQLLexer;
import dml.team5.antlr.PLSQLParser;

/**
 * <p>
 * This helper class contains useful utility methods.
 * </p>
 * 
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 */
public final class Utility
{
    public static final HashMap<String, List<String>> SCHEMA            = new HashMap<String, List<String>>();
    public static final List<SelectedElement>         SELECTED_ELEMENTS = new ArrayList<SelectedElement>();
    public static final List<String>                  TABLES            = new ArrayList<String>();

    /**
     * <p>
     * Builds a mapping of the database's schema, matching column names to table names.
     * </p>
     * 
     * @param connection
     *            the {@link java.sql.Connection} to the database server.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final void buildSchemaMap(final Connection connection) throws SQLException
    {
        Utility.SCHEMA.clear();
        Utility.buildTableList(connection);

        for ( String table : Utility.TABLES )
        {
            final List<String> columns = new ArrayList<String>();
            final CachedRowSet results = Utility.executeSQLStatement(connection, "Describe " + table);

            while ( results.next() )
            {
                columns.add(results.getString(1));
            }

            Utility.SCHEMA.put(table, columns);
            results.close();
        }
    }

    /**
     * <p>
     * Build a list of all the relevant table names in the database if the current list is empty.
     * </p>
     * 
     * @param connection
     *            the {@link java.sql.Connection} to the database server.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final void buildTableList(final Connection connection) throws SQLException
    {
        if ( Utility.TABLES.isEmpty() )
        {
            final String query = "Select TABLE_NAME From INFORMATION_SCHEMA.TABLES Where TABLE_SCHEMA = '?' Order By TABLE_NAME Asc";
            final PreparedStatement pstatement = connection.prepareStatement(query);

            pstatement.setString(1, ServerSettings.getDatabase());

            final CachedRowSet results = Utility.executeSQLStatement(pstatement);

            while ( results.next() )
            {
                Utility.TABLES.add(results.getString(1));
            }

            results.close();
            pstatement.close();
        }
    }

    /**
     * <p>
     * Creates an XML {@link org.w3c.dom.Document} using the DOM API from the resulting {@link javax.sql.rowset.CachedRowSet} of a SQL query.
     * </p>
     * 
     * @param results
     *            the resulting {@link javax.sql.rowset.CachedRowSet} of a SQL query.
     * @return the {@link org.w3c.dom.Document}.
     * @throws ParserConfigurationException
     *             if a {@link javax.xml.parsers.DocumentBuilder} cannot be created which satisfies the configuration requested.
     * @throws SQLException
     *             if a database access error occurs.
     * @throws SAXException
     *             if a parsing error occurs.
     * @throws IOException
     *             if an input/output error occurs.
     * @since 1.1
     */
    public static final Document createDocument(final CachedRowSet results) throws ParserConfigurationException, SQLException, SAXException, IOException
    {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource inputStream = new InputSource(new StringReader(Utility.writeXMLResults(results)));
        return documentBuilder.parse(inputStream);
    }

    /**
     * <p>
     * Executes a SQL statement on the database server and returns the corresponding {@link javax.sql.rowset.CachedRowSet}.
     * </p>
     * 
     * @param connection
     *            the {@link java.sql.Connection} to the database server.
     * @param inputString
     *            the input {@link java.lang.String} containing the SQL query.
     * @return the corresponding {@link javax.sql.rowset.CachedRowSet}.
     * @throws SQLException
     *             if a database access error occurs, or the given SQL statement produces anything other than a single {@link javax.sql.rowset.CachedRowSet} object.
     * @since 1.0
     */
    public static final CachedRowSet executeSQLStatement(final Connection connection, final String inputString) throws SQLException
    {
        // Create the output buffer.
        CachedRowSet results = new CachedRowSetImpl();

        // Create the statement.
        Statement statement = connection.createStatement();

        // Store the results of the executed query in the output buffer.
        results.populate(statement.executeQuery(inputString));

        // Close the statement.
        statement.close();

        // Return the output buffer.
        return results;
    }

    /**
     * <p>
     * Executes a SQL statement on the database server and returns the corresponding {@link javax.sql.rowset.CachedRowSet}.
     * </p>
     * 
     * @param pstatement
     *            the {@link java.sql.PreparedStatement} to execute.
     * @return the corresponding {@link javax.sql.rowset.CachedRowSet}.
     * @throws SQLException
     *             if a database access error occurs, or the given SQL statement produces anything other than a single {@link javax.sql.rowset.CachedRowSet} object.
     * @since 1.0
     */
    public static final CachedRowSet executeSQLStatement(final PreparedStatement pstatement) throws SQLException
    {
        // Create the output buffer.
        CachedRowSet results = new CachedRowSetImpl();

        // Store the results of the executed query in the output buffer.
        results.populate(pstatement.executeQuery());

        // Return the output buffer.
        return results;
    }

    /**
     * <p>
     * Establishes a {@link java.sql.Connection} to the database server.
     * </p>
     * 
     * @param isOracle
     *            true to connect to an Oracle database, false to connect to a MySQL database.
     * @return the {@link java.sql.Connection} to the database server.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.0
     */
    public static final Connection getConnection(final boolean isOracle) throws SQLException
    {
        // Return the connection.
        if ( isOracle )
        {
            // Oracle
            return DriverManager.getConnection("jdbc:oracle:thin:@" + ServerSettings.getServer() + ":" + ServerSettings.getPort() + ":" + ServerSettings.getDatabase(), ServerSettings.getUsername(), ServerSettings.getPassword());
        }
        else
        {
            // MySQL
            return DriverManager.getConnection("jdbc:mysql://" + ServerSettings.getServer() + "/" + ServerSettings.getDatabase() + "?zeroDateTimeBehavior=convertToNull&autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8" + "&user=" + ServerSettings.getUsername() + "&password=" + ServerSettings.getPassword());
        }
    }

    /**
     * <p>
     * Lexically analyzes the input {@link java.lang.String} and returns the appropriate {@link org.antlr.v4.runtime.Parser}.
     * </p>
     * 
     * @param inputString
     *            the input {@link java.lang.String}.
     * @param isModified
     *            true to use the {@link dml.team5.antlr.ModifiedPLSQLParser}, false to use the {@link dml.team5.antlr.PLSQLParser}.
     * @return the {@link org.antlr.v4.runtime.Parser}.
     * @since 1.1
     */
    public static final Parser getParser(final String inputString, final boolean isModified)
    {
        // Declare the lexer variable.
        Lexer lexer = null;

        // Invoke the lexer with the input string.
        if ( isModified )
        {
            lexer = new ModifiedPLSQLLexer(new ANTLRInputStream(inputString));
        }
        else
        {
            lexer = new PLSQLLexer(new ANTLRInputStream(inputString));
        }

        // Return an instance of the parser.
        if ( isModified )
        {
            return new ModifiedPLSQLParser(new CommonTokenStream(lexer));
        }
        else
        {
            return new PLSQLParser(new CommonTokenStream(lexer));
        }
    }

    /**
     * <p>
     * Takes a {@link org.antlr.v4.runtime.Parser} and returns a {@link org.antlr.v4.runtime.tree.ParseTree}.
     * </p>
     * 
     * @param parser
     *            the {@link org.antlr.v4.runtime.Parser}.
     * @param isSilent
     *            true to receive no console output, false otherwise.
     * @return the {@link org.antlr.v4.runtime.tree.ParseTree}.
     * @throws RecognitionException
     *             if a parsing error occurs.
     * @since 1.1
     */
    public static final ParseTree getParseTree(final Parser parser, final boolean isSilent) throws RecognitionException
    {
        // If silent, do not echo parse errors to the console.
        if ( isSilent )
        {
            parser.removeErrorListeners();
        }

        // Tell ANTLR we will handle any encountered errors with a fail-fast strategy.
        parser.setErrorHandler(new BailErrorStrategy());

        try
        {
            // Parse the input query and return the ParseTree.
            if ( parser instanceof PLSQLParser )
            {
                return ( (PLSQLParser) parser ).sql_script();
            }
            else if ( parser instanceof ModifiedPLSQLParser ) { return ( (ModifiedPLSQLParser) parser ).sql_script(); }
        }
        catch ( final ParseCancellationException pce )
        {
            // If not silent, throw RecognitionException back to the caller.
            if ( !isSilent )
            {
                if ( pce.getCause() instanceof RecognitionException ) { throw (RecognitionException) pce.getCause(); }
            }
        }

        // Return null ParseTree.
        return null;
    }

    /**
     * <p>
     * Takes an input {@link java.lang.String} and returns a {@link org.antlr.v4.runtime.tree.ParseTree}.
     * </p>
     * 
     * @param inputString
     *            the input {@link java.lang.String}.
     * @param isModified
     *            true to use the {@link dml.team5.antlr.ModifiedPLSQLParser}, false to use the {@link dml.team5.antlr.PLSQLParser}.
     * @param isSilent
     *            true to receive no console output, false otherwise.
     * @return the {@link org.antlr.v4.runtime.tree.ParseTree}.
     * @throws RecognitionException
     *             if a parsing error occurs.
     * @since 1.1
     */
    public static final ParseTree getParseTree(final String inputString, final boolean isModified, final boolean isSilent) throws RecognitionException
    {
        // Return the ParseTree.
        return Utility.getParseTree(Utility.getParser(inputString, isModified), isSilent);
    }

    /**
     * <p>
     * Returns the {@link dml.team5.SelectedElement} for the given {@link javax.sql.rowset.CachedRowSet} and columnIndex.
     * </p>
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param columnIndex
     *            the index of the column in the {@link javax.sql.rowset.CachedRowSet}.
     * @return the {@link dml.team5.SelectedElement} for the given {@link javax.sql.rowset.CachedRowSet} and columnIndex.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    private static final SelectedElement getSelectedElement(final CachedRowSet results, final int columnIndex) throws SQLException
    {
        String tableName = "";
        String columnName = "";
        String columnLabel = "";

        if ( !Utility.SELECTED_ELEMENTS.isEmpty() )
        {
            SelectedElement sel_elem = Utility.SELECTED_ELEMENTS.get(columnIndex - 1);

            tableName = sel_elem.tableName;
            columnName = sel_elem.columnName;
            columnLabel = sel_elem.columnLabel;
        }

        if ( tableName.isEmpty() )
        {
            tableName = results.getMetaData().getTableName(columnIndex).trim();

            if ( tableName.isEmpty() )
            {
                tableName = Utility.getTableName(results, columnIndex);
            }
        }

        if ( columnName.isEmpty() )
        {
            columnName = results.getMetaData().getColumnName(columnIndex).trim();
        }

        if ( columnLabel.isEmpty() )
        {
            columnLabel = results.getMetaData().getColumnLabel(columnIndex).trim();
        }

        return new SelectedElement(tableName, columnName, columnLabel);
    }

    /**
     * <p>
     * Returns the table name {@link java.lang.String} associated with a particular {@link javax.sql.rowset.CachedRowSet} and columnIndex.
     * </p>
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param columnIndex
     *            the index of the column in the {@link javax.sql.rowset.CachedRowSet}.
     * @return the table name {@link java.lang.String} associated with a particular {@link javax.sql.rowset.CachedRowSet} and columnIndex.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String getTableName(final CachedRowSet results, final int columnIndex) throws SQLException
    {
        // TODO: Improve this algorithm.
        // Fails on query: "Select * From C, S"
        String tableName = "";

        for ( String table : Utility.TABLES )
        {
            if ( tableName.isEmpty() )
            {
                List<String> columns = Utility.SCHEMA.get(table);
                ResultSetMetaData rsmd = results.getMetaData();

                for ( String column : columns )
                {
                    if ( column.equalsIgnoreCase(rsmd.getColumnName(columnIndex)) )
                    {
                        tableName = table;
                        break;
                    }
                }
            }
            else
            {
                break;
            }
        }

        return tableName;
    }

    /**
     * <p>
     * Takes a string and pads it with whitespace to the left.
     * </p>
     * 
     * @param s
     *            the {@link java.lang.String} to pad.
     * @param n
     *            the minimum string length.
     * @return the whitespace padded {@link java.lang.String}.
     * @since 1.1
     */
    public static String padLeft(final String s, final int n)
    {
        // Return a whitespace padded string.
        return String.format("%1$" + n + "s", s);
    }

    /**
     * <p>
     * Takes a string and pads it with whitespace to the right.
     * </p>
     * 
     * @param s
     *            the {@link java.lang.String} to pad.
     * @param n
     *            the minimum string length.
     * @return the whitespace padded {@link java.lang.String}.
     * @since 1.1
     */
    public static String padRight(final String s, final int n)
    {
        // Return a whitespace padded string.
        return String.format("%1$-" + n + "s", s);
    }

    /**
     * <p>
     * Constructs a table {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * </p>
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @return the table {@link java.lang.String}.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String writeSQLResults(final CachedRowSet results) throws SQLException
    {
        // Return SQL results.
        return Utility.writeSQLResults(results, 15);
    }

    /**
     * <p>
     * Constructs a table {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * </p>
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param minimumFieldWidth
     *            the minimum width of a field in characters.
     * @return the table {@link java.lang.String}.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String writeSQLResults(final CachedRowSet results, final int minimumFieldWidth) throws SQLException
    {
        // Return SQL results.
        return Utility.writeSQLResults(results, minimumFieldWidth, '=');
    }

    /**
     * <p>
     * Constructs a table {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * </p>
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param minimumFieldWidth
     *            the minimum width of a field in characters.
     * @param separatorCharacter
     *            the character used to construct the separator between the table header and the table body.
     * @return the table {@link java.lang.String}.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String writeSQLResults(final CachedRowSet results, final int minimumFieldWidth, final char separatorCharacter) throws SQLException
    {
        // Declare constants.
        final ResultSetMetaData rsmd = results.getMetaData();
        final StringBuffer output = new StringBuffer();

        /*
         * Write the table header.
         */

        // Loop through columns.
        for ( int i = 1; i <= rsmd.getColumnCount(); i++ )
        {
            // Write field label.
            output.append(Utility.padRight(rsmd.getColumnLabel(i), Math.max(minimumFieldWidth, rsmd.getColumnDisplaySize(i))));
        }

        /*
         * Write the table separator.
         */

        // Write new line.
        output.append("\n");

        // Write separator characters.
        for ( int i = 0; i < ( ( rsmd.getColumnCount() * 2 ) * minimumFieldWidth ); i++ )
        {
            output.append(separatorCharacter);
        }

        // Write new line.
        output.append("\n");

        /*
         * Write the table data.
         */

        // Loop through rows.
        while ( results.next() )
        {
            // Loop through columns.
            for ( int i = 1; i <= rsmd.getColumnCount(); i++ )
            {
                // Write field data.
                if ( results.getObject(i) != null )
                {
                    output.append(Utility.padRight(results.getObject(i).toString().trim(), Math.max(minimumFieldWidth, rsmd.getColumnDisplaySize(i))));
                }
                else
                {
                    output.append(Utility.padRight("", Math.max(minimumFieldWidth, rsmd.getColumnDisplaySize(i))));
                }
            }

            // Write new line.
            output.append("\n");
        }

        // Reset the position of result's row pointer.
        results.beforeFirst();

        // Return SQL results.
        return output.toString();
    }

    /**
     * <p>
     * Constructs an XML {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * </p>
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @return the XML {@link java.lang.String}.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String writeXMLResults(final CachedRowSet results) throws SQLException
    {
        // TODO: DTD versus XSD
        // TODO: Grouping
        // TODO: Compression

        // Declare data structures.
        final ResultSetMetaData rsmd = results.getMetaData();
        final StringBuffer output = new StringBuffer();

        // Declare default strings.
        String versionString = "1.0", rootTag = "ThisQuery", rowTag = "A_Record";

        // Write header.
        output.append("<?xml version=\"" + versionString + "\"?>\n");

        // Write opening root tag.
        output.append("<" + rootTag + ">\n");

        // Loop through rows.
        while ( results.next() )
        {
            // Write opening record tag.
            output.append("\t<" + rowTag + ">\n");

            // Loop through columns.
            for ( int i = 1; i <= rsmd.getColumnCount(); i++ )
            {
                // Declare default strings.
                SelectedElement xmlStrings = Utility.getSelectedElement(results, i);
                final Object VALUE = results.getObject(i);

                // Write opening attribute tag.
                output.append("\t\t<" + xmlStrings.columnLabel + " table=\"" + xmlStrings.tableName + "\" name=\"" + xmlStrings.columnName + "\">");

                if ( VALUE != null )
                {
                    // Write attribute value.
                    output.append(VALUE.toString().trim());
                }

                // Write closing attribute tag.
                output.append("</" + xmlStrings.columnLabel + ">\n");
            }

            // Write closing record tag.
            output.append("\t</" + rowTag + ">\n");
        }

        // Write closing root tag.
        output.append("</" + rootTag + ">\n");

        // Reset the position of result's row pointer.
        results.beforeFirst();

        // Return XML results.
        return output.toString();
    }
}
