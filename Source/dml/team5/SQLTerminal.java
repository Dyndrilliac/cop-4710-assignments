package dml.team5;
import java.util.Scanner;
import java.util.Arrays;
public class SQLTerminal{
	static String SQLInput;
	static String SQLOutput;
	static String XMLInput;
	static String XMLOutput;
	private static Scanner userInput = new Scanner(System.in);
	static String temp;
	static int option;
	public static void main(String args[]){
		while(true){
			System.out.println("**********Option Menu**********");
			System.out.println("1. DTD");
			System.out.println("2. XSD");
			System.out.println("*******************************");
			temp = userInput.nextLine();
			if(temp.equals("1")){
				option = 1;
				break;
			}else if(temp.equals("2")){
				option = 2;
				break;
			}else{
				System.out.println("The option you entered is not valid.  Please try again.");
				continue;
			}
		}
		System.out.println("If at any time you wish to switch between DTD and XSD, enter 1 for DTD or 2 for XSD in the Command Line.");
		SQLInput = "";
		while(true){
			temp = "";
			System.out.println("Command Line Input: ");
			temp = userInput.nextLine();
			System.out.println(temp);
			if(temp.length() == 1 && temp.charAt(0) == '1'){
				System.out.println("You have changed output to DTD.");
				option = 1;
				continue;
			}else if(temp.length() == 1 && temp.charAt(0) == '2'){
				System.out.println("You have changed output to XSD.");
				option = 2;
				continue;
			}else if(temp.equals("exit")){
				System.out.println("Now exiting.");
				System.exit(0);
			}
			if(!temp.contains(";")){
				if(SQLInput.length() == 0){
					SQLInput = temp;
				}else{
					SQLInput = SQLInput.concat(" ").concat(temp);
				}
				continue;
			}else{
				if(SQLInput.length() == 0){
					SQLInput = temp;
				}else{
					SQLInput = SQLInput.concat(" ").concat(temp);
				}
			}
			// call the connect class
			SQLOutput = stripper.strip(SQLInput);
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
class stripper{
	public static String[][] changesArray;
	private static String output;
	static String strip(String input){
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
		boolean notAs = false;
		boolean notFrom = false;
		while(true){
			input = input.toLowerCase();
			for(int i = 0; i < input.length(); i++){
				currentChar = input.charAt(i);
				if(currentChar == '<'){
					changesArray[changesIndex][0] = "group";
					groupMarker++;
					current = "";
					groupIdNeed = true;
				}else if (currentChar == '>'){
					for(int j = changesIndex; j > -1; j--){
						if(changesArray[j][0] != null && changesArray[j][0].equals("group")){
							if(changesArray[j][2] != null){
								continue;
							}
							changesArray[j][2] = previous;
							break;
						}
					}
					groupMarker--;
				}
				else if (currentChar == '+'){
					compressMarker = true;
					continue;
				}else if (!notAs && currentChar == 'a'){
					if(current.length() > 0){
						notAs = true;
						i--;
						continue;
					}
					if(input.charAt(i+1) == 's'){
						if (!Character.isLowerCase(input.charAt(i+2))){
							changesArray[changesIndex][0] = "rename";
							changesArray[changesIndex][1] = previous;
							output = output + "as";
							i++;
							asMarker = true;
						}else{
							notAs = true;
							i--;
						}
					}else{
						notAs = true;
						i--;
					}
				}else if(!notFrom && currentChar == 'f'){
					System.out.println("caught f");
					if (current.length() > 0){
						System.out.println("caught here");
						current = current + currentChar;
						output = output + currentChar;
						continue;
					}
					if(input.charAt(i+1) == 'r'){
						System.out.println("caught r");
						if(input.charAt(i+2) == 'o'){
							System.out.println("caught o");
							if(input.charAt(i+3) == 'm'){
								System.out.println("caught m");
								if (!Character.isLowerCase(input.charAt(i+4))){
									System.out.println("caught from");
									for (int k = i; k < input.length(); k++){
										currentChar = input.charAt(k);
										output = output + currentChar;
									}
									System.out.println(Arrays.deepToString(changesArray));
									return output;
								}else{
									notFrom = true;
									i--;
									System.out.println("thinks theres more after caught from ");
								}
							}else{
								notFrom = true;
								i--;
							}
						}else{
							notFrom = true;
							i--;
							System.out.println("caught only f");
						}
					}else{
						notFrom = true;
						i--;
					}
				}else if(Character.isLowerCase(currentChar)|| currentChar == '.' 
						|| currentChar == '_' || Character.isDigit(currentChar)
						|| currentChar == '$' || currentChar == '#'){
					if(asMarker){
						while (Character.isLowerCase(currentChar) || currentChar == '.' 
								|| currentChar == '_' || Character.isDigit(currentChar)
								|| currentChar == '$' || currentChar == '#'){
							current = current + currentChar;
							i++;
							currentChar = input.charAt(i);
						}
						output = output + current;
						changesArray[changesIndex][2] = current;
						changesIndex++;
						asMarker = false;
						i--;
					}else if(groupIdNeed){
						while (Character.isLowerCase(currentChar) || currentChar == '.' 
								|| currentChar == '_' || Character.isDigit(currentChar)
								|| currentChar == '$' || currentChar == '#'){
							current = current + currentChar;
							i++;
							currentChar = input.charAt(i);
						}
						changesArray[changesIndex][1] = current;
						output = output + current;
						if(compressMarker){
							changesArray[changesIndex][3] = "compress";
							compressMarker = false;
						}
						changesIndex++;
						groupIdNeed = false;
						i--;
					}else if(compressMarker){
						while (Character.isLowerCase(currentChar) || currentChar == '.' 
								|| currentChar == '_' || Character.isDigit(currentChar)
								|| currentChar == '$' || currentChar == '#'){
							current = current + currentChar;
							i++;
							currentChar = input.charAt(i);
						}
						changesArray[changesIndex][0] = "compress";
						changesArray[changesIndex][1] = current;
						output = output + current;
						changesIndex++;
						i--;
						compressMarker = false;
					}else{
						current = current + currentChar;
						notAs = true;
						notFrom = true;
						output = output + currentChar;
					}
				}else{
					if(asMarker){
						output = output + currentChar;
						continue;
					}else if (groupIdNeed || compressMarker){
						output = output + currentChar;
						continue;
					}else{
						notAs = false;
						notFrom = false;
						output = output + currentChar;
						if(current.length() > 0){
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
// select distinct s.sname as salesperson_name, <+ customer, orders.cno as customer_no, orders.totqty,> from s, orders where s.sno = orders.sno;
// select distinct faculty.address as address, + faculty.salary as salary from faculty;