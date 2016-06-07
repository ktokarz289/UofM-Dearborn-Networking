package project2_427;

/*
 * Program 2-2
 * CIS 427
 * Chris Lund, Kris Tokarz
*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

import project2_427.segment;

public class SenderActions extends TimerTask
{
    Timer time = new Timer();
    File file;
    FileInputStream fis;
    BufferedReader br;
    String currLine;
    DatagramSocket clientSocket = new DatagramSocket();
    segment segment = new segment(0,0,0,0,null);
    int sendBase;
    int N = 100;
    int nextseqnum;
    ArrayList<segment> segments = new ArrayList<segment>();
    byte[] receiveData = new byte[1024];					//structure will be for acks
    Socket connectionSocket;
    int toSeq = 0;
    
    public SenderActions() throws IOException
    {
	file = new File("alice.txt");                                           //Open file
        fis = new FileInputStream(file);					//create file stream
        br = new BufferedReader(new InputStreamReader(fis));                    //read file as necessary
        nextseqnum = 1;
        sendBase = 1;								//Initialize to 1
        segment = new segment(1,0,0,0,"");
    }
    
    //readLine, part 2 of preparing to send
    public void readLine() throws IOException
    {
	currLine = br.readLine();						//In case of time out, store message to resend
	segment.setPayload(currLine);						//Convert line to string	
    }
	
    public void prepareSeg() throws IOException
    {   									//increase segnum
		readLine();								//get payload
		segment.setLength(segment.getLength());
        segment.incSeqnum();
        nextseqnum = segment.getSeqnum();
    }	

    public void UDP_send() throws IOException, InterruptedException
    {
	//System.out.println("sendBase= " + sendBase + " " + "nextSeqNum= " + nextseqnum);
    	int temp2 = sendBase + N;
        int drop = (int)Math.ceil(Math.random()*100+1);
        //test++;
        if (drop > 5)
        {
        	//System.out.println( nextseqnum + " " + temp2);
            if(nextseqnum < temp2) 
            {
                prepareSeg();							//get our segment going
                String temp = segment.getPayload();
                segment.setChecksum(segment.calculateChecksum(temp.getBytes()));
                sUDP_send();
                if(sendBase >= nextseqnum)
                {
                    //reset timer
                	if(time == null)
                    {
                        time.schedule(this, 500);
                        toSeq = segment.getSeqnum();
                	}
                }
                //sendBase = nextseqnum;
                nextseqnum += segment.getLength();
            }
            else
            {
                this.timeOut();
            }
            String end = segment.getPayload();
        
            if (end.contains("THE END"))
            {
                System.out.println("file sent. closing program.");
                System.exit(0);
            }
        }
        else 
        {
            this.timeOut();
        }
    }
    
    public void timeOut()throws IOException, InterruptedException
    {
        Thread.sleep(500);
        System.out.println("send fail, retrying...");
        this.sUDP_send();
        sendBase += 50;
    }
    
    public void sUDP_send() throws IOException
    {
        InetAddress IPAddress = InetAddress.getLocalHost();
    	//segment s = new segment(segment.getSeqnum(),0,segment.getLength(),(int)segment.getChecksum(),segment.getPayload());
        byte[] sendData = new byte[1024];									   //structure for send file line
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(segment);
        os.close();
        sendData = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket
            (sendData, sendData.length, IPAddress, 3138);                      //envelope for line
        //clientSocket.setReceiveBufferSize(N);
        //clientSocket.setSendBufferSize(N);
        clientSocket.send(sendPacket);										   //send envelope
    }
    
    public void UDP_rcv() throws IOException
    {
    	int chance;
    	segment rseg = null;
        segment seg = new segment(0,0,0,0,null);
	    DatagramPacket receivePacket = new DatagramPacket
                (receiveData, receiveData.length);
	    clientSocket.receive(receivePacket);
	    byte[] data = receivePacket.getData();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        
        try 
        {
            rseg = (segment) is.readObject();
            is.close();
            if (rseg.getSeqnum() == toSeq){
            	toSeq = 0;
            	time.cancel();
            	time = null;
            }
        } 
        catch (ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
        
        chance = (int) Math.floor(Math.random()*100+1);				//95% of time the data is not corrupt. Subject to change
        if(chance > 5){												//for testing purposes.
        	//System.out.println(rseg.getAcknum());
	        //sendBase = rseg.getAcknum();
	    	if(sendBase < nextseqnum)
	        {
	    		if(time != null){
	            //time.cancel();
	            //time.purge();
	            //time = new Timer();
	            //time.scheduleAtFixedRate(this, 0, 500);
	    		}
	        }
	    	sendBase = rseg.getAcknum();
        } 
    }

    @Override
    public void run() 
    {
	try 
        {
            sUDP_send();
	}
        catch (IOException e) 
        {
            e.printStackTrace();
	}
    }
}
