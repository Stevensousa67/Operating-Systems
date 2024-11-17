//ReaderWriter.java synchronized but without any policy between readers & Writers

import java.io.*;

public class ReaderWriter2
{
	public static void main(String args[])throws IOException
	{
	   Database r = new Database();
		Writer p = new Writer(r);
		Reader c = new Reader(r);
		System.out.println("Starting...\n");
		p.start();
		c.start();
		// all threads are running
	   // all sending messages to the same object
		// we block until all complete
		try {
		      p.join(); 
				c.join();
		}
		catch (InterruptedException ie) { }
		System.out.println("All done..");
	}
}
class Writer extends Thread
{
	private Database r;
	
	//constructor
	public Writer(Database r)
	{
      this.r = r;
	}
	//will produce integers from 0 to 9
	public void run()
	{
		for (int i=0; i<10; i++)
		{
			r.setBuf(i);
			System.out.println("Writer set buffer to "+i);
			//sleep for random amount of time
			try
			{
				sleep ((int)(Math.random()*100));
			}
			catch(InterruptedException e){ }
			
		}//for
	}//run
}
class Reader extends Thread
{
	private Database r;
	//constructor
	public Reader(Database r)
	{
      this.r = r;
	}
	//will produce integers from 0 to 9
	public void run()
	{
		int val = r.getBuf();
		System.out.println("Reader retrieved "+val);
		while (val != 9)
		{	
            //sleep for random amount of time
			try
			{
				sleep ((int)(Math.random()*100));
			}
			catch(InterruptedException e){ }
			val = r.getBuf();
	    	System.out.println("Reader retrieved "+val);
		}//while
	}//run
}
class Database
{	private int val=0;
	public synchronized void setBuf(int val)
	{
		this.val=val;
	}
	public synchronized Integer getBuf()
	{ 	
	  		return val;
	}
}
