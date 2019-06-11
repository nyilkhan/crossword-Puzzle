package nyilkhan_CSCI201_Assignment4;

public class Square {
	public boolean acrossWord;
	public boolean downWord;
	public boolean startAcross;
	public boolean startDown;
	private char number = '0';
	private char letter;
	public boolean occupied;
	private int xCoord;
	private int yCoord;
	public int placedWords;
	public boolean visable = false;
	
	public Square(char letter, int xCoord, int yCoord, char num) {
		this.letter = letter;
		this.number = num;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.placedWords = 0;
		this.occupied = false;
		this.acrossWord = false;
		this.downWord = false;
		this.startAcross = false;
		this.startDown = false;
	}
	public void setNumber(char n) {
		this.number = n;
	}
	public char getNumber() {
		return this.number;
	}
	public void setLetter(char l) {
		this.letter = l;
	}
	public char getLetter(){
		return this.letter;
	}
	public int getX() {
		return this.xCoord;
	}
	public int getY() {
		return this.yCoord;
	}
}
