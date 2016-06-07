package project2_427;

/*
 * Program 2-2
 * CIS 427
 * Chris Lund, Kris Tokarz
*/

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import project2_427.segment;

public class Receiver
{
    static int SERVER_PORT = 3138;
    static segment segment = new segment(0,0,0,0,null);
    static int N = 50;
    
    public static void main(String[] args) throws Exception
    {
        DatagramSocket receiveSocket = new DatagramSocket(SERVER_PORT);
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getLocalHost();
        byte[] ACKData = new byte[1024];
        byte[] receiveData = new byte[1024];
        Boolean kill = false, inc;
        int ack = 1, lastSeqNum = 0;
        long check = 0, receiveCheck = 0;
        String ACK = null;
        int numRuns = 0;
        ArrayList<segment> buffer = new ArrayList<segment>();
        segment tempSeg;
        long tStart = System.currentTimeMillis();
        
        while (true)
        {
            int drop = (int)Math.ceil(Math.random()*100+1);
            inc = false;
            DatagramPacket packet = new DatagramPacket
                (receiveData, receiveData.length);
            receiveSocket.receive(packet);
            //this should change things
            byte[] data = packet.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            numRuns ++;
            if (drop > 5)
            {
                try 
                {
                    segment = (segment) is.readObject();
                    is.close();
                    if (lastSeqNum != segment.getSeqnum()){
                    	ack = segment.getSeqnum();
                    }
                } 
                catch (ClassNotFoundException e) 
                {
                    e.printStackTrace();
                }
                //System.out.println(lastSeqNum + " " + segment.getSeqnum() + " " + ack);
                if (check == receiveCheck && lastSeqNum != segment.getSeqnum() && ack == segment.getSeqnum())  //&& ack == segment.getSeqnum()
                {                                                                   // comparing the sent checksum to the checksum of the data.
                	System.out.println(segment.getPayload());
                	for(int i = 0; i < buffer.size(); i++){							//display all buffered segments contents
                		System.out.println(buffer.get(i).getPayload());
                	}
                	buffer.clear();
                    inc = true;
                }
                else if (ack < segment.getSeqnum() && lastSeqNum != segment.getSeqnum()){	  //acknum and seqnum are out of sync, buffer it
                  	buffer.add(segment);         
                }
            }
            // converting the ack to from int to string to bytes.
            IPAddress = packet.getAddress();
            int port = packet.getPort();
            segment.setAcknum(ack);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(segment);
            
            ACKData = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket
                    (ACKData, ACKData.length, IPAddress, port);                 // sending ack.
            clientSocket.send(sendPacket);
            os.close();
            
            if (inc)
            {
                //segment.incAcknum();                                            // increments the ack and stores the last sequence received. 
                //ack += segment.getLength();
                lastSeqNum = segment.getSeqnum();
            }
            if (drop > 5)
            {
	            String end = segment.getPayload();
	            
	            if (end.contains("THE END"))
	            {
	                System.out.println("file received. Exiting program.");
	                long tEnd = System.currentTimeMillis();
	                long tDelta = tEnd - tStart;
	                double elapsedSeconds = tDelta / 1000.0;
	                System.out.println("Time elapsed: " + elapsedSeconds);
	                receiveSocket.close();
	                System.exit(0);
	            }
            }
          //}
        }
        
    }
}
