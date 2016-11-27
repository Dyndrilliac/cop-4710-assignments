
package dml.team5;

import java.util.Locale;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This helper class is a simple data structure to store information about selected elements.
 */
public class SelectedElement
{
    public final String columnLabel;
    public final String columnName;
    public final String tableName;

    /**
     * Constructs an instance of {@link dml.team5.SelectedElement}.
     * 
     * @param tableName
     * @param columnName
     * @since 1.1
     */
    public SelectedElement(final String tableName, final String columnName)
    {
        this(tableName, columnName, columnName.toUpperCase(Locale.ROOT));
    }

    /**
     * Constructs an instance of {@link dml.team5.SelectedElement}.
     * 
     * @param tableName
     * @param columnName
     * @param columnLabel
     * @since 1.1
     */
    public SelectedElement(final String tableName, final String columnName, final String columnLabel)
    {
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnLabel = columnLabel.toUpperCase(Locale.ROOT);
    }

    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     * @since 1.1
     */
    @Override
    public boolean equals(final Object obj)
    {
        if ( this == obj ) { return true; }
        if ( obj == null ) { return false; }
        if ( !( obj instanceof SelectedElement ) ) { return false; }

        SelectedElement other = (SelectedElement) obj;

        if ( this.columnLabel == null )
        {
            if ( other.columnLabel != null ) { return false; }
        }
        else if ( !this.columnLabel.equals(other.columnLabel) ) { return false; }

        if ( this.columnName == null )
        {
            if ( other.columnName != null ) { return false; }
        }
        else if ( !this.columnName.equals(other.columnName) ) { return false; }

        if ( tableName == null )
        {
            if ( other.tableName != null ) { return false; }
        }
        else if ( !this.tableName.equals(other.tableName) ) { return false; }

        return true;
    }

    /**
     * @return
     * @see java.lang.Object#hashCode()
     * @since 1.1
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.columnLabel == null ) ? 0 : this.columnLabel.hashCode() );
        result = prime * result + ( ( this.columnName == null ) ? 0 : this.columnName.hashCode() );
        result = prime * result + ( ( this.tableName == null ) ? 0 : this.tableName.hashCode() );
        return result;
    }
}
