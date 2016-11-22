
package dml.team5;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This class converts the relational database output of both unmodified and modified Oracle PL/SQL selection statements into XML.
 */
public class PLSQL2XMLConverter
{
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
        if ( args.length > 0 )
        {
            for ( int i = 0; i < args.length; i++ )
            {
                // Separate multiple pieces of output with blank lines.
                if ( i > 0 )
                {
                    System.out.println();
                }

                System.out.println(new PLSQL2XMLConverter(args[i]));
            }
        }
        else
        {
            System.out.println("Usage: " + args[0] + " <InputString1> <InputString2> ... <InputStringN>");
            System.out.println("Remember to enclose strings containing whitepace with double-quotes!");
        }
    }

    private boolean isModified = false;
    /*
     * Declare private instance variables.
     */
    private String originalInput = "";
    private String outputString  = "";
    private String strippedInput = "";

    /**
     * Constructs a new instance of {@link dml.team5.PLSQL2XMLConverter} based on an input {@link java.lang.String}.
     * 
     * @param inputString
     *            the input {@link java.lang.String}.
     * @since 1.0
     */
    public PLSQL2XMLConverter(final String inputString)
    {
        this.setOriginalInput(inputString.trim());
        this.setStrippedInput(Stripper.strip(this.getOriginalInput()));

        // Declare variables to keep track of the parse tree, the connection, the query results, and whether we're doing pure SQL or modified SQL.
        ParseTree parseTree = null;
        Connection connection = null;
        CachedRowSet results = null;
        String input = this.getOriginalInput();

        try
        {
            // Parse the input query as unmodified PL/SQL.
            parseTree = Utility.getParseTree(input, false, true);

            // If the first parse fails, try parsing the input again as modified PL/SQL.
            if ( parseTree == null )
            {
                this.setModified(true);
                parseTree = Utility.getParseTree(input, true, false);
            }

            try
            {
                // Connect to the Oracle database server.
                connection = Utility.getConnection();

                // If the input is modified PL/SQL, strip out the modifications and make it legal SQL again.
                if ( this.isModified() )
                {
                    input = this.getStrippedInput();
                }

                // Execute the input query.
                results = Utility.executeSQLStatement(connection, input);

                // Is the input query a select statement?
                if ( input.toLowerCase().startsWith("select") )
                {
                    if ( results != null )
                    {
                        // If debugging, print out the table too.
                        if ( PLSQL2XMLConverter.DEBUG )
                        {
                            System.out.println(Utility.writeSQLResults(results));
                        }

                        // Construct the output XML.
                        this.setOutputString(Utility.writeXMLResults(results));
                    }
                }
            }
            catch ( final SQLException sqle )
            {
                // Failed to execute the input query.
                sqle.printStackTrace();
            }
            finally
            {
                try
                {
                    // Try to close the connection.
                    if ( connection != null )
                    {
                        connection.close();
                    }
                }
                catch ( SQLException sqle )
                {
                    // Failed to close the connection.
                    sqle.printStackTrace();
                }

                connection = null;
            }
        }
        catch ( final RecognitionException re )
        {
            // Failed to parse the input query.
            re.printStackTrace();
        }
    }

    /**
     * Returns the input {@link java.lang.String}.
     * 
     * @return the input {@link java.lang.String}.
     * @since 1.0
     */
    public final String getOriginalInput()
    {
        return this.originalInput;
    }

    /**
     * Returns the stripped input {@link java.lang.String}.
     * 
     * @return the stripped input {@link java.lang.String}.
     * @since 1.1
     */
    public final String getStrippedInput()
    {
        return this.strippedInput;
    }

    /**
     * @return the isModified
     */
    public final boolean isModified()
    {
        return isModified;
    }

    /**
     * @param isModified
     *            the isModified to set
     */
    protected final void setModified(boolean isModified)
    {
        this.isModified = isModified;
    }

    /**
     * Allows the input {@link java.lang.String} to be modified.
     * 
     * @param originalInput
     *            the input {@link java.lang.String}.
     * @since 1.0
     */
    protected final void setOriginalInput(final String originalInput)
    {
        this.originalInput = originalInput;
    }

    /**
     * Allows the output {@link java.lang.String} to be modified.
     * 
     * @param outputString
     *            the output {@link java.lang.String}.
     * @since 1.0
     */
    protected final void setOutputString(final String outputString)
    {
        this.outputString = outputString;
    }

    /**
     * Allows the stripped input {@link java.lang.String} to be modified.
     * 
     * @param strippedInput
     *            the stripped input {@link java.lang.String}.
     * @since 1.1
     */
    protected final void setStrippedInput(final String strippedInput)
    {
        this.strippedInput = strippedInput;
    }

    /**
     * Return the output {@link java.lang.String}.
     * 
     * @return the output {@link java.lang.String}.
     * @see java.lang.Object#toString()
     * @since 1.0
     */
    @Override
    public final String toString()
    {
        return this.outputString;
    }
}
