
package dml.team5;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This abstract class provides the basic variables and methods necessary for converting from one language to another.
 */
public abstract class LanguageConverter
{
    /*
     * Declare private instance variables.
     */
    private String originalInput = "";
    private String outputString  = "";

    /**
     * Constructs a new instance of {@link dml.team5.LanguageConverter} based on an input {@link java.lang.String}.
     * 
     * @param inputString
     *            the input {@link java.lang.String}.
     * @since 1.1
     */
    public LanguageConverter(final String inputString)
    {
        // Set the original input.
        this.setOriginalInput(inputString.trim());
    }

    /**
     * Executes the conversion process: parses input, strips out SQL modifications, connects to database, executes input, and converts the output into XML.
     * 
     * @throws Exception
     *             if there is an error.
     * @since 1.0
     */
    protected abstract void convert() throws Exception;

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
     * Allows the input {@link java.lang.String} to be altered.
     * 
     * @param originalInput
     *            the new input {@link java.lang.String}.
     * @since 1.0
     */
    protected final void setOriginalInput(final String originalInput)
    {
        this.originalInput = originalInput;
    }

    /**
     * Allows the output {@link java.lang.String} to be altered.
     * 
     * @param outputString
     *            the new output {@link java.lang.String}.
     * @since 1.0
     */
    protected final void setOutputString(final String outputString)
    {
        this.outputString = outputString;
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
