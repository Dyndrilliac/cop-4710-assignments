
package dml.team5;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import dml.team5.antlr.ModifiedPLSQLLexer;
import dml.team5.antlr.ModifiedPLSQLParser;
import dml.team5.antlr.PLSQLLexer;
import dml.team5.antlr.PLSQLParser;
import oracle.jdbc.rowset.OracleCachedRowSet;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This helper class contains useful utility methods.
 */
public final class Utility
{
    /**
     * Creates an XML {@link org.w3c.dom.Document} using the DOM API from the resulting {@link javax.sql.rowset.CachedRowSet} of a SQL query.
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
    public static Document createDocument(CachedRowSet results) throws ParserConfigurationException, SQLException, SAXException, IOException
    {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource inputStream = new InputSource(new StringReader(Utility.writeXMLResults(results)));
        return documentBuilder.parse(inputStream);
    }

    /**
     * Executes a SQL statement on the Oracle database server and returns the corresponding {@link javax.sql.rowset.CachedRowSet}.
     * 
     * @param connection
     *            the {@link java.sql.Connection}.
     * @param inputString
     *            the input {@link java.lang.String} containing the SQL query.
     * @return the {@link javax.sql.rowset.CachedRowSet}.
     * @throws SQLException
     *             if a database access error occurs, or the given SQL statement produces anything other than a single {@link javax.sql.rowset.CachedRowSet} object.
     * @since 1.0
     */
    public static final CachedRowSet executeSQLStatement(final Connection connection, final String inputString) throws SQLException
    {
        // Create the output buffer.
        CachedRowSet results = new OracleCachedRowSet();

        // Store the results of the executed query in the output buffer.
        results.populate(connection.createStatement().executeQuery(inputString));

        // Return the output buffer.
        return results;
    }

    /**
     * Establishes a {@link java.sql.Connection} to the Oracle database server.
     * 
     * @return the {@link java.sql.Connection}.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.0
     */
    public static final Connection getConnection() throws SQLException
    {
        // Return the connection.
        return DriverManager.getConnection("jdbc:oracle:thin:@" + OracleServerSettings.SERVER + ":" + OracleServerSettings.PORT + ":" + OracleServerSettings.DATABASE, OracleServerSettings.USERNAME, OracleServerSettings.PASSWORD);
    }

    /**
     * Lexically analyzes the input {@link java.lang.String} and returns the appropriate {@link org.antlr.v4.runtime.Parser}.
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
        // Declare the variables.
        ANTLRInputStream inputStream = null;
        Lexer lexer = null;
        CommonTokenStream tokens = null;

        // Pass the input string to ANTLR.
        inputStream = new ANTLRInputStream(inputString);

        // Invoke the lexer with the input string.
        if ( isModified )
        {
            lexer = new ModifiedPLSQLLexer(inputStream);
        }
        else
        {
            lexer = new PLSQLLexer(inputStream);
        }

        // Populate the token stream with the lexer's output.
        tokens = new CommonTokenStream(lexer);

        // Return an instance of the parser.
        if ( isModified )
        {
            return new ModifiedPLSQLParser(tokens);
        }
        else
        {
            return new PLSQLParser(tokens);
        }
    }

    /**
     * Takes a {@link org.antlr.v4.runtime.Parser} and returns a {@link org.antlr.v4.runtime.tree.ParseTree}.
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
     * Takes an input {@link java.lang.String} and returns a {@link org.antlr.v4.runtime.tree.ParseTree}.
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
     * Takes a string and pads it with whitespace to the left.
     * 
     * @param s
     *            the {@link java.lang.String} to pad.
     * @param n
     *            minimum string length.
     * @return the whitespace padded {@link java.lang.String}.
     * @since 1.1
     */
    public static String padLeft(final String s, final int n)
    {
        // Return a whitespace padded string.
        return String.format("%1$" + n + "s", s);
    }

    /**
     * Takes a string and pads it with whitespace to the right.
     * 
     * @param s
     *            the {@link java.lang.String} to pad.
     * @param n
     *            minimum string length.
     * @return the whitespace padded {@link java.lang.String}.
     * @since 1.1
     */
    public static String padRight(final String s, final int n)
    {
        // Return a whitespace padded string.
        return String.format("%1$-" + n + "s", s);
    }

    /**
     * Creates constructs a table {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
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
        return Utility.writeSQLResults(results, 10);
    }

    /**
     * Creates constructs a table {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param minimumFieldWidth
     *            the minimum width of the field in characters.
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
     * Constructs a table {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param minimumFieldWidth
     *            the minimum width of the field in characters.
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
        for ( int i = 0; i < ( rsmd.getColumnCount() * minimumFieldWidth ); i++ )
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
                output.append(Utility.padRight(results.getString(i), Math.max(minimumFieldWidth, rsmd.getColumnDisplaySize(i))));
            }

            // Write new line.
            output.append("\n");
        }

        // Return SQL results.
        return output.toString();
    }

    /**
     * Constructs an XML {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @return the XML {@link java.lang.String}
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String writeXMLResults(final CachedRowSet results) throws SQLException
    {
        // Declare data structures.
        final ResultSetMetaData rsmd = results.getMetaData();
        final StringBuffer output = new StringBuffer();

        // Declare default strings.
        String version = "1.0", root = "ThisQuery", record = "A_Record";

        // Write header.
        output.append("<?xml version=\"" + version + "\"?>\n");

        // Write opening root tag.
        output.append("<" + root + ">\n");

        // Loop through rows.
        while ( results.next() )
        {
            // Write opening record tag.
            output.append("\t<" + record + ">\n");

            // Loop through columns.
            for ( int i = 1; i <= rsmd.getColumnCount(); i++ )
            {
                // Declare constants.
                final String COL_LABEL = rsmd.getColumnLabel(i);
                final String TBL_NAME = rsmd.getTableName(i);
                final String COL_NAME = rsmd.getColumnName(i);
                final Object VALUE = results.getObject(i);

                // Write opening attribute tag.
                output.append("\t\t<" + COL_LABEL + " table=\"" + TBL_NAME + "\" name=\"" + COL_NAME + "\">");

                if ( VALUE != null )
                {
                    // Write attribute value.
                    output.append(VALUE.toString().trim());
                }

                // Write closing attribute tag.
                output.append("</" + COL_LABEL + ">\n");
            }

            // Write closing record tag.
            output.append("\t</" + record + ">\n");
        }

        // Write closing root tag.
        output.append("</" + root + ">\n");

        // Return XML results.
        return output.toString();
    }
}
