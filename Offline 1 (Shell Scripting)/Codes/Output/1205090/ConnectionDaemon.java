/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Abdullah Al Maruf
 */
package dlloffline;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

class ConnectionDaemon
{
	public static double SWITCH_PACKET_MISSING_RATE;// = 0.01;
	public static double ERROR_RATE;// = 0.01;

	public static double getDropRate(){return SWITCH_PACKET_MISSING_RATE;}
	public static double getErrorRate(){return SWITCH_PACKET_MISSING_RATE;}

	public static void main(String args[]) throws Exception
	{
		double dropRate=0;
		double errorRate=0;
		int argCount=args.length;

		if (argCount>0)dropRate=Double.parseDouble(args[0]);
		if (argCount>1)errorRate=Double.parseDouble(args[1]);

		ConnectionDaemon.SWITCH_PACKET_MISSING_RATE=dropRate;
		ConnectionDaemon.ERROR_RATE=errorRate;

		ServerSocket welcomeSocket= new ServerSocket(9009);
		Socket connectionSocket[]=new Socket[2];
		DataInputStream[] inFromClient=new DataInputStream[2];
		DataOutputStream outToClient[]=new DataOutputStream[2];
		//while (true)
		for (int i=0;i<2;i++)
		{
			connectionSocket[i]=welcomeSocket.accept();
			inFromClient[i]=new DataInputStream(connectionSocket[i].getInputStream());
			outToClient[i]=new DataOutputStream(connectionSocket[i].getOutputStream());
		}
		SThread A2B=new SThread("A","B",inFromClient[0],outToClient[1]);
        SThread B2A=new SThread("B","A",inFromClient[1],outToClient[0]);
        A2B.join();
        B2A.join();
        //close the sockets here
	}
}
//===========================================================
class SThread extends Thread{
	String name;
	String otherName;
	DataInputStream inFromClient;
	DataOutputStream outToClient;
	String clientSentence;
	public SThread(String n, String o, DataInputStream in, DataOutputStream out){
		name=n;
		otherName=o;
		inFromClient=in;
		outToClient=out;
		start();
	}
	public void run(){
		try{
			while (true){

				byte[] temp=SimPhy.readDeStuffed(inFromClient);
				Frame f=new Frame(temp);
				System.out.println("\tFrame Received from "+name+": "+f.getString()+"\n");
				//System.out.println("<"+uniqueCurrentTimeMS()+">"+"Frame Received from "+name+": "+f.getString()+"\n");

				//add error introducing code here
				//Drop Packet Randomly
				double dpp = Math.random();
				//System.out.println("Dpp: "+dpp+" Drop Rate: "+ ConnectionDaemon.getDropRate());
				if (dpp < ConnectionDaemon.getDropRate()) {
					System.out.println("\tDropping Frame:  "+f.getString()+"\n");
					continue;
				}

				//add random error before transmitting
				double fep = Math.random();
				if (fep < ConnectionDaemon.getErrorRate()) {
					//int changeByteAt=Math.random().nextInt(temp.length); //change a random byte
					int changeByteAt=temp.length-1; //toggle the checksum
					temp[changeByteAt]=(byte)(temp[changeByteAt]^(byte)255); //set	to -1. Or may use XOR to toggle
					System.out.println("Error added to Frame \n");
					//addError(f);
				}

				SimPhy.writeStuffed(outToClient,temp);
				System.out.println("Frame Sent to "+otherName+": "+new Frame(temp).getString()+"\n");
				//System.out.println("<"+uniqueCurrentTimeMS()+">"+"Frame Sent to "+otherName+": "+new Frame(temp).getString()+"\n");
			}
		}
		catch(Exception e){
		}
	}
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private static final AtomicLong LAST_TIME_MS = new AtomicLong();
	public static long uniqueCurrentTimeMS() {
		long now = System.currentTimeMillis();
		while(true) {
			long lastTime = LAST_TIME_MS.get();
			if (lastTime >= now)
				now = lastTime+1;
			if (LAST_TIME_MS.compareAndSet(lastTime, now))
				return now;
		}
	}
}
//===========================================================
