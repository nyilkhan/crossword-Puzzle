package nyilkhan_CSCI201_Assignment4;

import java.io.Serializable;

public class UserMessage implements Serializable{
	public static final long serialVersionUID = 1;
	private String message;
	
	public UserMessage(String message) {
		this.message = message;
	}
	public UserMessage(GameBoard gb) {
		this.message = "";
		
		String ret = "";
		for(int i = 0; i < gb.boardSize; i++) {
			String temp = "";
			boolean foundLetter = false;
			
			for(int j = 0; j < gb.boardSize; j++) {
				if(gb.board[i][j].getLetter() != '*') {
					foundLetter = true;
				}
				//if the square does not have a number in it
				if(gb.board[i][j].getNumber() == '0') {
					temp += " ";
				}
				//if the square does have a number in it
				else {
					temp += gb.board[i][j].getNumber();
				}
				if(gb.board[i][j].visable) {
					temp += gb.board[i][j].getLetter();
				}
				else if(gb.board[i][j].getLetter() != '*'){
					temp += "_";
				}
				else {
					temp += " ";
				}
			}
			//temp += "\n";
			if(foundLetter) {
				ret += temp;
			}
		}
		
		this.message = ret;
	}
	public String getMessage() {
		return this.message;
	}
	

}
