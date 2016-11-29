
package dml.team5;

/**
 * <p>
 * This class provides a method to strip out our modifications to the SELECT query SQL syntax.
 * </p>
 * 
 * @author Merrillee Palmer (N00449190@ospreys.unf.edu)
 * @version 1.1
 */
public class Stripper
{
    public static String[][] changesArray;
    public static String     input;
    public static String     output;

    /**
     * <p>
     * This method takes an input {@link java.lang.String}, strips out our SQL modifications, and then returns the stripped {@link java.lang.String} as output.
     * </p>
     * 
     * @param input
     *            the input {@link java.lang.String}.
     * @return the the output {@link java.lang.String}.
     * @since 1.0
     */
    public static final String strip(final String input)
    {
        Stripper.input = input;
        Stripper.output = "";
        Stripper.changesArray = new String[100][4];
        int changesIndex = 0;
        char currentChar;
        String previous = "";
        boolean compressMarker = false;
        String current = "";
        boolean asMarker = false;
        boolean groupIdNeed = false;
        boolean notAs = false;
        boolean notFrom = false;

        while ( true )
        {
            String lc_input = input;//.toLowerCase();

            for ( int i = 0; i < lc_input.length(); i++ )
            {
                currentChar = lc_input.charAt(i);

                if ( currentChar == '<' )
                {
                    Stripper.changesArray[changesIndex][0] = "group";
                    current = "";
                    groupIdNeed = true;
                }
                else if ( currentChar == '>' )
                {
                    for ( int j = changesIndex; j > -1; j-- )
                    {
                        if ( Stripper.changesArray[j][0] != null && Stripper.changesArray[j][0].equals("group") )
                        {
                            if ( Stripper.changesArray[j][2] != null )
                            {
                                continue;
                            }

                            Stripper.changesArray[j][2] = previous;
                            break;
                        }
                    }
                }
                else if ( currentChar == '+' )
                {
                    compressMarker = true;
                    continue;
                }
                else if ( !notAs && currentChar == 'a' )
                {
                    if ( current.length() > 0 )
                    {
                        notAs = true;
                        i--;
                        continue;
                    }
                    if ( lc_input.charAt(i + 1) == 's' )
                    {
                        if ( !Character.isLowerCase(lc_input.charAt(i + 2)) )
                        {
                            Stripper.changesArray[changesIndex][0] = "rename";
                            Stripper.changesArray[changesIndex][1] = previous;
                            Stripper.output = Stripper.output + "as";
                            i++;
                            asMarker = true;
                        }
                        else
                        {
                            notAs = true;
                            i--;
                        }
                    }
                    else
                    {
                        notAs = true;
                        i--;
                    }
                }
                else if ( !notFrom && currentChar == 'f' )
                {
                    // System.out.println("caught f");
                    if ( current.length() > 0 )
                    {
                        // System.out.println("caught here");
                        current = current + currentChar;
                        Stripper.output = Stripper.output + currentChar;
                        continue;
                    }
                    if ( lc_input.charAt(i + 1) == 'r' )
                    {
                        // System.out.println("caught r");
                        if ( lc_input.charAt(i + 2) == 'o' )
                        {
                            // System.out.println("caught o");
                            if ( lc_input.charAt(i + 3) == 'm' )
                            {
                                // System.out.println("caught m");
                                if ( !Character.isLowerCase(lc_input.charAt(i + 4)) )
                                {
                                    // System.out.println("caught from");
                                    for ( int k = i; k < lc_input.length(); k++ )
                                    {
                                        currentChar = lc_input.charAt(k);
                                        Stripper.output = Stripper.output + currentChar;
                                    }
                                    // System.out.println(Arrays.deepToString(Stripper.changesArray));
                                    return Stripper.output;
                                }
                                else
                                {
                                    notFrom = true;
                                    i--;
                                    // System.out.println("thinks theres more after caught from ");
                                }
                            }
                            else
                            {
                                notFrom = true;
                                i--;
                            }
                        }
                        else
                        {
                            notFrom = true;
                            i--;
                            // System.out.println("caught only f");
                        }
                    }
                    else
                    {
                        notFrom = true;
                        i--;
                    }
                }
                else if ( Character.isLowerCase(currentChar) || currentChar == '.' || currentChar == '_' )
                {
                    if ( asMarker )
                    {
                        while ( Character.isLowerCase(currentChar) || currentChar == '.' || currentChar == '_' )
                        {
                            current = current + currentChar;
                            i++;
                            currentChar = lc_input.charAt(i);
                        }

                        Stripper.output = Stripper.output + current;
                        Stripper.changesArray[changesIndex][2] = current;
                        changesIndex++;
                        asMarker = false;
                        i--;
                    }
                    else if ( groupIdNeed )
                    {
                        while ( Character.isLowerCase(currentChar) || currentChar == '.' || currentChar == '_' )
                        {
                            current = current + currentChar;
                            i++;
                            currentChar = lc_input.charAt(i);
                        }

                        Stripper.changesArray[changesIndex][1] = current;
                        Stripper.output = Stripper.output + current;

                        if ( compressMarker )
                        {
                            Stripper.changesArray[changesIndex][3] = "compress";
                            compressMarker = false;
                        }

                        changesIndex++;
                        groupIdNeed = false;
                        i--;
                    }
                    else if ( compressMarker )
                    {
                        while ( Character.isLowerCase(currentChar) || currentChar == '.' || currentChar == '_' || Character.isDigit(currentChar) )
                        {
                            current = current + currentChar;
                            i++;
                            currentChar = lc_input.charAt(i);
                        }

                        Stripper.changesArray[changesIndex][0] = "compress";
                        Stripper.changesArray[changesIndex][1] = current;
                        Stripper.output = Stripper.output + current;
                        changesIndex++;
                        i--;
                        compressMarker = false;
                    }
                    else
                    {
                        current = current + currentChar;
                        notAs = true;
                        notFrom = true;
                        Stripper.output = Stripper.output + currentChar;
                    }
                }
                else
                {
                    if ( asMarker )
                    {
                        Stripper.output = Stripper.output + currentChar;
                        continue;
                    }
                    else if ( groupIdNeed || compressMarker )
                    {
                        Stripper.output = Stripper.output + currentChar;
                        continue;
                    }
                    else
                    {
                        notAs = false;
                        notFrom = false;
                        Stripper.output = Stripper.output + currentChar;

                        if ( current.length() > 0 )
                        {
                            previous = current;
                        }

                        current = "";
                    }
                }
            }

            break;
        }
        // System.out.println(Arrays.deepToString(Stripper.changesArray));
        return Stripper.output;
    }
}
// select distinct name as salesperson, <customer, name as custname, <custaddress, street, city, phone,>,>, from s, c, orders, p where s = orders and c = orders and p = orders;
// select distinct s.sname as salesperson_name, <+ customer, orders.cno as customer_no, orders.totqty,> from s, orders where s.sno = orders.sno;
// select distinct faculty.address as address, + faculty.salary as salary from faculty;
