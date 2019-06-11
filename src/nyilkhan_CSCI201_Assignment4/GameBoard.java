package nyilkhan_CSCI201_Assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class GameBoard {

	public Vector<Question> allQuestions;
	//vector for questions that have been answered
	public Vector<Question> answered;
	private Vector<Question> placedQuestions;
	private Vector<Integer> questionsOnBoard;
	public Square[][] board;
	private boolean boardMade = false;
	public int boardSize;
	
	public GameBoard() {
		allQuestions = new Vector<Question>();
		answered = new Vector<Question>();
		placedQuestions = new Vector<Question>();
		questionsOnBoard = new Vector<Integer>();
		
	}
	
	public void setBoardSize() {
		int acrossChars = 0;
		int downChars = 0;
		//assumes that GB already has the questions
		for(Question q: allQuestions) {
			if(q.isAcross()) {
				acrossChars += q.getAnswer().length();
			}
			else {
				downChars += q.getAnswer().length();
			}
		}
		//create the board according to the maximum number of characters for down and across
		boardSize = (Math.max(acrossChars, downChars))*2;
		board = new Square[boardSize][boardSize];
		//initialize the board with empty squares
		for(int i = 0; i < boardSize; i++) {
			for(int j = 0; j < boardSize; j++) {
				Square sq = new Square('*',j,i,'0');
				this.board[i][j] = sq;
			}
		}
	}
	
	
	
	public void newQuestion(boolean across, int points, String answer, String question) {
		Question q = new Question(across, points, answer, question);
		allQuestions.add(q);
	}
	
	///ask CP if this is correct
	public void clearQuestions() {
		allQuestions = new Vector<Question>();
	}
	

	
	
	//backtracking function
	public boolean generateBoard(Question q) {
		
		//System.out.println(wordToCheck);
		Vector<Question> wordsLeft = compAnswers(allQuestions, placedQuestions);
		if(wordsLeft.size() == 0) {
			//System.out.println("Board should be made now 1");
			boardMade = true;
			//call function to make board
			return true;
		}
		
		//goes through y values
		for(int i = 0; i < boardSize; i++) {
			//goes through x values
			for(int j = 0; j < boardSize; j++) {
				boolean first = firstWord();
				
				if(i==0 && j==0 && first) {
					i = boardSize/2;
					j = boardSize/2;
				}
				
				
				//if the current word is across
				if(q.isAcross()) {
					//System.out.println("Acrosss Word");
					if(this.validStartHoriz(q, board[i][j])) {
						//System.out.println("Was able to find spot for " + q.getAnswer());
						this.placeWord(q, board[i][j]);
						
						//have to put here so wordsLeft vector updates for the word that was just placed
						wordsLeft = compAnswers(allQuestions, placedQuestions);
						//System.out.println(wordsLeft.size());
						if(wordsLeft.size() == 0) {
							//System.out.println("Board should be made now 2");
							boardMade = true;
							//call function to make board
							return true;
						}
						for(int z = 0;z < wordsLeft.size();z++) {
							if(this.generateBoard(wordsLeft.get(z))) {
								return true;
							}
						}
						this.removeWord(q, board[i][j]);
					}
				}
				//if the current word is down
				else {
					//System.out.println("Acrosss Word");
					if(this.validStartVert(q, board[i][j])) {
						//System.out.println("Was able to find spot for " + q.getAnswer() + i + " " + j + " ");
						this.placeWord(q, board[i][j]);
						
						wordsLeft = compAnswers(allQuestions, placedQuestions);
						//System.out.println(wordsLeft.size());
						if(wordsLeft.size() == 0) {
							//System.out.println("Board should be made now 3");
							boardMade = true;
							//call function to make board
							return true;
						}
						for(int z = 0;z < wordsLeft.size();z++) {
							if(this.generateBoard(wordsLeft.get(z))) {
								return true;
							}
						}
						this.removeWord(q, board[i][j]);
					}
				}
			}
		}
		return false;
	}
	
	
	
	public boolean validStartHoriz(Question q, Square sq) {
		String wordToCheck = q.getAnswer();
		int questionNum = q.getNumber();
		
		boolean connectionFound = false;
		int currX = sq.getX();
		int currY = sq.getY();

		//if there are no words on the board, we dont need to connect with anything
		boolean firstWordCheck = this.firstWord();
		if(firstWordCheck) {
			connectionFound = true;
		}
		
		///// if this question is already on the board //////
		if(questionsOnBoard.contains(questionNum)) {
			//if the number of the start is not the same as what is on the board
			if(Character.getNumericValue(board[currY][currX].getNumber()) != questionNum) {
				return false;
			}
		}
		//if the questions is not on the board, but the first square has a different number already on it
		else {
			//if the square has a number in it other than 0
			if(board[currY][currX].getNumber() != '0') {
				return false;
			}
		}
		
		for(int i = 0;i<wordToCheck.length();i++) {
			//if it falls off the board
			if(currY >= boardSize || currX >= boardSize) {
				//System.out.println("1");
				return false;
			}
			if(board[currY][currX].occupied && board[currY][currX].getLetter()==wordToCheck.charAt(i)) {
				connectionFound = true;
			}
			//if letter to the left of first letter
			if(i==0) {
				if(currX > 0 && board[currY][currX-1].occupied) {
					//System.out.println("2");
					return false;
				}
			}
			//if there is a letter to the right of the last letter
			else if(i==wordToCheck.length()-1) {
				if(currX < boardSize-1 && board[currY][currX+1].occupied) {
					//System.out.println("3");
					return false;
				}
			}
			//if letter above is occupied
			if(currY > 0 && !board[currY][currX].occupied && board[currY-1][currX].occupied) {
				//System.out.println("4");
				return false;
			}
			//if letter below is occupied
			else if(currY < boardSize-1 && !board[currY][currX].occupied && board[currY+1][currX].occupied) {
				//System.out.println("5");
				return false;
			}
			//if there is a different letter at the board already
			else if(board[currY][currX].occupied && board[currY][currX].getLetter()!=wordToCheck.charAt(i)) {
				//System.out.println("6");
				return false;
			}
			//if the square is already in an across word
			else if(board[currY][currX].acrossWord) {
				//System.out.println("7");
				return false;
			}
			currX++;
		}
		if(!connectionFound) {
			//System.out.println("8");
			return false;
		}
		return true;
	}
	
	
	public boolean validStartVert(Question q, Square sq) {
		String wordToCheck = q.getAnswer();
		int questionNum = q.getNumber();
		boolean connectionFound = false;
		int currX = sq.getX();
		int currY = sq.getY();
		//if there are no words on the board, we dont need to connect with anything
		boolean firstWordCheck = this.firstWord();
		if(firstWordCheck) {
			connectionFound = true;
		}
		
		///// if this question is already on the board //////
		if(questionsOnBoard.contains(questionNum)) {
			//if the number of the start is not the same as what is on the board
			if(Character.getNumericValue(board[currY][currX].getNumber()) != questionNum) {
				//System.out.println("Not the same number " + Character.getNumericValue(board[currY][currX].getNumber()) + " "+ questionNum);
				return false;
			}
		}
		
		
		
		for(int i = 0; i < wordToCheck.length();i++) {
			if(currY >= this.boardSize || currX >= this.boardSize) {
				//System.out.println("1");
				return false;
			}
			if(this.board[currY][currX].occupied && this.board[currY][currX].getLetter()==wordToCheck.charAt(i)) {
				connectionFound = true;
			}
			//if letter to the top of first letter
			if(i==0) {
				if(currY > 0 && this.board[currY-1][currX].occupied) {
					//System.out.println("2");
					return false;
				}
			}
			//if there is a letter to the bottom of the last letter
			else if(i==wordToCheck.length()-1) {
				if(currY < this.boardSize-1 && this.board[currY+1][currX].occupied) {
					//System.out.println("3");
					return false;
				}
			}
			//if letter to the left is occupied
			if(currX > 0 && !board[currY][currX].occupied && this.board[currY][currX-1].occupied) {
				//System.out.println("4");
				return false;
			}
			//if letter to the right is occupied
			else if(currX < this.boardSize-1 && !board[currY][currX].occupied && this.board[currY][currX+1].occupied ) {
				//System.out.println("5");
				return false;
			}
			//if there is a different letter at the board already
			else if(this.board[currY][currX].occupied && this.board[currY][currX].getLetter()!=wordToCheck.charAt(i)) {
				//System.out.println("6");
				return false;
			}
			//if the square is already in a down word
			else if(this.board[currY][currX].downWord) {
				//System.out.println("7");
				return false;
			}
			currY++;
		}
		if(!connectionFound) {
			//System.out.println("8");
			return false;
		}
		return true;
	}
		
	
	public void placeWord(Question q, Square sq) {
		//make sure to add to placedAnswers Vector
		int currX = sq.getX();
		int currY = sq.getY();
		String word = q.getAnswer();
		int questionNum = q.getNumber();

		for(int i = 0;i < word.length();i++) {
			//if we are looking at the first letter
			if(i==0) {
				char temp = (char)(questionNum +'0');
				this.board[currY][currX].setNumber(temp);
				questionsOnBoard.add(questionNum);
				
				//sets the starting square for the question
				q.setX(currX);
				q.setY(currY);
				
				if(q.isAcross()) {
					board[currY][currX].startAcross = true;
				}
				else{
					board[currY][currX].startDown = true;
				}
			}
			
			//if the square is not occupied with a letter, we fill it in and occupy it
			if(!this.board[currY][currX].occupied) {	
				this.board[currY][currX].setLetter(word.charAt(i));
				this.board[currY][currX].occupied = true;
			}
			this.board[currY][currX].placedWords++;
			if(q.isAcross()) {
				this.board[currY][currX].acrossWord = true;
				currX++;
			}
			else {
				this.board[currY][currX].downWord = true;
				currY++;
			}
		}
		placedQuestions.add(q);
		//System.out.println("Word placed");
		//printBoard();
	}
	
	
	public void removeWord(Question q, Square sq) {
		int currX = sq.getX();
		int currY = sq.getY();
		String word = q.getAnswer();
		for(int i = 0; i < word.length();i++) {
			//is the square is not in another word
			if(this.board[currY][currX].placedWords == 1) {
				this.board[currY][currX].setLetter('*');
				this.board[currY][currX].occupied = false;
				//remove the number from the board IF it is not a part of another word
				
			}
			//remove the number from the board IF it is not a part of another word
			if(i==0) {
				//resets the questions starting square since we are taking it off the board
				q.setX(-1);
				q.setY(-1);
				
				/////
				if(q.isAcross()) {
					board[currY][currX].startAcross = false;
					//if the current 1st square is not the start of a downword
					if(!board[currY][currX].startDown) {
						//reset number in square
						this.board[currY][currX].setNumber(' ');
					}
				}
				else if(!q.isAcross()) {
					board[currY][currX].startDown = false;
					//if the current 1st square is not the start of an across word
					if(!board[currY][currX].startAcross) {
						//reset number in square
						this.board[currY][currX].setNumber(' ');
					}
				}
			}
			
			this.board[currY][currX].placedWords--;
			if(q.isAcross()) {
				this.board[currY][currX].acrossWord = false;
				currX++;
			}
			else {
				this.board[currY][currX].downWord = false;
				currY++;
			}
		}
		placedQuestions.remove(q);
		
		int tempIndex = -1;
		for(int i = 0;i < questionsOnBoard.size();i++) {
			if(questionsOnBoard.get(i) == q.getNumber()) {
				tempIndex = i;
				break;
			}
		}
		questionsOnBoard.remove(tempIndex);
		//System.out.println("Word removed");
	}
	
	
	
	//compares 2 vectors and returns whichever Questions are not in both
	public static Vector<Question> compAnswers(Vector<Question> allQ, Vector<Question> placedQ) {
		Vector<Question> retVec = new Vector<Question>();
		
		for(int i = 0;i < allQ.size();i++) {
			if(!placedQ.contains(allQ.get(i))) {
				retVec.add(allQ.get(i));
			}
		}
		return retVec;
	}
	
	//function to check if there are already any words on the board
	public boolean firstWord() {
		for(int i = 0; i < boardSize; i++) {
			for(int j = 0; j < boardSize; j++) {
				if(this.board[i][j].occupied) {
					return false;
				}
			}
		}
		return true;
	}
	
	//function to just print the board
	public void printBoard() {
		for(int i = 0; i < boardSize; i++) {
			for(int j = 0; j < boardSize; j++) {
				System.out.print(board[i][j].getLetter());
			}
			System.out.print("\n");
		}
	}
	
	//creates the board as a string that can be printed for users
	public String printStringBoard() {
		String ret = "";
		for(int i = 0; i < boardSize; i++) {
			String temp = "";
			boolean foundLetter = false;
			
			for(int j = 0; j < boardSize; j++) {
				if(board[i][j].getLetter() != '*') {
					foundLetter = true;
				}
				//if the square does not have a number in it
				if(board[i][j].getNumber() == '0') {
					temp += " ";
				}
				//if the square does have a number in it
				else {
					temp += board[i][j].getNumber();
				}
				if(board[i][j].visable) {
					temp += board[i][j].getLetter();
				}
				else if(board[i][j].getLetter() != '*'){
					temp += "_";
				}
				else {
					temp += " ";
				}
			}
			temp += "\n";
			if(foundLetter) {
				ret += temp;
			}
		}
		return ret;
	}
	
	//function to make questions on the board visable if the user gets them right
	public void makeVisable(Question q) {
		int currX = q.getX();
		int currY = q.getY();
		//if the question is across
		if(q.isAcross()) {
			for(int i = 0;i < q.getAnswer().length();i++) {
				board[currY][currX+i].visable = true;
			}
		}
		else {
			for(int i = 0;i < q.getAnswer().length();i++) {
				board[currY+i][currX].visable = true;
			}
		}
	}
	
	
	public static void main(String[] args) {
		File gamedata = new File("gamedata/sample2.txt");
		GameBoard gb = new GameBoard();
		System.out.println("STARING!!!!!!!!");
		
		//boolean for whether the current set of words is across or not
		boolean currAcross = false;
		try {
			FileReader fr = new FileReader(gamedata);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while(line != null) {
				if(line.trim().toLowerCase().contentEquals("across")) {
					currAcross = true;
				}
				else if(line.trim().toLowerCase().contentEquals("down")) {
					currAcross = false;
				}
				else {
					String[] parts = line.split("[|]");

					try {
						int points = Integer.parseInt(parts[0]);
						String answer = parts[1];
						String question = parts[2];
						gb.newQuestion(currAcross, points, answer.toLowerCase(), question);
					}
					catch(NumberFormatException nfe){
						System.out.println("nfe" + nfe.getMessage());
						//System.out.println("Not a valid game file5 ");
						
					}catch(NullPointerException npe) {
						System.out.println("npe " + npe.getMessage());
						
					}
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		}catch(FileNotFoundException fnfe) {
			System.out.println("The file " + " could not be found.");
			//System.out.println("Not a valid game file6 ");
			
		}catch(IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
			//System.out.println("Not a valid game file7 ");
		}
		
		gb.setBoardSize();
		System.out.println(gb.boardSize);
		//we are able to find all of the questions
		
		
		if(gb.generateBoard(gb.allQuestions.get(0))) {
			System.out.println("Board Made");
		}
		else {
			System.out.println("Board Not Made Starting at " + gb.allQuestions.get(0).getAnswer());
		}
		//gb.printBoard();
		System.out.println("Board Made: " + gb.boardMade);
		gb.printBoard();
		System.out.println(gb.printStringBoard());
	}

}

class Question{
	
	private int number;
	private String answer;
	private String question;
	private boolean across;
	private int startX = -1;
	private int startY = -1;
	
	public Question(boolean across, int number, String answer, String question) {
		this.number = number;
		this.answer = answer;
		this.question = question;
		this.across = across;
	}
	public boolean isAcross() {
		return this.across;
	}
	public int getNumber() {
		return this.number;
	}
	public String getAnswer() {
		return this.answer;
	}
	public String getQuestion() {
		return this.question;
	}
	public void setX(int x) {
		this.startX = x;
	}
	public void setY(int y) {
		this.startY = y;
	}
	public int getX() {
		return this.startX;
	}
	public int getY() {
		return this.startY;
	}
}


