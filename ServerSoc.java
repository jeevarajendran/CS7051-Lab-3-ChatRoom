
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
			private static final ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(5);
			private int clientId=0;
			int chatRoomNo=1;
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
							System.out.println("Waiting for a client to get connected  (New prog):\n");																		System.out.println("------------------------------------------\n");
							Socket socket = serverSocket.accept();
							clientId++;
							System.out.println("Connected to a client\n--------------\n");
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
					String chatRoomName = "chatRoom"+(i+1);
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
			public void lookChatRooms(String chatRoomName,ClientThread clientThread)
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
							ct.output.println(message);
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
				private PrintWriter output = null;
			
				ClientThread(Socket socket,ServerSoc serverSoc,int clientId)
				{
					this.socket=socket;
					this.serverSoc=serverSoc;
					this.kill = false;
					this.clientId=clientId;
				}
			
				@Override			
				public void run()
				{
					try
					{
						BufferedReader bd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						output = new PrintWriter(socket.getOutputStream(),true);
						while(!kill)
						{
							String temp = bd.readLine();
							System.out.println("Message from Client : "+ temp +" \n");
							String messagetoclient = null;
	
							if(temp.equals("JOIN_CHATROOM"))
							{				
								serverSoc.lookChatRooms("chatRoom1",this);					                   
								messagetoclient = "JOINED";
								messagetoclient = messagetoclient + "\nclientId:"+clientId+"\nChatRoomNo:"+chatRoomNo;
								output.println(messagetoclient);
							}
							else if(temp.equals("LEAVE_CHATROOM"))
							{

								kill=true;
								output.println("You are successfully disconnected");
								output.close();
								socket.close();
								serverSoc.killService();
						
							}
							else if(temp.equals("CHAT"))
							{
								String message = "Hi How are you?";
								serverSoc.chat(message, this, "chatRoom1");	
							}
							else
							{
								messagetoclient = "Got your random message";
								output.println(messagetoclient);	
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
				
				public void addClient(ClientThread clientThread)
				{
					this.clientsConnected.add(clientThread);
				}

				public void chat()
				{
					
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
