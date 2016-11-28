
package dml.team5;

import java.util.Locale;
import java.util.Scanner;

/**
 * <p>
 * This class contains a more sophisticated console driver to interact with the user and control the program.
 * </p>
 * 
 * @author Merrillee Palmer (N00449190@ospreys.unf.edu)
 * @author Matthew Boyette (N00868808@ospreys.unf.edu)
 * @version 1.1
 */
public class SQLTerminal
{

    /**
     * <p>
     * Sophisticated console-based driver which interacts with the user and controls the program.
     * </p>
     * 
     * @param args
     *            the array of command-line arguments.
     * @since 1.0
     */
    public static final void main(final String args[])
    {
        Scanner userInput = new Scanner(System.in);
        String input = "", output = "";
        boolean exitFlag = false, isOracle = true, isDebugMode = false, isDTD = true;

        System.out.println("Welcome to the SQL2XML program!\n");

        do
        {
            StringBuffer inputBuffer = new StringBuffer();
            boolean isInputDone = false;

            System.out.println("You may enter any valid SQL command below. Terminate input with a semicolon.");
            System.out.println("For a list of special commands, type help. To exit the program, type exit.\n");

            do
            {
                System.out.print("$sql2xml> ");
                System.out.flush();

                String s = userInput.nextLine();

                if ( s.contains(";") )
                {
                    isInputDone = true;
                    s = s.substring(0, s.indexOf(";"));
                }

                inputBuffer.append(s.trim());
            }
            while ( !isInputDone );

            input = inputBuffer.toString().trim();

            if ( !input.isEmpty() )
            {
                System.out.println();

                switch ( input.toLowerCase(Locale.ROOT) )
                {
                    case "help":
                        System.out.println("\thelp:\tdisplay this help menu.");
                        System.out.println("\texit:\texit the program.");
                        System.out.println("\tsetup:\tconfigure custom program settings.\n");
                        break;

                    case "exit":
                        exitFlag = true;
                        break;

                    case "setup":
                        String command = "";
                        boolean setupFlag = false;

                        do
                        {
                            StringBuffer commandBuffer = new StringBuffer();
                            boolean isCommandDone = false;

                            System.out.println("Setup mode engaged. Terminate input with a semicolon.");
                            System.out.println("For a list of properties and their current values, type get.");
                            System.out.println("To change a property's value, type set <propertyName> <newValue>.");
                            System.out.println("To exit setup mode and return to normal execution, type exit.\n");

                            do
                            {
                                System.out.print("$setup> ");
                                System.out.flush();

                                String s = userInput.nextLine();

                                if ( s.contains(";") )
                                {
                                    isCommandDone = true;
                                    s = s.substring(0, s.indexOf(";"));
                                }

                                commandBuffer.append(" " + s.trim());
                            }
                            while ( !isCommandDone );

                            command = commandBuffer.toString().trim();

                            if ( !command.isEmpty() )
                            {
                                System.out.println();

                                switch ( command.toLowerCase(Locale.ROOT) )
                                {
                                    case "exit":
                                        setupFlag = true;
                                        break;

                                    case "get":
                                        System.out.println("\tdatabaseString\t- " + Utility.padRight(ServerSettings.getDatabase(), 30) + " - the name of the database with which to connect.");
                                        System.out.println("\tpasswordString\t- " + Utility.padRight(ServerSettings.getPassword(), 30) + " - the password that corresponds to the provided username.");
                                        System.out.println("\tportString\t- " + Utility.padRight(ServerSettings.getPort(), 30) + " - the port on which the database server is listening for connections.");
                                        System.out.println("\tserverString\t- " + Utility.padRight(ServerSettings.getServer(), 30) + " - the domain or IP address belonging to the database server.");
                                        System.out.println("\tusernameString\t- " + Utility.padRight(ServerSettings.getDatabase(), 30) + " - the name which uniquely identifies each user account.");
                                        System.out.println("\tisOracle\t- " + Utility.padRight(Boolean.toString(isOracle), 30) + " - true to connect to an Oracle database, false to connect to a MySQL database.");
                                        System.out.println("\tisDebugMode\t- " + Utility.padRight(Boolean.toString(isDebugMode), 30) + " - true to print the SQL table in addition to the XML tags, false otherwise.");
                                        System.out.println("\tisDTD\t\t- " + Utility.padRight(Boolean.toString(isDTD), 30) + " - true to use the W3C Data Type Definition (DTD) format, false to use the W3C XML Schema Definition (XSD) format.");
                                        System.out.println();
                                        break;

                                    default:
                                        String[] commandParts = command.split("\\s", 3);

                                        if ( commandParts.length == 3 )
                                        {
                                            if ( commandParts[0].trim().toLowerCase(Locale.ROOT).contentEquals("set") )
                                            {
                                                switch ( commandParts[1].trim() )
                                                {
                                                    case "databaseString":
                                                        ServerSettings.setDatabase(commandParts[2].trim());
                                                        break;

                                                    case "passwordString":
                                                        ServerSettings.setPassword(commandParts[2].trim());
                                                        break;

                                                    case "portString":
                                                        ServerSettings.setPort(commandParts[2].trim());
                                                        break;

                                                    case "serverString":
                                                        ServerSettings.setServer(commandParts[2].trim());
                                                        break;

                                                    case "usernameString":
                                                        ServerSettings.setUsername(commandParts[2].trim());
                                                        break;

                                                    case "isOracle":
                                                        isOracle = Boolean.parseBoolean(commandParts[2].trim());
                                                        break;

                                                    case "isDebugMode":
                                                        isDebugMode = Boolean.parseBoolean(commandParts[2].trim());
                                                        break;

                                                    case "isDTD":
                                                        isDTD = Boolean.parseBoolean(commandParts[2].trim());
                                                        break;

                                                    default:
                                                        System.out.println("Setup error: unrecognized property.\n");
                                                        break;
                                                }
                                            }
                                            else
                                            {
                                                System.out.println("Setup error: unrecognized command.\n");
                                            }
                                        }
                                        else
                                        {
                                            System.out.println("Setup error: incorrect number of command parameters.\n");
                                        }
                                        break;
                                }
                            }
                        }
                        while ( !setupFlag );
                        break;

                    default:
                        output = ( new PLSQL2XMLConverter(input, isOracle, isDebugMode, isDTD) ).toString();
                        System.out.println(output);
                        break;
                }
            }

            System.out.flush();
        }
        while ( !exitFlag );

        userInput.close();
    }
}
