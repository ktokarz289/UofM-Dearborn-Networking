package project2_427;

/*
 * Program 2-2
 * CIS 427
 * Chris Lund, Kris Tokarz
*/

import java.io.*;

public class segment implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int seqnum;
    private int acknum;
    private int length;
    private long checksum;
    private String payload;
    
    public segment(int seq, int ackno, int leng, int check, String load)
    {
        this.acknum = ackno;
        this.seqnum = seq;
        this.checksum = check;
        this.payload = load;
        this.length = leng;
    }

    public void incSeqnum()
    {
        this.seqnum += this.getLength();
    }
    
    public void incAcknum()
    {
        this.acknum += this.getLength();
    }
    public void setSeqnum(int in)
    {
        this.seqnum = in;
    }
    
    public void setChecksum(long in)
    {
        if (this.payload != null)
        {
            this.checksum = in;
        }
        else
        {
            this.checksum = 0;
        }
    }
    
    public void setPayload(String pl)
    {
	this.payload = pl;
    }
    
    public void setAcknum(int in)
    {
        this.acknum = in;
    }
    
    public void setLength(int in)
    {
        this.length = in;
    }
    public int getSeqnum()
    {
        return this.seqnum;
    }
    
    public int getAcknum()
    {
        return this.acknum;
    }
    
    public int getLength()
    {
        if (this.payload != null)
        {
            return this.payload.length();
        }
        else
        {
            return 1;
        }
    }
    
    public long getChecksum()
    {
        return this.checksum;
    }
    
    public String getPayload()
    {
        return this.payload;
    }
    
    public long calculateChecksum(byte[] buf) 
    {
        int length = buf.length;
        int i = 0;
        long sum = 0;
        long data;
        if(buf != null)
        {
        // Handle all pairs
        while (length > 1) 
        {
          // Corrected to include @Andy's edits and various comments on Stack Overflow
	    data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
	    sum += data;
	    // 1's complement carry bit correction in 16-bits (detecting sign extension)
	    if ((sum & 0xFFFF0000) > 0) 
            {
                sum = sum & 0xFFFF;
	        sum += 1;
	    }

	    i += 2;
	    length -= 2;
	}

	// Handle remaining byte in odd length buffers
	if (length > 0) 
        {
              // Corrected to include @Andy's edits and various comments on Stack Overflow
            sum += (buf[i] << 8 & 0xFF00);
              // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) 
            {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
	sum = ~sum;
	sum = sum & 0xFFFF;
	return sum;
        }
        else return 0;
    }
}
