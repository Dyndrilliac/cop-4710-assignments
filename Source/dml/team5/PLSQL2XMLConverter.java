
package dml.team5;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import javax.sql.rowset.CachedRowSet;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * <p>
 * This class converts the relational database output of both unmodified and modified Oracle PL/SQL selection statements into XML.
 * </p>
 * <p>
 * Command-line usage: <code>java {@link dml.team5.PLSQL2XMLConverter} InputString1 InputString2 ... InputStringN</code>
 * </p>
 * <p>
 * Remember to enclose strings containing whitespace with double-quotes!
 * </p>
 * 
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 */
public final class PLSQL2XMLConverter extends LanguageConverter
{
    /**
     * <p>
     * Simple console-based command-line test driver. Each argument is an input {@link java.lang.String}.
     * </p>
     * 
     * @param args
     *            the array of command-line arguments.
     * @since 1.0
     */
    public static final void main(final String[] args)
    {
        final boolean isDebugMode = false;
        final boolean isDTD = true;
        final boolean isOracle = true;

        if ( args.length > 0 )
        {
            for ( int i = 0; i < args.length; i++ )
            {
                // Separate multiple pieces of output with blank lines.
                if ( i > 0 )
                {
                    System.out.println();
                }

                // Echo input.
                System.out.println("Input: " + args[i] + "\n");

                // Print output.
                System.out.println(new PLSQL2XMLConverter(args[i], isOracle, isDebugMode, isDTD));
            }
        }
        else
        {
            // If no input is given, show the user how to invoke the program.
            System.out.println("Usage: " + PLSQL2XMLConverter.class.getSimpleName() + " InputString1 InputString2 ... InputStringN");
            System.out.println("Remember to enclose strings containing whitespace with double-quotes!");
        }
    }

    /*
     * Declare private instance variables.
     */
    private boolean isDebugMode = false;
    private boolean isDTD       = true;
    private boolean isModified  = false;
    private boolean isOracle    = false;

    /**
     * <p>
     * Constructs a new instance of {@link dml.team5.PLSQL2XMLConverter} based on an input {@link java.lang.String}.
     * </p>
     * 
     * @param inputString
     *            the input {@link java.lang.String}.
     * @param isOracle
     *            true to connect to an Oracle database, false to connect to a MySQL database.
     * @param isDebugMode
     *            true to print the SQL table in addition to the XML tags, false otherwise.
     * @param isDTD
     *            true to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
     * @since 1.0
     */
    public PLSQL2XMLConverter(final String inputString, final boolean isOracle, final boolean isDebugMode, final boolean isDTD)
    {
        // Set the original input.
        super(inputString.trim());

        // Set whether Oracle or MySQL.
        this.setOracle(isOracle);

        // Set whether debugging or not.
        this.setDebugMode(isDebugMode);

        // Set whether DTD or XSD.
        this.setDTD(isDTD);

        try
        {
            // Execute conversion process.
            this.convert();
        }
        catch ( final Exception e )
        {
            // If there is an error, then set the output to a meaningful error message of some kind.
            this.setOutputString(e.toString() + "\n");
        }
    }

    /**
     * <p>
     * Executes the conversion process: parses input, strips out SQL modifications, connects to database, executes input, and converts the output into XML.
     * </p>
     * 
     * @throws Exception
     *             if a database access error occurs, or if a parsing error occurs.
     * @since 1.0
     */
    @Override
    protected final void convert() throws Exception
    {
        // Get the input.
        String input = this.getOriginalInput();

        // Declare variables to keep track of the parse tree, the connection, and the query results.
        ParseTree parseTree = null;
        Connection connection = null;
        CachedRowSet results = null;

        // Is the input a SELECT query?
        boolean isInputSelect = input.toLowerCase(Locale.ROOT).startsWith("select");

        try
        {
            // Try to parse the input query silently as unmodified PL/SQL.
            parseTree = Utility.getParseTree(input, this.isModified(), true);

            if ( parseTree == null )
            {
                if ( isInputSelect )
                {
                    // If the silent parse fails, set the isModified flag to true.
                    this.setModified(true);

                    // Try to parse the input query as modified PL/SQL.
                    parseTree = Utility.getParseTree(input, this.isModified(), true);

                    // If the input query is modified PL/SQL and a SELECT query, then strip out the modifications and make it unmodified PL/SQL again.
                    input = this.getStrippedInput();
                }
            }

            // Try to parse the input query as unmodified PL/SQL.
            parseTree = Utility.getParseTree(input, false, false);

            // Walk the parse tree to identify and store the table ID's.
            ParseTreeWalker.DEFAULT.walk(new TableListener(), parseTree);

            // Walk the parse tree to identify and store the selected elements.
            ParseTreeWalker.DEFAULT.walk(new SelectedElementListener(), parseTree);

            try
            {
                // Connect to the Oracle database server.
                connection = Utility.getConnection(this.isOracle());

                // Execute the input query.
                results = Utility.executeSQLStatement(connection, input);

                // Make a mapping of the database's schema, matching column names to table names.
                //Utility.buildSchemaMap(connection);

                // Try to close the connection.
                connection.close();

                if ( results != null )
                {
                    // Is the input query a select statement?
                    if ( isInputSelect )
                    {
                        // If debugging, print out the table too.
                        if ( this.isDebugMode() )
                        {
                            System.out.println(Utility.writeSQLResults(results));
                        }

                        // Construct the output XML.
                        this.setOutputString(Utility.writeXMLResults(results));
                    }

                    // Try to close the result set.
                    results.close();
                }
            }
            catch ( final SQLException sqle )
            {
                // Failed to connect and execute the input query.
                //throw new Exception(sqle.toString());
                sqle.printStackTrace();
            }
            finally
            {
                connection = null;
                results = null;
            }
        }
        catch ( final RecognitionException re )
        {
            // Failed to parse the input query.
            throw new Exception(re.toString());
        }
        finally
        {
            parseTree = null;
        }
    }

    /**
     * <p>
     * Returns the stripped input {@link java.lang.String}.
     * </p>
     * 
     * @return the stripped input {@link java.lang.String}.
     * @since 1.1
     */
    public final String getStrippedInput()
    {
        return Stripper.strip(this.getOriginalInput());
    }

    /**
     * <p>
     * Return the value of the isDebugMode flag.
     * </p>
     * <p>
     * True to print the SQL table in addition to the XML tags, false otherwise.
     * </p>
     * 
     * @return the value of the isDebugMode flag.
     * @since 1.1
     */
    public final boolean isDebugMode()
    {
        return this.isDebugMode;
    }

    /**
     * <p>
     * Return the value of the isDTD flag.
     * </p>
     * <p>
     * True to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
     * </p>
     * 
     * @return the value of the isDTD flag.
     * @since 1.1
     */
    public final boolean isDTD()
    {
        return this.isDTD;
    }

    /**
     * <p>
     * Return the value of the isModified flag.
     * </p>
     * <p>
     * True to use the {@link dml.team5.antlr.ModifiedPLSQLParser}, false to use the {@link dml.team5.antlr.PLSQLParser}.
     * </p>
     * 
     * @return the value of the isModified flag.
     * @since 1.1
     */
    public final boolean isModified()
    {
        return this.isModified;
    }

    /**
     * <p>
     * Return the value of the isOracle flag.
     * </p>
     * <p>
     * True to connect to an Oracle database, false to connect to a MySQL database.
     * </p>
     * 
     * @return the value of the isOracle flag.
     * @since 1.1
     */
    public final boolean isOracle()
    {
        return this.isOracle;
    }

    /**
     * <p>
     * Allows the isDebugMode flag to be altered.
     * </p>
     * <p>
     * True to print the SQL table in addition to the XML tags, false otherwise.
     * </p>
     * 
     * @param isDebugMode
     *            the new value for the isDebugMode flag.
     * @since 1.1
     */
    protected final void setDebugMode(final boolean isDebugMode)
    {
        this.isDebugMode = isDebugMode;
    }

    /**
     * <p>
     * Allows the isDTD flag to be altered.
     * </p>
     * <p>
     * True to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
     * </p>
     * 
     * @param isDTD
     *            the new value for the isDTD flag.
     * @since 1.1
     */
    protected final void setDTD(final boolean isDTD)
    {
        this.isDTD = isDTD;
    }

    /**
     * <p>
     * Allows the isModified flag to be altered.
     * </p>
     * <p>
     * True to use the {@link dml.team5.antlr.ModifiedPLSQLParser}, false to use the {@link dml.team5.antlr.PLSQLParser}.
     * </p>
     * 
     * @param isModified
     *            the new value for the isModified flag.
     * @since 1.1
     */
    protected final void setModified(final boolean isModified)
    {
        this.isModified = isModified;
    }

    /**
     * <p>
     * Allows the isOracle flag to be altered.
     * </p>
     * <p>
     * True to connect to an Oracle database, false to connect to a MySQL database.
     * </p>
     * 
     * @param isOracle
     *            the new value for the isOracle flag.
     * @since 1.1
     */
    protected final void setOracle(final boolean isOracle)
    {
        this.isOracle = isOracle;
    }
}
