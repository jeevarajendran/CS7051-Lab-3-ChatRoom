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
							System.out.println("Waiting for a client to get connected  (New prog):\n");																					System.out.println("------------------------------------------\n");
							Socket socket = serverSocket.accept();
							clientId++;
							System.out.println("Connected to a client\n--------------\n");
							ServerSoc.pool.execute(new ClientThread(socket,this,clientId));
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
					ChatRoom newChatRoom = new ChatRoom(name,chatRoomId);
					chatRoomList.add(newChatRoom);
				}
			}
			public class ClientThread implements Runnable
			{
				private Socket socket;
				private ServerSoc serverSoc;
				private boolean kill;
				private int clientId;
			
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
						PrintWriter output = new PrintWriter(socket.getOutputStream(),true);
						while(!kill)
						{
							String temp = bd.readLine();
							System.out.println("Message from Client : "+ temp +" \n");
							String messagetoclient = null;
	
							if(temp.equals("JOIN_CHATROOM"))
							{									                   
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
				ChatRoom(String chatRoomName,int chatRoomId)
				{
					this.chatRoomName = chatRoomName;
					this.chatRoomId = chatRoomId;
				}
			
			}
			
			public static void main(String[] args) throws Exception
			{				   
				ServerSoc serSoc = new ServerSoc(new ServerSocket(7777));
				serSoc.initializeChatRooms();
				serSoc.start();
			}																														
		}

		
