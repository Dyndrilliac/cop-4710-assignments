
package dml.team5;

/**
 * <p>
 * This abstract class provides the basic variables and methods necessary for converting from one language to another.
 * </p>
 * 
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 */
public abstract class LanguageConverter
{
    /*
     * Declare private instance variables.
     */
    private String originalInput = "";
    private String outputString  = "";

    /**
     * <p>
     * Constructs a new instance of {@link dml.team5.LanguageConverter} based on an input {@link java.lang.String}.
     * </p>
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
     * <p>
     * Executes the conversion process.
     * </p>
     * 
     * @throws Exception
     *             if there is an error.
     * @since 1.0
     */
    protected abstract void convert() throws Exception;

    /**
     * <p>
     * Returns the input {@link java.lang.String}.
     * </p>
     * 
     * @return the input {@link java.lang.String}.
     * @since 1.0
     */
    public final String getOriginalInput()
    {
        return this.originalInput;
    }

    /**
     * <p>
     * Allows the input {@link java.lang.String} to be altered.
     * </p>
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
     * <p>
     * Allows the output {@link java.lang.String} to be altered.
     * </p>
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
     * <p>
     * Return the output {@link java.lang.String}.
     * </p>
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
