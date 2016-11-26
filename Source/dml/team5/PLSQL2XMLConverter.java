
package dml.team5;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import javax.sql.rowset.CachedRowSet;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This class converts the relational database output of both unmodified and modified Oracle PL/SQL selection statements into XML.
 * 
 *          Command-line usage: {@link dml.team5.PLSQL2XMLConverter} InputString1 InputString2 ... InputStringN
 */
public final class PLSQL2XMLConverter extends LanguageConverter
{
    /*
     * Declare private static variables.
     */
    private static final boolean DEBUG = true;

    /**
     * Simple console-based command-line test driver. Each argument is an input {@link java.lang.String}.
     * 
     * @param args
     *            the array of command-line arguments.
     * @since 1.0
     */
    public static final void main(final String[] args)
    {
        final boolean isDTD = true;

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
                System.out.println(new PLSQL2XMLConverter(args[i], isDTD));
            }
        }
        else
        {
            // If no input is given, show the user how to invoke the program.
            System.out.println("Usage: " + args[0] + " InputString1 InputString2 ... InputStringN");
            System.out.println("Remember to enclose strings containing whitepace with double-quotes!");
        }
    }

    /*
     * Declare private instance variables.
     */
    private boolean isDTD      = true;
    private boolean isModified = false;

    /**
     * Constructs a new instance of {@link dml.team5.PLSQL2XMLConverter} based on an input {@link java.lang.String}.
     * 
     * @param inputString
     *            the input {@link java.lang.String}.
     * @param isDTD
     *            true to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
     * @since 1.0
     */
    public PLSQL2XMLConverter(final String inputString, final boolean isDTD)
    {
        // Set the original input.
        super(inputString.trim());

        // Set whether DTD or XSD.
        this.setDTD(isDTD);

        // Execute conversion process.
        this.convert();
    }

    /**
     * Executes the conversion process: parses input, strips out SQL modifications, connects to database, executes input, and converts the output into XML.
     * 
     * @since 1.0
     */
    @Override
    protected final void convert()
    {
        // Get the input.
        String input = this.getOriginalInput();

        // Declare variables to keep track of the parse tree, the connection, and the query results.
        ParseTree parseTree = null;
        Connection connection = null;
        CachedRowSet results = null;

        try
        {
            // Try to parse the input query silently as unmodified PL/SQL.
            parseTree = Utility.getParseTree(input, false, true);

            if ( parseTree == null )
            {
                // If the silent parse fails, set the isModified flag to true.
                this.setModified(false);

                // Try to parse the input query as modified PL/SQL.
                parseTree = Utility.getParseTree(input, true, false);

                // If the input query is modified PL/SQL, strip out the modifications and make it unmodified PL/SQL again.
                input = this.getStrippedInput();
            }

            // Try to parse the input query as unmodified PL/SQL.
            parseTree = Utility.getParseTree(input, false, false);

            try
            {
                // Connect to the Oracle database server.
                connection = Utility.getConnection(false);

                // Execute the input query.
                results = Utility.executeSQLStatement(connection, input);

                // Make a mapping of the database's schema, matching column names to table names.
                Utility.buildSchemaMap(connection);

                // Try to close the connection.
                connection.close();

                if ( results != null )
                {
                    // Is the input query a select statement?
                    if ( input.toLowerCase(Locale.ROOT).startsWith("select") )
                    {
                        // If debugging, print out the table too.
                        if ( PLSQL2XMLConverter.DEBUG )
                        {
                            System.out.println(Utility.writeSQLResults(results));
                        }

                        // Construct the output XML.
                        this.setOutputString(Utility.writeXMLResults(results, this, this.isDTD()));
                    }
                }

                // Try to close the result set.
                results.close();
            }
            catch ( final SQLException sqle )
            {
                // Failed to connect and execute the input query.
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
            re.printStackTrace();
        }
        finally
        {
            parseTree = null;
        }
    }

    /**
     * Returns the stripped input {@link java.lang.String}.
     * 
     * @return the stripped input {@link java.lang.String}.
     * @since 1.1
     */
    public final String getStrippedInput()
    {
        return Stripper.strip(this.getOriginalInput());
    }

    /**
     * Returns whether or not the input {@link java.lang.String} contains an AS clause.
     * 
     * @return true if the input {@link java.lang.String} contains an AS clause, false otherwise.
     * @since 1.1
     */
    public final boolean hasAsClauseInInput()
    {
        // Declare an unassigned constant String reference.
        final String selected_elements;

        if ( this.isModified() )
        {
            selected_elements = Utility.extractFirstSubStringByPattern(this.getStrippedInput(), Utility.SELECTED_ELEMENT_PATTERN);
        }
        else
        {
            selected_elements = Utility.extractFirstSubStringByPattern(this.getOriginalInput(), Utility.SELECTED_ELEMENT_PATTERN);
        }

        return selected_elements.toLowerCase(Locale.ROOT).contains(" as ");
    }

    /**
     * Return the value of the isDTD flag.
     * True to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
     * 
     * @return the value of the isDTD flag.
     * @since 1.1
     */
    public final boolean isDTD()
    {
        return this.isDTD;
    }

    /**
     * Return the value of the isModified flag.
     * True to use the {@link dml.team5.antlr.ModifiedPLSQLParser}, false to use the {@link dml.team5.antlr.PLSQLParser}.
     * 
     * @return the value of the isModified flag.
     * @since 1.1
     */
    public final boolean isModified()
    {
        return this.isModified;
    }

    /**
     * Allows the isDTD flag to be altered.
     * True to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.
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
     * Allows the isModified flag to be altered.
     * True to use the {@link dml.team5.antlr.ModifiedPLSQLParser}, false to use the {@link dml.team5.antlr.PLSQLParser}.
     * 
     * @param isModified
     *            the new value for the isModified flag.
     * @since 1.1
     */
    protected final void setModified(final boolean isModified)
    {
        this.isModified = isModified;
    }
}
