package nyilkhan_CSCI201_Assignment4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.Scanner;

public class UserClient extends Thread{

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private int numPlayers;
	public boolean gameEnded = false;
	
	
	
	public UserClient() {
		Scanner scan = new Scanner(System.in);	
		System.out.println("Welcome to 201 Crossword!");
		Socket s = null;
		boolean connectionMade = false;
		while(connectionMade == false) {
			System.out.println("Enter the server hostname \n");
			String hostname = scan.nextLine();
			System.out.println("Enter the server port \n");
			String port = scan.nextLine();
			
			
			try {
				s = new Socket(hostname, Integer.parseInt(port));
				ois = new ObjectInputStream(s.getInputStream());
				oos = new ObjectOutputStream(s.getOutputStream());
				
				connectionMade = true;
				this.start();
				//sends out what client wants to send
				while(true) {
					String line = scan.nextLine();
					UserMessage um = new UserMessage(line);
					oos.writeObject(um);
					oos.flush();
					if(gameEnded) {
						System.out.println("GAME HAS ENDED FOR USER");
						s.close();
						System.exit(0);
						return;
					}
				}
	
			}catch(SocketException se) {
				//checks if the connection was made, trying to loop again if it was not
				System.out.println("Socket Exception " + se.getMessage());
				connectionMade = false;
				
			}catch(IOException ioe){
				System.out.println("ioe" + ioe.getMessage());
				connectionMade = false;
			}
		}
		scan.close();
	}
	
	public void run() {
		try {
			while(true) {
				UserMessage um = (UserMessage)ois.readObject();
				System.out.println(um.getMessage());
				if(um.getMessage().contains("GAME_ENDED")) {
					gameEnded = true;
				}
				if(gameEnded) {
					//System.out.println("GAME HAS ENDED FOR USER");
					System.exit(MAX_PRIORITY);
					return;
				}
			}
		}catch(IOException ioe) {
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	
	public static void main(String[] args) {
		
		UserClient uc = new UserClient();

	}

}
