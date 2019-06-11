package nyilkhan_CSCI201_Assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {

	private Vector<ServerThreadGame> serverThreads;
	private Vector<Lock> lockVec;
	private Vector<Condition> conditionVec;
	private File chosenGame = null;
	public GameBoard gb;
	private int currInd = 0;
	public Vector<Integer> points;
	
	public int numPlayers = 1;
	private int playersJoined = 0;
	public boolean gameEnded = false;

	public GameServer(int port) {

		ServerSocket ss = null;
		boolean firstConn = true;
		points = new Vector<Integer>();
		

		gb = new GameBoard();
		
		
		try {
			ss = new ServerSocket(port);
			System.out.println("Listening on port " + port);
			System.out.println("Waiting for players...");
			
			serverThreads = new Vector<ServerThreadGame>();
			lockVec = new Vector<Lock>();
			conditionVec = new Vector<Condition>();
			
			boolean validGame = true;
			
			if(serverThreads.size() == 0) {
				Socket s = ss.accept();
				System.out.println("Connection from " + s.getInetAddress());
				Lock lock = new ReentrantLock();
				Condition turn = lock.newCondition();
				
				ServerThreadGame stg = new ServerThreadGame(s, this, lock, turn, true, 0);
				serverThreads.add(stg);
				lockVec.add(lock);
				conditionVec.add(turn);
				points.add(0);
				playersJoined++;
				System.out.println("Number of players: " + numPlayers + "\n");
				
				chooseGame();
				if(!checkGame()) {
					//System.out.println("Didn't find valid file");
					UserMessage um = new UserMessage("Didn't find valid file");
					broadcast(um, null);
					validGame = false;
				}
				else {
					gb.setBoardSize();
					System.out.println("Generating Board...");
					gb.generateBoard(gb.allQuestions.get(0));
				}
				

			}
			
			
			
			
			while(playersJoined < numPlayers) {
				
				UserMessage um = new UserMessage("Waiting for player " + (playersJoined + 1) + "\n");
				this.broadcast(um, null);
				
				Socket s = ss.accept();
				System.out.println("Connection from " + s.getInetAddress() + "\n");
				Lock lock = new ReentrantLock();
				Condition turn = lock.newCondition();
				ServerThreadGame stg = new ServerThreadGame(s, this, lock, turn, false, playersJoined);
				serverThreads.add(stg);
				lockVec.add(lock);
				conditionVec.add(turn);
				points.add(0);
				//increments the number of players that have joined
				playersJoined++;
				
				//if it is not a valid game, then we choose another file and create a new board
				if(!validGame) {
					chooseGame();
					gb = new GameBoard();
					validGame = true;
					//if it is still invalid
					if(!checkGame()) {
						//System.out.println("Did not find valid file");
						um = new UserMessage("Didn't find valid file");
						broadcast(um, null);
						validGame = false;
					}
					else {
						gb.setBoardSize();
						System.out.println("Generating Board...");
						gb.generateBoard(gb.allQuestions.get(0));
					}
					
				}
			}
			//keeps checking until we make a games
			while(!validGame) {
				chooseGame();
				gb = new GameBoard();
				validGame = true;
				if(!checkGame()) {
					//System.out.println("Did not find valid file");
					UserMessage um = new UserMessage("Didn't find valid file");
					broadcast(um, null);
					validGame = false;
				}
				else {
					gb.setBoardSize();
					System.out.println("Generating Board...");
					gb.generateBoard(gb.allQuestions.get(0));
				}
				
			}
			
			lockVec.get(0).lock();
			try {
				conditionVec.get(0).signal();
			}finally {
				lockVec.get(0).unlock();
			}
			
			System.out.println("Game can now Begin");
		
		}catch(IOException ioe) {
			System.out.println("ioe" + ioe.getMessage());
		}finally {
			try {
				if(ss!=null) ss.close();
			}catch (IOException ioe) {
				System.out.println("ioe" + ioe.getMessage());
			}
		}
		
		
		//sends a message to all the players that the game is begenning now
		UserMessage gameBegin = new UserMessage("The Game is now begenning \n");
		for(int i = 0; i < serverThreads.size(); i++) {
			serverThreads.get(i).sendPrivateMessage(gameBegin);
		}
		System.out.println("Sending Game Board Now \n");
		
		
		
	}
	
	//picks a file at random
	public void chooseGame() {
		Random rand = new Random();
		File gamedata = new File("gamedata/");
		File[] contentOfDirectory = gamedata.listFiles();
		int numFiles = contentOfDirectory.length;
		/*
		for(int i = 0; i < contentOfDirectory.length; i++) {
			System.out.format("File Name: %s%n", contentOfDirectory[i].getName());
		}*/
		int fileIndex = rand.nextInt(numFiles);
		File chosenGame = contentOfDirectory[fileIndex];
		System.out.println(chosenGame);
		this.chosenGame = chosenGame;
	}
	
	//checks whether the game file is valid, and enters the questions into the gameboard's array of Questions
	public boolean checkGame() {
		boolean acrossFound = false;
		boolean downFound = false;
		//boolean for whether the current set of words is across or not
		boolean currAcross = false;
		
		try {
			FileReader fr = new FileReader(this.chosenGame);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while(line != null) {
				
				if(line.trim().toLowerCase().contentEquals("across")) {
					if(acrossFound) {
						//System.out.println("Not a valid game file1 ");
						return false;
					}
					acrossFound = true;
					currAcross = true;
					
				}
				else if(line.trim().toLowerCase().contentEquals("down")) {
					if(downFound) {
						//System.out.println("Not a valid game file2 ");
						return false;
					}
					downFound = true;
					currAcross = false;
				}
				else {
					String[] parts = line.split("[|]");

					if(parts.length != 3) {
						//System.out.println("Not a valid game file3 ");
						return false;
					}
					try {
						int points = Integer.parseInt(parts[0]);
						String answer = parts[1];
						if(answer.trim().contains(" ")) {
							//System.out.println("Not a valid game file4 ");
							return false;
						}
						String question = parts[2];
						gb.newQuestion(currAcross, points, answer.toLowerCase(), question);
						//gb.newAnswer(answer);
							
					}
					catch(NumberFormatException nfe){
						System.out.println("nfe" + nfe.getMessage());
						//System.out.println("Not a valid game file5 ");
						return false;
					}catch(NullPointerException npe) {
						System.out.println("npe " + npe.getMessage());
						return false;
					}
				}
				line = br.readLine();
				
			}
			br.close();
			fr.close();
		}catch(FileNotFoundException fnfe) {
			System.out.println("The file " + this.chosenGame + " could not be found.");
			//System.out.println("Not a valid game file6 ");
			return false;
		}catch(IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
			//System.out.println("Not a valid game file7 ");
			return false;
		}
		//System.out.println("Is a valid game file8 ");
		return true;
	}
	
	
	public void broadcast(UserMessage um, ServerThreadGame stg) {
		if(um != null) {
			/////////do some broadcasting///////////////
			System.out.println(um.getMessage());
			for(ServerThreadGame temp: serverThreads) {
				//if it is not the specified thread, then it sends the message to it
				if(temp != stg) {
					temp.sendPrivateMessage(um);
				}
			}
		}
	}

	public void broadcastLocal(UserMessage um, ServerThreadGame stg) {
		if(um != null) {
			/////////do some broadcasting///////////////
			for(ServerThreadGame temp: serverThreads) {
				//if it is not the specified thread, then it sends the message to it
				if(temp != stg) {
					temp.sendPrivateMessage(um);
				}
			}
		}
	}
	
	
	
	public void runGame() {
		
		//number of questions on the board
		int numQuestions = gb.allQuestions.size();
		System.out.println("Num Questions = " + numQuestions);
		//number of questions that have been answered
		int numAnswered = 0;
		
		//vector of questions that have not been answered
		Vector<Question> notAnswered = new Vector<Question>();
		notAnswered = gb.allQuestions;


	}
	public void newGame() {
		
		UserMessage um = new UserMessage("GAME_ENDED");
		broadcastLocal(um,null);
		this.gameEnded = true;
		GameServer gs = new GameServer(3456);
		
		return;
	}
	
	
	public void signalNext() {
		currInd++;
		if(currInd == lockVec.size()) {
			currInd = 0;
		}
		lockVec.get(currInd).lock();
		try {
			conditionVec.get(currInd).signal();
		}finally {
			lockVec.get(currInd).unlock();
		}
	}
	
	
	
	public static void main(String[] args) {
		GameServer gs = new GameServer(3456);
		
		
		
		

	}

}
