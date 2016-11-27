
package dml.team5;

import dml.team5.antlr.PLSQLBaseListener;
import dml.team5.antlr.PLSQLParser;

/**
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 * 
 *          This helper class walks the {@link org.antlr.v4.runtime.tree.ParseTree} to construct the master TABLES {@link java.util.List}.
 */
public class TableListener extends PLSQLBaseListener
{
    /**
     * Constructs an instance of {@link dml.team5.TableListener}.
     * 
     * @since 1.1
     */
    public TableListener()
    {
        Utility.TABLES.clear();
    }

    /**
     * Adds each table reference seen while walking the {@link org.antlr.v4.runtime.tree.ParseTree} to the master TABLES {@link java.util.List}.
     * 
     * @param ctx
     * @see dml.team5.antlr.PLSQLBaseListener#enterTable_ref(dml.team5.antlr.PLSQLParser.Table_refContext)
     * @since 1.1
     */
    @Override
    public void enterTable_ref(final PLSQLParser.Table_refContext ctx)
    {
        Utility.TABLES.add(ctx.getText().trim());
    }
}
