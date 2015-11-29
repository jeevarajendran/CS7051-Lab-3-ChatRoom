/*
 * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
   * and open the template in the editor.
    */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.*;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

    /**
     *
      * @author jeevarajendran
       */
		public class ServerSoc
		{
			private ServerSocket serverSocket;
			private static final ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
			private int clientId=0;
			private int chatRoomNo=1000;
		
			List<ChatRoom> chatRoomList = new ArrayList<ChatRoom>();
			List<ClientThread> clientLists = new ArrayList<ClientThread>();
			
			//constructor to initialize server socket
			public ServerSoc(ServerSocket serverSocket)
			{
				this.serverSocket = serverSocket;
			}
			
			//method to accept client connection
			public void start()
			{
				try
				{
					while(true)
					{	
						if(pool.getActiveCount()<pool.getMaximumPoolSize())
						{										
							System.out.println("Waiting for a client to get connected \n-------------------------------------");													Socket socket = serverSocket.accept();
							clientId++;
							System.out.println("Connected to a client\n-------------------------------------");
							ClientThread client = new ClientThread(socket,this,clientId);
							clientLists.add(client);
							ServerSoc.pool.execute(client);
						}
						
					}						                       
				}
				catch(Exception e)
				{
					System.out.println(e);
				}	
			}
			
			//method to close server socket and shut down the pool
			public void killService()
			{
				try
				{
					pool.shutdownNow();
					serverSocket.close();
					System.exit(0);
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
			}
			
			//to initialize the chat rooms of server
			public void initializeChatRooms()
			{
				for(int i=0;i<5;i++)
				{
					String chatRoomName = "room"+(i+1);
					int chatRoomId = chatRoomNo+1;
					chatRoomNo++;
					ChatRoom newChatRoom = new ChatRoom(chatRoomName ,chatRoomId);
					chatRoomList.add(newChatRoom);
				}
			}
			
			//to list the chat rooms
			public void viewChatRooms()
			{
				ListIterator ilview = chatRoomList.listIterator();
				while(ilview.hasNext())
				{
					ChatRoom cr = (ChatRoom)ilview.next();
					System.out.println(cr.chatRoomName);
					System.out.println(cr.chatRoomId);					
				}
			}
			
			//method to join the client to a chat room
			public void joinChatRoom(String chatRoomName,ClientThread clientThread)
			{
				ListIterator illookup = chatRoomList.listIterator();
				while(illookup.hasNext())
				{
					ChatRoom cr = (ChatRoom)illookup.next();
					if((cr.chatRoomName).equals(chatRoomName))
					{
						cr.addClient(clientThread);
						break;
					}					
				}
			}
			
			//method for the client to leave a chat room
			public void leaveChatRoom(int chatRoomId,ClientThread clientThread)
			{
				ListIterator illeave = chatRoomList.listIterator();
				while(illeave.hasNext())
				{
					ChatRoom cr = (ChatRoom)illeave.next();
					if(cr.chatRoomId==chatRoomId)
					{
						cr.removeClient(clientThread);
						break;
					}					
				}
			}
			
			//to disconnect and leave from all the chat rooms
			public void leaveAllChatRooms(ClientThread clientThread)
			{
				ListIterator illeave = chatRoomList.listIterator();
				while(illeave.hasNext())
				{
					ChatRoom cr = (ChatRoom)illeave.next();
					ListIterator ilclientsConnected = cr.clientsConnected.listIterator();
					while(ilclientsConnected.hasNext())
					{
						ClientThread ctCompare = (ClientThread)ilclientsConnected.next();
						if(ctCompare.clientName.equals(clientThread.clientName))
						{
							cr.disconnect(clientThread);
							break;
						}
					}					
				}
			}
			
			//method for messaging
			public void chat(int chatRoomId,ClientThread clientThread,String message)
			{
				ListIterator ilchat = chatRoomList.listIterator();
				while(ilchat.hasNext())
				{
					ChatRoom cr = (ChatRoom)ilchat.next();
					if(cr.chatRoomId==chatRoomId)
					{
						cr.chat(clientThread,message);
						break;
					}					
				}
			}
			
			public void displayTheClients(String chatRoomName)
			{
				ListIterator ildisplay = chatRoomList.listIterator();
				while(ildisplay.hasNext())
				{
					ChatRoom cr = (ChatRoom)ildisplay.next();
					if((cr.chatRoomName).equals(chatRoomName))
					{
						ListIterator ilClientDisplay = cr.clientsConnected.listIterator();
						while(ilClientDisplay.hasNext())
						{
							ClientThread ct = (ClientThread)ilClientDisplay.next();
							System.out.println("Client :" + ct.clientId);
						}
						break;
					}					
				}	
				System.out.println("Displayed the clients currently in the chat room");
			}
	
			//client class
			public class ClientThread implements Runnable
			{
				private Socket socket;
				private ServerSoc serverSoc;
				private boolean kill;
				private int clientId;
				private String clientName;
			
				ClientThread(Socket socket,ServerSoc serverSoc,int clientId)
				{
					this.socket=socket;
					this.serverSoc=serverSoc;
					this.kill = false;
					this.clientId=clientId;
				}

				public void joinChatRoom(String chatRoomName, ClientThread clientThread)
				{
					serverSoc.joinChatRoom(chatRoomName,clientThread);	
				}
				
				public void leaveChatRoom(int chatRoomId, ClientThread clientThread)
				{
					serverSoc.leaveChatRoom(chatRoomId,clientThread);	
				}
				
				public void leaveAllChatRooms(ClientThread clientThread)
				{
					serverSoc.leaveAllChatRooms(clientThread);
				}
				
				public void chat(int chatRoomId, ClientThread clientThread, String message)
				{
					serverSoc.chat(chatRoomId, clientThread,message);	
				}
				public void killService()
				{
					serverSoc.killService();	
				}
			
				@Override			
				public void run()
				{
					try
					{
						BufferedReader bd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
						while(!kill)
						{
							List<String> inputStrings= new ArrayList<String>();
							String temp = "";

							while(bd.ready())
							{
								inputStrings.add(bd.readLine());
							}
							ListIterator ilstring = inputStrings.listIterator();
							while(ilstring.hasNext())
							{
								temp=temp+(String)(ilstring.next());
							}

							//conditions to check the incoming message from client and route accordingly
							if(temp.equals("HELO BASE_TEST"))
							{
								String messagetoclient = "HELO BASE_TEST";
								messagetoclient = messagetoclient + "\nIP:134.226.58.160\nPort:7777\nStudentID:23456";
								output.println(messagetoclient);
							}
							else if(temp.startsWith("JOIN_CHATROOM")==true)
							{	
								String chatRoomName = ((String)(inputStrings.get(0))).split(":")[1];
								this.clientName =((String)(inputStrings.get(3))).split(":")[1];
								joinChatRoom(chatRoomName,this);
												                   
							}
							else if(temp.startsWith("CHAT")==true)
							{
								int chatRoomId =Integer.parseInt( (((String)(inputStrings.get(0))).split(":")[1]).trim());
								String message =(((String)(inputStrings.get(3))).split(":")[1].trim());
								chat(chatRoomId,this,message+"\n\n");
							}
							else if(temp.startsWith("LEAVE_CHATROOM")==true)
							{
								int chatRoomId =Integer.parseInt( (((String)(inputStrings.get(0))).split(":")[1]).trim());
								leaveChatRoom(chatRoomId,this);
							}
							else if(temp.startsWith("DISCONNECT")==true)
							{
								leaveAllChatRooms(this);
								socket.close();
							}
							else if(temp.equals("KILL_SERVICE"))
							{

								kill=true;
								socket.close();
								killService();
						
							}
							else
							{
								//do nothing		
							}
						}
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
			}
			
			public class ChatRoom
			{
				private String chatRoomName;
				private int chatRoomId;
				List<ClientThread> clientsConnected = new ArrayList<ClientThread>();

				ChatRoom(String chatRoomName,int chatRoomId)
				{
					this.chatRoomName = chatRoomName;
					this.chatRoomId = chatRoomId;
				}

				//to add a client to the current chat room
				public void addClient(ClientThread clientThread)
				{
					this.clientsConnected.add(clientThread);
					String messagetoclient = "JOINED_CHATROOM:"+this.chatRoomName+"\nSERVER_IP:134.226.58.160\nPORT:7777\nROOM_REF:"+this.chatRoomId+"\nJOIN_ID:"+clientThread.clientId+"\n";
					sendMessage(messagetoclient,clientThread);
					chat(clientThread,clientThread.clientName+" has joined this chatroom.\n\n");
				}
				
				//to remove a client from the current chat room
				public void removeClient(ClientThread clientThread)
				{
					String messagetoclient = "LEFT_CHATROOM:"+this.chatRoomId+"\nJOIN_ID:"+clientThread.clientId+"\n";
					sendMessage(messagetoclient,clientThread);
					chat(clientThread,clientThread.clientName+"  has left this chatroom.\n\n");
					this.clientsConnected.remove(clientThread);
					
				}
				
				//to disconnect from the chat rooms and server
				public void disconnect(ClientThread clientThread)
				{
					chat(clientThread,clientThread.clientName+"  has left this chatroom.\n\n");
					this.clientsConnected.remove(clientThread);
					
				}
				
				//to message
				public void chat(ClientThread clientThread, String message)
				{
					ListIterator ilClientChat = clientsConnected.listIterator();
					String messageToClient2 = "CHAT:"+this.chatRoomId+"\nCLIENT_NAME:"+clientThread.clientName+"\nMESSAGE:"+message;
					if(!ilClientChat.hasNext())
					{
						sendMessage(messageToClient2,clientThread);
					}
					while(ilClientChat.hasNext())
					{
						ClientThread ct = (ClientThread)ilClientChat.next();
						sendMessage(messageToClient2,ct);
					}
								
				}
			
				//method to send a message to client / chat room 
				public void sendMessage(String messagetoclient,ClientThread clientThread)
				{
					try
					{
						PrintWriter output =  new PrintWriter(clientThread.socket.getOutputStream(),true);
						System.out.println("Message to Client / Chat Room : " + messagetoclient);
						output.printf(messagetoclient);	
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
					
				}
				
			}
			
			public static void main(String[] args) throws Exception
			{				   
				ServerSoc serSoc = new ServerSoc(new ServerSocket(7777));
				serSoc.initializeChatRooms();
				serSoc.viewChatRooms();
				serSoc.start();
			}																														
		}