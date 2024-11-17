import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class RW_M {
	public static void main(String[] args) {
		// Parse command-line arguments
		int numberOfReaders = Integer.parseInt(args[0]);
		int numberOfWriters = Integer.parseInt(args[1]);
		int numAccesses = Integer.parseInt(args[2]);

		System.out.println("# of readers is :" + numberOfReaders);
		System.out.println("# of writers is :" + numberOfWriters);
		System.out.println("Reader/Writer is starting...");

		Database database = new Database();

		// Create and start writer threads
		Writer[] writers = new Writer[numberOfWriters];
		for (int i = 0; i < numberOfWriters; i++) {
			writers[i] = new Writer((char) ('F' + i), database, numAccesses);
			writers[i].start();
		}

		// Create and start reader threads
		Reader[] readers = new Reader[numberOfReaders];
		for (int i = 0; i < numberOfReaders; i++) {
			readers[i] = new Reader((char) ('A' + i), database, numAccesses);
			readers[i].start();
		}

		// Wait for all threads to finish
		try {
			for (Writer writer : writers) {
				writer.join();
			}
			for (Reader reader : readers) {
				reader.join();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		System.out.println("Reader/Writer Complete");
	}
}

class Writer extends Thread {
	private final char name;
	private final Database db;
	private final int accesses;

	public Writer(char name, Database db, int accesses) {
		this.name = name;
		this.db = db;
		this.accesses = accesses;
	}

	@Override
	public void run() {
		Random random = new Random();
		for (int i = 0; i < accesses; i++) {
			try {
				db.startWrite();
				int value = random.nextInt(10);
				db.setBuf(value);
				System.out.println("Writer " + name + " set buffer to " + value);
				db.stopWrite();
				Thread.sleep(random.nextInt(1001));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}

class Reader extends Thread {
	private final char name;
	private final Database db;
	private final int accesses;

	public Reader(char name, Database db, int accesses) {
		this.name = name;
		this.db = db;
		this.accesses = accesses;
	}

	@Override
	public void run() {
		Random random = new Random();
		for (int i = 0; i < accesses; i++) {
			try {
				db.startRead();
				int value = db.getBuf();
				System.out.println("Reader " + name + " retrieved " + value);
				db.stopRead();
				Thread.sleep(random.nextInt(1001));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}

class Database {
	private int buf = 0;
	private final Monitor monitor = new Monitor();

	public void setBuf(int value) {
		this.buf = value;
	}

	public int getBuf() {
		return buf;
	}

	public void startWrite() {
		monitor.startWrite();
	}

	public void stopWrite() {
		monitor.stopWrite();
	}

	public void startRead() {
		monitor.startRead();
	}

	public void stopRead() {
		monitor.stopRead();
	}
}

class Monitor {
	private final Semaphore rwMutex = new Semaphore(1);
	private int readCount = 0;
	private final Semaphore mutex = new Semaphore(1);
	private AtomicInteger waitingWriters = new AtomicInteger(0);
	// No longer using readersCanProceed; logic changed significantly.

	public void startWrite() {
		try {
			waitingWriters.incrementAndGet(); // Increment before acquiring lock
			rwMutex.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void stopWrite() {
		if (waitingWriters.decrementAndGet() == 0) {
			rwMutex.release(); // Only release if no writers are waiting
		} else {
			rwMutex.release(); // Release lock for other writers

		}

	}

	public void startRead() {
		try {

			mutex.acquire();
			while (waitingWriters.get() > 0) { // readers wait here if there's a writer in queue.
				mutex.release();
				Thread.yield(); // to allow writers to actually acquire the rw_mutex after it has been released.
				mutex.acquire();
			}

			readCount++;
			if (readCount == 1) {
				rwMutex.acquire(); // the first reader gets rwMutex

			}

			mutex.release();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void stopRead() {
		try {
			mutex.acquire();
			readCount--;
			if (readCount == 0) {
				rwMutex.release(); // last reader releases the lock so that a writer can access it.
			}
			mutex.release();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}