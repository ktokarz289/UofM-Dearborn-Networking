package project2_427;

/*
 * Program 2-2
 * CIS 427
 * Chris Lund, Kris Tokarz
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import project2_427.SenderActions;
import project2_427.segment;

public class Sender 
{	
    public static void main(String argv[]) throws Exception
    {
	SenderActions actions = new SenderActions();                            //We want to send while there is file to read
	while(true)
	{
            actions.UDP_send();  						//Constantly send if possible
            actions.UDP_rcv();  						//Constantly receive if possible
        }
    }  
}
