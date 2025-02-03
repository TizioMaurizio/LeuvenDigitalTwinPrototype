package Utility;

import java.util.Scanner;

import static Utility.GlobalKeywords.*;
import static Utility.GlobalKeywords.BLANK_LINE;
import static Utility.StdErrorCodes.*;

/**
 * handles string to int conversion with error checking
 */
public class IntScanner {
	/**
	 * Handles the input of integer values, with support to control keywords. Requires choice domain [1, n]
	 * @param input is the Scanner to take input from
	 * @return the int representing the choice, in range [0, n-1]
	 * OR 1 for Yes, 0 for No
	 * OR the Standard Error Code associated with the entered string
	 */
	public static int nextInt(Scanner input){
		String inputLine=input.nextLine();
		return nextInt(inputLine);
	}

	/**
	 * Handles the input of integer values, with support to control keywords. Requires choice domain [1, n]
	 * @param inputLine is the String to extract int from
	 * @return the int representing the choice, in range [0, n-1]
	 * OR 1 for Yes, 0 for No
	 * OR the Standard Error Code associated with the entered string
	 */
	public static int nextInt(String inputLine){
		int choiceNumber=0;
		try {
			choiceNumber= Integer.parseInt(inputLine);
			if (choiceNumber<1) {//choice is not valid
				choiceNumber=ERR_SELECT_NOT_VALID.getIntValue();
			}
			else {
				choiceNumber--;
			}
		}
		catch (NumberFormatException e){//not a number
			switch (inputLine){
				case STOP_CMD:{
					choiceNumber= STOP.getIntValue();
					break;
				}
				case CANCEL_TURN_CMD:{
					choiceNumber= CANCEL_ACTION.getIntValue();
					break;
				}
				case BLANK_LINE:{
					choiceNumber=StdErrorCodes.BLANK_LINE.getIntValue();
					break;
				}
				case EXIT_TURN_CMD:{
					choiceNumber= CANCEL_ACTION.getIntValue();
					break;
				}
				case "y":
					choiceNumber=1;
					break;
				case "Y":
					choiceNumber=1;
					break;
				case "n":
					choiceNumber=0;
					break;
				case "N":
					choiceNumber=0;
					break;
				default:
					choiceNumber= UNKNOWN.getIntValue();

			}
		}
		return choiceNumber;
	}

}
