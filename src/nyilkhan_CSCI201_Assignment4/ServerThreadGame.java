package nyilkhan_CSCI201_Assignment4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ServerThreadGame extends Thread{

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private GameServer gs;
	private Lock lock;
	private Condition turn;
	private boolean first;
	private int serverIndex;
	
	
	public ServerThreadGame(Socket s, GameServer gs, Lock lock, Condition turn, boolean first, int serverIndex) {
		this.lock = lock;
		this.turn = turn;
		this.first = first;
		this.serverIndex = serverIndex;
		
		
		try {
			this.gs = gs;
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			
			//gs.gameLock.lock();
			if(this.first) {
				UserMessage um = new UserMessage("How Many Players will there Be? \n");
				this.sendPrivateMessage(um);
				
				try {
					String numPlayerString = ((UserMessage)ois.readObject()).getMessage();
					int num = Integer.parseInt(numPlayerString);
					this.gs.numPlayers = (num);
					
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
			else {
				UserMessage um = new UserMessage("There is a game already waiting for you. Player " + this.serverIndex + " has already joined.");
				this.sendPrivateMessage(um);
			}
			
			
			this.start();
		}catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}finally {
			//gs.gameLock.unlock();
		}
	}
	
	public void sendPrivateMessage(UserMessage um) {
		try {
			oos.writeObject(um);
			oos.flush();
		}catch(IOException ioe) {
			System.out.println("ioe 1" + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while(true) {
				lock.lock();
				if(this.first) {
					this.first=false;
				}
				
				try {
					turn.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//allows the player to keep playing until it is not his turn
				boolean notWrong = true;
				//keeps track of if the game is over
				boolean gameOver = false;
				
				
				while(notWrong) {
					//finds the words that are still in play (different between allQuestions, and a vector of questions that have been answered
					Vector<Question> stillInPlay = new Vector<Question>();
					stillInPlay = GameBoard.compAnswers(gs.gb.allQuestions,gs.gb.answered);
					
					//if there are no words left
					if(stillInPlay.size() == 0) {
						int indexMaxPoints = -1;
						int maxPoints = 0;
						printBoardEachRound(gs.gb.allQuestions);
						for(int i = 0; i < gs.numPlayers; i++) {
							UserMessage mssg = new UserMessage("Player " + (i+1) + " - " + (gs.points.get(i)) + " correct answers. \n");
							gs.broadcastLocal(mssg, null);
							if(gs.points.get(i) > maxPoints) {
								indexMaxPoints = i;
								maxPoints = gs.points.get(i);
							}
							//if there is a tie, set it to -10
							else if(gs.points.get(i) == maxPoints) {
								indexMaxPoints = 1000;
							}
						}
						//if there was a tie
						if(indexMaxPoints == 1000) {
							UserMessage mssg = new UserMessage("There was a tie \n");
							gs.broadcast(mssg, null);
						}
						else {
							UserMessage mssg = new UserMessage("Player " + (indexMaxPoints + 1) + " is the winner \n");
							gs.broadcast(mssg, null);
						}
						gameOver = true;
						break;
					}
					
					
					printBoardEachRound(stillInPlay);
					UserMessage mssg = new UserMessage("Player " + (this.serverIndex+1) + "'s turn \n");
					gs.broadcast(mssg, this);

					int index = -1;
					UserMessage um;
					String num = "";
					
					while(index == -1) {
						um = new UserMessage("Would you like to answer a question across (a) or down (d)? \n");
						this.sendPrivateMessage(um);
						String aOrD = ((UserMessage)ois.readObject()).getMessage();
						
						//asks user to enter a move that works (must be a or d)
						while(!aOrD.contentEquals("a") && !aOrD.contentEquals("d")) {
							um = new UserMessage("That is not a valid option?\n Would you like to answer a question across (a) or down (d)?? \n");
							this.sendPrivateMessage(um);
							aOrD = ((UserMessage)ois.readObject()).getMessage();
						}
						//asks user for number they want to answer
						um = new UserMessage("What number? \n");
						this.sendPrivateMessage(um);
						
						
						////CHECK IF IT IS A VALID NUMBER!!!!!!!!!!////////////
						num = ((UserMessage)ois.readObject()).getMessage();
						int numToPlay = Integer.parseInt(num);
						///////////////////////////
						
						
						//finds the actual questions that is trying to be answered
						for(int i = 0; i < stillInPlay.size(); i++) {
							//if it is across, we look for the question with the specified number and across
							if(aOrD.contentEquals("a")) {
								if(stillInPlay.get(i).getNumber() == numToPlay && stillInPlay.get(i).isAcross()) {
									index = i;
									break;
								}
							}
							//if it is down, we look for the question with the specified number and down
							else if(aOrD.contentEquals("d")){
								if(stillInPlay.get(i).getNumber() == numToPlay && !stillInPlay.get(i).isAcross()) {
									index = i;
									break;
								}
							}
						}
						//if the move was not found, ask user to try again
						if(index == -1) {
							um = new UserMessage("Please enter a valid move \n");
							this.sendPrivateMessage(um);
						}
					}
						
					
					String guess = "";
					//asks the currPlayer for a guess, and tells all players what he guessed
					if(stillInPlay.get(index).isAcross()) {
						um = new UserMessage("What is your guess for "+ num + " across? \n");
						this.sendPrivateMessage(um);
						guess = ((UserMessage)ois.readObject()).getMessage();
						um = new UserMessage("Player " + (this.serverIndex+1) + " guessed '"+ guess+ "' for " + num + " across. \n");
						gs.broadcast(um, this);
						
					}
					else {
						um = new UserMessage("What is your guess for "+ num + " down? \n");
						this.sendPrivateMessage(um);
						
						guess = ((UserMessage)ois.readObject()).getMessage();
						um = new UserMessage("Player " + (this.serverIndex+1) + " guessed '"+ guess+ "' for " + num + " down. \n");
						gs.broadcast(um, this);
					}
					
					
					//if the user guesses correctly
					if(guess.toLowerCase().contentEquals(stillInPlay.get(index).getAnswer())) {
						um = new UserMessage("This is correct! \n");
						this.sendPrivateMessage(um);
						gs.gb.makeVisable(stillInPlay.get(index));
						gs.broadcast(um, this);
						//adds the question that was answered to vector of answered questions
						gs.gb.answered.add(stillInPlay.get(index));
						int currPoint = (gs.points.get(this.serverIndex))+1;
						gs.points.set(this.serverIndex, currPoint);
					}
					//if the user does not guess correctly
					else {
						um = new UserMessage("This is not correct! \n");
						this.sendPrivateMessage(um);
						gs.broadcast(um, this);
						if(gs.numPlayers != 1) {
							notWrong = false;
						}
						//notWrong = false;
					}	
				}
				//if the game has ended
				if(gameOver) {
					gs.newGame();
					return;
					
				}
				
				gs.signalNext();
				lock.unlock();
				
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			
		}
	}
	
	public void printBoardEachRound(Vector<Question> notAnswered) {
		
		boolean acrossLeft = false;
		boolean downLeft = false;
		
		String boardState = gs.gb.printStringBoard();
		UserMessage um = new UserMessage(boardState);
		
		gs.broadcastLocal(um, null);		
		for(int j = 0; j < notAnswered.size();j++) {
			if(notAnswered.get(j).isAcross()) {
				acrossLeft = true;
			}
		}
		if(acrossLeft) {
			String across = "ACROSS";
			UserMessage acc = new UserMessage(across);
			gs.broadcastLocal(acc, null);
			//if we do, we show 
			for(int j = 0; j < notAnswered.size();j++) {
				if(notAnswered.get(j).isAcross()) {
					String mssg = notAnswered.get(j).getNumber() + " " + notAnswered.get(j).getQuestion();
					UserMessage tempMessage = new UserMessage(mssg);
					gs.broadcastLocal(tempMessage,null);
				}
			}
			acc = new UserMessage("\n");
			gs.broadcastLocal(acc, null);
		}
		//itarates through the not answered questions to see if we have any across words
		for(int j = 0; j <notAnswered.size();j++) {
			if(!(notAnswered.get(j).isAcross())) {
				downLeft = true;
			}
		}
		if(downLeft) {
			String down = "DOWN";
			UserMessage acc = new UserMessage(down);
			gs.broadcastLocal(acc,null);
			//if we do, we show 
			for(int j = 0; j < notAnswered.size();j++) {
				if(!notAnswered.get(j).isAcross()) {
					String mssg = notAnswered.get(j).getNumber() + " " + notAnswered.get(j).getQuestion();
					UserMessage tempMessage = new UserMessage(mssg);
					gs.broadcastLocal(tempMessage,null);
				}
			}
			acc = new UserMessage("\n");
			gs.broadcastLocal(acc, null);
		}
		
		
	}
	
	
	
	
	public static void main(String[] args) {

	}

}
