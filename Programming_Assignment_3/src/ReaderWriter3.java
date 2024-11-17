//ReaderWriter.java :writer starts first

import java.io.*;

public class ReaderWriter3
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
	private boolean readable = false;
	
	public synchronized void setBuf(int val)
	{
		while (readable)
		{
			try
			{
				wait();
			}
			catch(InterruptedException e){ }
		}//while
		this.val=val;
		readable = true;
		notify();
	}
	public synchronized Integer getBuf()
	{
		while(!readable)
        {
			try
			{
				wait();
			}
			catch(InterruptedException e){ }
		}//while
		
		readable = false;
		int x = val;
		notify();
		return x;
	}
}
