
package dml.team5;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This helper class contains useful utility methods.
 */
public final class Utility
{
    public static final HashMap<String, List<String>> SCHEMA                   = new HashMap<String, List<String>>();
    public static final String                        SELECTED_ELEMENT_PATTERN = "((?<=((S|s)(E|e)(L|l)(E|e)(C|c)(T|t))\\s+)(.+)(?=(\\s+(F|f)(R|r)(O|o)(M|m))))";
    public static final List<String>                  TABLES                   = new ArrayList<String>();

    public static final void buildSchemaMap(final Connection connection) throws SQLException
    {
        Utility.SCHEMA.clear();
        Utility.buildTableList(connection);

        for ( String table : Utility.TABLES )
        {
            List<String> columns = new ArrayList<String>();
            CachedRowSet results = Utility.executeSQLStatement(connection, "Describe " + table);

            while ( results.next() )
            {
                columns.add(results.getString(1));
            }

            Utility.SCHEMA.put(table, columns);
        }
    }

    public static final void buildTableList(final Connection connection) throws SQLException
    {
        if ( Utility.TABLES.isEmpty() )
        {
            CachedRowSet results = Utility.executeSQLStatement(connection, "Select TABLE_NAME From INFORMATION_SCHEMA.TABLES Where TABLE_SCHEMA = '" + OracleServerSettings.DATABASE() + "' Order By TABLE_NAME Asc");
            while ( results.next() )
            {
                Utility.TABLES.add(results.getString(1));
            }
        }
    }

    /**
     * Creates an XML {@link org.w3c.dom.Document} using the DOM API from the resulting {@link javax.sql.rowset.CachedRowSet} of a SQL query.
     * 
     * @param results
     *            the resulting {@link javax.sql.rowset.CachedRowSet} of a SQL query.
     * @param parent
     *            the parent instance of a {@link dml.team5.PLSQL2XMLConverter} object.
     * @param isDTD
     *            true to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
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
    public static final Document createDocument(CachedRowSet results, final PLSQL2XMLConverter parent, final boolean isDTD) throws ParserConfigurationException, SQLException, SAXException, IOException
    {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource inputStream = new InputSource(new StringReader(Utility.writeXMLResults(results, parent, isDTD)));
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
     * Extracts the first substring that matches the Regular Expression pattern from a given {@link java.lang.String}.
     * 
     * @param s
     *            the {@link java.lang.String} from which to extract the substring.
     * @param p
     *            the Regular Expression pattern {@link java.lang.String}.
     * @return the extracted {@link java.lang.String}.
     * @since 1.1
     */
    public static final String extractFirstSubStringByPattern(final String s, final String p)
    {
        List<String> results = Utility.extractSubStringsByPattern(s, p);
        String result = "";

        if ( !results.isEmpty() )
        {
            result = results.get(0);
        }

        return result;
    }

    /**
     * Extracts the last substring that matches the Regular Expression pattern from a given {@link java.lang.String}.
     * 
     * @param s
     *            the {@link java.lang.String} from which to extract the substring.
     * @param p
     *            the Regular Expression pattern {@link java.lang.String}.
     * @return the extracted {@link java.lang.String}.
     * @since 1.1
     */
    public static final String extractLastSubStringByPattern(final String s, final String p)
    {
        List<String> results = Utility.extractSubStringsByPattern(s, p);
        String result = "";

        if ( !results.isEmpty() )
        {
            result = results.get(results.size() - 1);
        }

        return result;
    }

    /**
     * Extracts every substring that matches the Regular Expression pattern from a given {@link java.lang.String}.
     * 
     * @param s
     *            the {@link java.lang.String} from which to extract substrings.
     * @param p
     *            the Regular Expression pattern {@link java.lang.String}.
     * @return the {@link java.util.List} of extracted {@link java.lang.String}s.
     * @since 1.1
     */
    public static final List<String> extractSubStringsByPattern(final String s, final String p)
    {
        List<String> results = new ArrayList<String>();
        Pattern pattern = null;
        Matcher matcher = null;

        try
        {
            pattern = Pattern.compile(p);
        }
        catch ( final PatternSyntaxException pse )
        {
            pse.printStackTrace();
            return results;
        }

        matcher = pattern.matcher(s);

        while ( matcher.find() )
        {
            results.add(matcher.group().trim());
        }

        return results;
    }

    /**
     * Establishes a {@link java.sql.Connection} to the Oracle database server.
     * 
     * @param isOracle
     *            .
     * @return the {@link java.sql.Connection}.
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
            return DriverManager.getConnection("jdbc:oracle:thin:@" + OracleServerSettings.SERVER() + ":" + OracleServerSettings.PORT() + ":" + OracleServerSettings.DATABASE(), OracleServerSettings.USERNAME(), OracleServerSettings.PASSWORD());
        }
        else
        {
            // MySQL
            return DriverManager.getConnection("jdbc:mysql://" + OracleServerSettings.SERVER() + "/" + OracleServerSettings.DATABASE() + "?zeroDateTimeBehavior=convertToNull&autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8" + "&user=" + OracleServerSettings.USERNAME() + "&password=" + OracleServerSettings.PASSWORD());
        }
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

    private static final String[] getXMLStrings(final CachedRowSet results, int columnIndex, PLSQL2XMLConverter parent) throws SQLException
    {
        String tableName = results.getMetaData().getTableName(columnIndex).trim();
        String columnName = results.getMetaData().getColumnName(columnIndex).trim();
        String columnLabel = results.getMetaData().getColumnLabel(columnIndex).trim();

        final String selected_elements_string;
        final String[] selected_elements;

        if ( parent.isModified() )
        {
            selected_elements_string = Utility.extractFirstSubStringByPattern(parent.getStrippedInput(), Utility.SELECTED_ELEMENT_PATTERN);
        }
        else
        {
            selected_elements_string = Utility.extractFirstSubStringByPattern(parent.getOriginalInput(), Utility.SELECTED_ELEMENT_PATTERN);
        }

        if ( selected_elements_string.contentEquals("*") )
        {
            // Handle "SELECT * FROM <TABLE_LIST>" queries.
            // Nothing you can do except check every table sequentially to see if it contains the columnName for the given columnIndex.
            // No AS clause is possible, so no need to worry about discrepancies between columnName and columnLabel.
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
        }
        else
        {
            // Handle "SELECT [<TABLE>.]<E1>[, [<TABLE>.]<E2>[, ...]] FROM <TABLE_LIST>" queries.
            selected_elements = ( selected_elements_string ).split(",");

            if ( ( columnIndex > 0 ) && ( columnIndex <= selected_elements.length ) )
            {
                final String selected_element = selected_elements[columnIndex - 1];

                // Two possibilities exist: either the selected element is fully qualified and includes its table name, or it isn't and it doesn't.
                if ( selected_element.contains(".") )
                {
                    // If it's fully qualified, just extract the table name.
                    if ( tableName.isEmpty() )
                    {
                        tableName = selected_element.substring(0, selected_element.indexOf("."));
                    }

                    // Handle AS clause.
                    if ( selected_element.toLowerCase(Locale.ROOT).contains(" as ") )
                    {
                        columnName = selected_element.substring(selected_element.indexOf(".") + 1, selected_element.indexOf(" "));
                        columnLabel = selected_element.substring(selected_element.lastIndexOf(" ") + 1);
                    }
                    else
                    {
                        columnName = selected_element.substring(selected_element.indexOf(".") + 1);
                        columnLabel = columnName;
                    }
                }
                else
                {
                    // If it's not fully qualified, then we have to do some additional shenanigans.
                    if ( tableName.isEmpty() )
                    {
                        // TODO: Figure out tableName.
                        tableName = "?";
                    }

                    // Handle AS clause.
                    if ( selected_element.toLowerCase(Locale.ROOT).contains(" as ") )
                    {
                        columnName = selected_element.substring(0, selected_element.indexOf(" "));
                        columnLabel = selected_element.substring(selected_element.lastIndexOf(" ") + 1);
                    }
                    else
                    {
                        columnName = selected_element;
                        columnLabel = columnName;
                    }
                }
            }
        }

        String[] xmlStrings = { tableName, columnName, columnLabel.toUpperCase(Locale.ROOT) };
        return xmlStrings;
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
        return Utility.writeSQLResults(results, 15);
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
     * Constructs an XML {@link java.lang.String} from the results of a SQL query in the form of a {@link javax.sql.rowset.CachedRowSet} object.
     * 
     * @param results
     *            the {@link javax.sql.rowset.CachedRowSet} resulting from executing a SQL query.
     * @param parent
     *            the parent instance of a {@link dml.team5.PLSQL2XMLConverter} object.
     * @param isDTD
     *            true to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
     * @return the XML {@link java.lang.String}.
     * @throws SQLException
     *             if a database access error occurs.
     * @since 1.1
     */
    public static final String writeXMLResults(final CachedRowSet results, final PLSQL2XMLConverter parent, final boolean isDTD) throws SQLException
    {
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
                String[] xmlStrings = Utility.getXMLStrings(results, i, parent);
                final Object VALUE = results.getObject(i);

                // Write opening attribute tag.
                output.append("\t\t<" + xmlStrings[2] + " table=\"" + xmlStrings[0] + "\" name=\"" + xmlStrings[1] + "\">");

                if ( VALUE != null )
                {
                    // Write attribute value.
                    output.append(VALUE.toString().trim());
                }

                // Write closing attribute tag.
                output.append("</" + xmlStrings[2] + ">\n");
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
