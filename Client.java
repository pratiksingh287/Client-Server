/*
 * Pratik Singh
 * 1001670417
 */




import java.net.*;
import java.io.*;
import java.util.*;


public class Client  {

	static int count =0;
	Random rand = new Random();
	int addc = rand.nextInt(9)+1;
	private ObjectInputStream sInput;		
	private ObjectOutputStream sOutput;		
	private Socket socket;
	private Server s;
	private ClientGUI cg;               // display client message
	private String server, username;    // client port number for connection
	private int port;
	
	Client(String server, int port, String username) {
		this(server, port, username, null);
	}
	
	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;       // server name
		this.port = port;           // port number for connection
		this.username = username;   // user name of the client
		this.cg = cg;
	}
	
	public boolean start() {
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);   // if connection rejected
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
            @Override
            public void run() { 
                	count = count + addc;
                	String msg = Integer.toString(count);
                	display1(msg);
                
            }
        };
        
         timer.schedule(task, 0, 1000);
         
         TimerTask task2 = new TimerTask() {
             @Override
             public void run() { 
            	String msg = Integer.toString(count);
             	sendMessage2(msg);	// to check other client present		
    			
             }
         };
         
         Timer timer2 = new Timer(); 
         timer2.schedule(task2, 0, 1000);
		 
         try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		new ListenFromServer().start();
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	private void display(String msg) {
		if(cg == null)
			System.out.println(msg);     
		else
			cg.append(msg + "\n");	
	}
	private void display1(String msg) {
		if(cg == null)
			System.out.println(msg);     
		else
			cg.append1(msg);	
	}
	
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}
	void sendMessage2(String msg) {
			// broadcast message
			s.broadcast(msg);
		
	}
	

	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
		try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
		if(cg != null)
			cg.connectionFailed();
			
	}
	
	public static void main(String[] args) {
		int portNumber = 2100;         // default port number
		String serverAddress = "localhost";   // default host  name
		String userName = "Anonymous";        // default user name

		switch(args.length) {
			case 3:
				serverAddress = args[2];
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");  // if port number doen't matches that of server
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			case 1: 
				userName = args[0];
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		Client client = new Client(serverAddress, portNumber, userName);
		if(!client.start())
			return;
		Scanner scan = new Scanner(System.in);
		
		    while(true) {
			System.out.print("> ");
			String msg = scan.nextLine();
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));  // to logout of the app
			break;
			}
			else if(msg.equalsIgnoreCase("WHOISIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));	// to check other client present		
			}
			else if(msg.equalsIgnoreCase("UPDATE")) {
				client.sendMessage(new ChatMessage(ChatMessage.UPDATE, msg));			
			}
			else {				
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));   
			}
		}
		
		client.disconnect();	
	}

	
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					if(cg == null) {
						System.out.println(msg);  // message displayed
						System.out.print("> ");
					}
					else {
						cg.append(msg);
					}
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);  // server stops the connection
					if(cg != null) 
						cg.connectionFailed();
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}
