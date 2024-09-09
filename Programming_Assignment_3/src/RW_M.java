	
import java.io.*;
import java.util.*;

public class RW_M
{
	public static void main(String args[])throws IOException
	{
		System.out.println("\nReader/Writer Program has received the arguments: " + args[0] + " and " + args[1] + ".\n");
		
		int number_of_readers = Integer.parseInt(args[0]);
		int number_of_writers = Integer.parseInt(args[1]);
		
		System.out.println("\nProgram will create " + number_of_readers + " Reader(s) and " + number_of_writers + " Writer(s).\n");
		
		Database thread_manager = new Database();

			// Create the Writers
			
				
			// Create the Readers

				
		System.out.println("\n\n   Begin Read/Write Calculations...\n\n");
		
			// Start the Readers
		
				
			// Start the Writers
		
		
		try {
		
				// Join the Writers
		
			
				// Join the Readers
		
		}
		
		catch (InterruptedException ie) { }

		System.out.println("\n   Reader/Writer Complete\n");
	}
}

class Writer extends Thread
{
	//create a database object
   	
  // Public Constructor Method
	public Writer(.......) 
	{
		
	}
	
		// Public Run Method
	public void run()
	{
	}
}

class Reader extends Thread
{
   //create a database object 
   
   // Public Constructor Method
	public Reader(.........)
	{
		
    }
	
		// Public Run Method
	public void run()
	{
	
	}
}

class Database
{
   //create a constructor 
   
   // Handle Data Structures
	private int database_val = 0;
	Monitor thread_monitor = new Monitor();			

		// Buffer Setter
	public void setBuf(int database_val)
	{
		
	}
	
		// Buffer Getter
	public Integer getBuf()
	{
		
		
		return database_val;
	}
}

class Monitor
{
	// Define Variables
	private boolean writing = false;
	private int readers_reading = 0;
	
	private int waiting_readers = 0;
	private int waiting_writers = 0;
	// add more variables as you need
   	
	synchronized void startWrite()
	{
	
					   
	 }
	 
	 synchronized void stopWrite()
	 {
		
	 }
	 
	 synchronized void startRead() 
	 {
	 
				    
	  }

	  synchronized void stopRead()
	  {
		
	  }		
} // Monitor
