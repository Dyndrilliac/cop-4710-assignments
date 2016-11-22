
package dml.team5;

import java.util.Arrays;
import java.util.Scanner;

public class SQLTerminal
{
    static int             option;
    static String          SQLInput;
    static String          SQLOutput;
    static String          temp;
    private static Scanner userInput = new Scanner(System.in);
    static String          XMLInput;
    static String          XMLOutput;

    public static void main(String args[])
    {
        while ( true )
        {
            System.out.println("**********Option Menu**********");
            System.out.println("1. DTD");
            System.out.println("2. XSD");
            System.out.println("*******************************");
            temp = userInput.nextLine();
            if ( temp.equals("1") )
            {
                option = 1;
                break;
            }
            else if ( temp.equals("2") )
            {
                option = 2;
                break;
            }
            else
            {
                System.out.println("The option you entered is not valid.  Please try again.");
                continue;
            }
        }
        System.out.println("If at any time you wish to switch between DTD and XSD, enter 1 for DTD or 2 for XSD in the Command Line.");
        SQLInput = "";
        while ( true )
        {
            temp = "";
            System.out.println("Command Line Input: ");
            temp = userInput.nextLine();
            System.out.println(temp);
            if ( temp.length() == 1 && temp.charAt(0) == '1' )
            {
                System.out.println("You have changed output to DTD.");
                option = 1;
                continue;
            }
            else if ( temp.length() == 1 && temp.charAt(0) == '2' )
            {
                System.out.println("You have changed output to XSD.");
                option = 2;
                continue;
            }
            else if ( temp.equals("exit") )
            {
                System.out.println("Now exiting.");
                System.exit(0);
            }
            if ( !temp.contains(";") )
            {
                if ( SQLInput.length() == 0 )
                {
                    SQLInput = temp;
                }
                else
                {
                    SQLInput = SQLInput.concat(" ").concat(temp);
                }
                continue;
            }
            else
            {
                if ( SQLInput.length() == 0 )
                {
                    SQLInput = temp;
                }
                else
                {
                    SQLInput = SQLInput.concat(" ").concat(temp);
                }
            }
            // call the connect class
            SQLOutput = Stripper.strip(SQLInput);
            System.out.println(SQLOutput);
            // call class to strip
            // call class to parse
            // call class to send to oracle
            // call class to create XML
            // call the close class
            SQLInput = "";
        }
    }
}

class Stripper
{
    public static String[][] changesArray;
    private static String    output;

    static String strip(String input)
    {
        output = "";
        changesArray = new String[100][4];
        int changesIndex = 0;
        char currentChar;
        String previous = "";
        boolean compressMarker = false;
        String current = "";
        boolean asMarker = false;
        boolean groupIdNeed = false;
        int groupMarker = 0;
        while ( true )
        {
            input = input.toLowerCase();
            for ( int i = 0; i < input.length(); i++ )
            {
                currentChar = input.charAt(i);
                if ( currentChar == '<' )
                {
                    changesArray[changesIndex][0] = "group";
                    groupMarker++;
                    current = "";
                    groupIdNeed = true;
                }
                else if ( currentChar == '>' )
                {
                    for ( int j = changesIndex; j > -1; j-- )
                    {
                        if ( changesArray[j][0] == "group" )
                        {
                            if ( changesArray[j][3] != null )
                            {
                                continue;
                            }
                            changesArray[j][3] = previous;
                            break;
                        }
                    }
                    i++;
                    groupMarker--;
                }
                else if ( currentChar == '+' )
                {
                    compressMarker = true;
                    continue;
                }
                else if ( currentChar == 'a' )
                {
                    if ( current.length() > 0 )
                    {
                        current = current + currentChar;
                        output = output + currentChar;
                        continue;
                    }
                    if ( input.charAt(i + 1) == 's' )
                    {
                        if ( !Character.isLowerCase(input.charAt(i + 2)) )
                        {
                            changesArray[changesIndex][0] = "rename";
                            changesArray[changesIndex][1] = previous;
                            output = output + "as";
                            i++;
                            asMarker = true;
                        }
                        else
                        {
                            current = current + currentChar;
                            output = output + currentChar;
                        }
                    }
                    else
                    {
                        current = current + currentChar;
                        output = output + currentChar;
                    }
                }
                else if ( currentChar == 'f' )
                {
                    if ( current.length() > 0 )
                    {
                        current = current + currentChar;
                        output = output + currentChar;
                        continue;
                    }
                    if ( input.charAt(i + 1) == 'r' )
                    {
                        if ( input.charAt(i + 2) == 'o' )
                        {
                            if ( input.charAt(i + 3) == 'm' )
                            {
                                if ( !Character.isLowerCase(input.charAt(i + 2)) )
                                {
                                    for ( int k = i; i < input.length(); k++ )
                                    {
                                        currentChar = input.charAt(k);
                                        output = output + currentChar;
                                    }
                                    System.out.println(Arrays.deepToString(changesArray));
                                    return output;
                                }
                                else
                                {
                                    current = current + currentChar;
                                    output = output + currentChar;
                                }
                            }
                            else
                            {
                                current = current + currentChar;
                                output = output + currentChar;
                            }
                        }
                        else
                        {
                            current = current + currentChar;
                            output = output + currentChar;
                        }
                    }
                    else
                    {
                        current = current + currentChar;
                        output = output + currentChar;
                    }
                }
                else if ( Character.isLowerCase(currentChar) )
                {
                    if ( asMarker )
                    {
                        while ( Character.isLowerCase(currentChar) )
                        {
                            current = current + currentChar;
                            i++;
                            currentChar = input.charAt(i);
                        }
                        output = output + current;
                        changesArray[changesIndex][2] = current;
                        changesIndex++;
                        asMarker = false;
                        i--;
                    }
                    else if ( groupIdNeed )
                    {
                        while ( Character.isLowerCase(currentChar) )
                        {
                            current = current + currentChar;
                            i++;
                            currentChar = input.charAt(i);
                        }
                        changesArray[changesIndex][1] = current;
                        output = output + current;
                        if ( compressMarker )
                        {
                            changesArray[changesIndex][4] = "compress";
                            compressMarker = false;
                        }
                        changesIndex++;
                        groupIdNeed = false;
                        i--;
                    }
                    else
                    {
                        current = current + currentChar;
                        output = output + currentChar;
                    }
                }
                else
                {
                    if ( asMarker )
                    {
                        output = output + currentChar;
                        continue;
                    }
                    else if ( groupIdNeed || compressMarker )
                    {
                        output = output + currentChar;
                        continue;
                    }
                    else
                    {
                        output = output + currentChar;
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
        System.out.println(Arrays.deepToString(changesArray));
        return output;
    }
}
// select distinct name as salesperson, <customer, name as custname, <custaddress, street, city, phone,>,>, from s, c, orders, p where s = orders and c = orders and p = orders;
