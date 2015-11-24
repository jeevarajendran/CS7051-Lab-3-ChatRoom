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
			private int chatRoomNo=1;
		
			List<ChatRoom> chatRoomList = new ArrayList<ChatRoom>();
			List<ClientThread> clientLists = new ArrayList<ClientThread>();
			
			public ServerSoc(ServerSocket serverSocket)
			{
				this.serverSocket = serverSocket;
			}

			public void start()
			{
				try
				{
					while(true)
					{	
						if(pool.getActiveCount()<pool.getMaximumPoolSize())
						{										
							System.out.println("Waiting for a client to get connected \n-------------------------------------");											Socket socket = serverSocket.accept();
							clientId++;
							System.out.println("Connected to a client\n---------------------------------");
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
			
			public void initializeChatRooms()
			{
				for(int i=0;i<5;i++)
				{
					String chatRoomName = "room"+(i+1);
					int chatRoomId = i+1;
					ChatRoom newChatRoom = new ChatRoom(chatRoomName ,chatRoomId);
					chatRoomList.add(newChatRoom);
				}
			}
			public void viewChatRooms()
			{
				ListIterator ilview = chatRoomList.listIterator();
				while(ilview.hasNext())
				{
					ChatRoom cr = (ChatRoom)ilview.next();
					System.out.println(cr.chatRoomName);					
				}
			}
			public void joinChatRoom(String chatRoomName,ClientThread clientThread)
			{
				ListIterator illookup = chatRoomList.listIterator();
				while(illookup.hasNext())
				{
					ChatRoom cr = (ChatRoom)illookup.next();
					System.out.println(cr.chatRoomName);
					if((cr.chatRoomName).equals(chatRoomName))
					{
						System.out.println("Found chat room");
						cr.addClient(clientThread);
						System.out.println("Client Added");
						break;
					}					
				}
				displayTheClients(chatRoomName);
			}
			public void displayTheClients(String chatRoomName)
			{
				ListIterator ildisplay = chatRoomList.listIterator();
				while(ildisplay.hasNext())
				{
					ChatRoom cr = (ChatRoom)ildisplay.next();
					System.out.println(cr.chatRoomName);
					if((cr.chatRoomName).equals(chatRoomName))
					{
						System.out.println("Found chat room for clients display");
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
			
			public void chat(String message, ClientThread clientThread, String chatRoomName)
			{
				ListIterator ilchat = chatRoomList.listIterator();
				while(ilchat.hasNext())
				{
					ChatRoom cr = (ChatRoom)ilchat.next();
					System.out.println(cr.chatRoomName);
					if((cr.chatRoomName).equals(chatRoomName))
					{
						System.out.println("Found chat room for clients chat");
						ListIterator ilClientChat = cr.clientsConnected.listIterator();
						while(ilClientChat.hasNext())
						{
							ClientThread ct = (ClientThread)ilClientChat.next();
							System.out.println("Client :" + ct.clientId);
							//ct.output.println(message);
						}
						break;
					}					
				}	
				System.out.println("Chat with clients done");	
				
			}
	
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
			
				@Override			
				public void run()
				{
					try
					{
						BufferedReader bd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
						while(!kill)
						{
							String temp = "";

							while(bd.ready())
							{
								String temp2 = bd.readLine();
								System.out.println("temp2 : "+ temp2);
								temp = temp + temp2;
							}

							System.out.println("Message from Client : "+ temp +" \n");
							String messagetoclient = null;
							
							if(temp.equals("HELO BASE_TEST"))
							{
								System.out.println("HELo BASE TEST INSIDE");									                   
								messagetoclient = "HELO BASE_TEST";
								messagetoclient = messagetoclient + "\nIP:134.226.58.115\nPort:7777\nStudentID:23456";
								output.println(messagetoclient);
							}
							else if(temp.startsWith("JOIN_CHATROOM")==true)
							{
								System.out.println("JOIN CHATROOM INSIDE");				
								this.clientName = "client1";
								joinChatRoom("room1",this);
								System.out.println("Client Joined :)");
								//serverSoc.lookChatRooms("room1",this);					                   
								
							}
							else if(temp.startsWith("CHAT")==true)
							{
								System.out.println("CHAT INSIDE");	
								
								String message = "CHAT:1\nCLIENT_NAME:client1\nMESSAGE:test\n\n";
								output.println(message);
								serverSoc.chat(message, this, "room1");
							}
							else if(temp.equals("LEAVE_CHATROOM"))
							{
								System.out.println("LEAVE CHATROOM INSIDE");

								kill=true;
								output.println("You are successfully disconnected");
								output.close();
								socket.close();
								serverSoc.killService();
						
							}
							else if(temp.equals("KILL_SERVICE"))
							{
								System.out.println("KILL SERVICE INSIDE");
								
								kill = true;	
								output.println("Killed");
							}
							else
							{
								System.out.println("ELSE INSIDE");
								kill= true;
								messagetoclient = "Got your random message";
								output.println(messagetoclient);		
							}
							System.out.println("done with while loop");
						}
						System.out.println("Last");
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
				public void sendMessage(String messagetoclient,ClientThread clientThread)
				{
					try
					{
					PrintWriter output =  new PrintWriter(clientThread.socket.getOutputStream(),true);
					output.println(messagetoclient);	
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
					
				}
				
				public void addClient(ClientThread clientThread)
				{
					this.clientsConnected.add(clientThread);
					String messagetoclient = "JOINED_CHATROOM: room1\nSERVER_IP:134.226.58.115\nPORT:7777\nROOM_REF:1\nJOIN_ID:1";
								//output.println(messagetoclient);
								//output.println("CHAT:1\nCLIENT_NAME:client1\nMESSAGE:client1 has joined this chatroom.\n\n");
					sendMessage(messagetoclient,clientThread);
					chat(clientThread,"client1 has joined the chatroom.");
				}

				public void chat(ClientThread clientThread, String message)
				{
					ListIterator ilClientChat = clientsConnected.listIterator();
						while(ilClientChat.hasNext())
						{
							String messageToClient = "CHAT: 1\nCLIENT_NAME:client1\nMESSAGE:"+message+"\n";
							ClientThread ct = (ClientThread)ilClientChat.next();
							sendMessage(messageToClient,ct);
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