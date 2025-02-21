/* $Id: Disk.java,v 1.13 2004/03/31 17:36:35 solomon Exp solomon $ */

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.BitSet;

/**
 * A software simulation of a Disk.
 * <p>
 * <b>You may not change this class.</b>
 * <p>
 * This disk is slow and ornery.
 * It contains a number of blocks, all BLOCK_SIZE bytes long.
 * All operations occur on individual blocks.
 * You can't modify any more or any less data at a time.
 * <p>
 * To read or write from the disk, call beginRead() or beginWrite().
 * Each of these functions will start the action and return immediately.
 * When the action has been completed, the Disk calls Kernel.interrupt()
 * to let you know the Disk is ready for more.
 * <p>
 * It may take a while for the disk to seek from one block to another.
 * Seek time is proportional to the difference in block numbers of the
 * blocks.
 * <p>
 * <b>Warning:</b> Don't call beginRead() or beginWrite() while the
 * disk is busy! If you don't treat
 * the Disk gently, the system will crash! (Just like a real machine!)
 * <p>
 * This disk saves its contents in the Unix file DISK between runs.
 * Since the file can be large, you should get in the habit of removing it
 * before logging off.
 *
 * @see Kernel
 */

public class Disk implements Runnable {
    public byte[] freeMap;
    public String[] fileTable;

    /** The maximum length of a file name in bytes. */
    public static final int FILE_NAME_OFFSET = 100;

    /** The size of a disk block in bytes. */
    public static final int BLOCK_SIZE = 512;

    /** Total size of this disk, in blocks. */
    public final int DISK_SIZE;

    /////////////////////////////////////////// Transient internal state

    /** Current location of the read/write head */
    protected int currentBlock = 0;

    /** The data stored on the disk */
    protected byte data[];

    /** An indication of whether an I/O operation is currently in progress. */
    protected boolean busy;

    /**
     * An indication whether the current I/O operation is a write operation.
     * Only meaningful if busy == true.
     */
    private boolean isWriting;

    /**
     * The block number to be read/written by the current operation.
     * Only meaningful if busy == true.
     */
    protected int targetBlock;

    /**
     * Memory buffer to/from which current I/O operation is transferring.
     * Only meaningful if busy == true.
     */
    private byte buffer[];

    /**
     * A flag set by beginRead or beginWrite to indicate that a request
     * has been submitted.
     */
    private boolean requestQueued = false;

    /** A count of read operations performed, for statistics. */
    protected int readCount;

    /** A count of write operations performed, for statistics. */
    protected int writeCount;

    /////////////////////////////////////////// Inner classes

    /**
     * The exception thrown when an illegal operation is attempted on the
     * disk.
     */
    static protected class DiskException extends RuntimeException {
        public DiskException(String s) {
            super("*** YOU CRASHED THE DISK: " + s);
        }
    }

    /////////////////////////////////////////// Constructors

    /**
     * Creates a new Disk.
     * If a Unix file named DISK exists in the local Unix directory, the
     * simulated disk contents are initialized from the Unix file.
     * It is an error if the DISK file exists but its size does not match
     * "size".
     * If there is no DISK file, the first block of the simulated disk is
     * cleared to nulls and the rest is filled with random junk.
     * 
     * @param size the total size of this disk, in blocks.
     */
    public Disk(int size) {
        File diskName = new File("DISK");
        if (diskName.exists()) {
            if (diskName.length() != size * BLOCK_SIZE) {
                throw new DiskException(
                        "File DISK exists but is the wrong size");
            }
        }
        this.DISK_SIZE = size;
        if (size < 1) {
            throw new DiskException("A disk must have at least one block!");
        }
        int mapSize = new BigDecimal(DISK_SIZE / 8.0).setScale(0, RoundingMode.UP).intValue();
        freeMap = new byte[mapSize];
        fileTable = new String[size];
        // NOTE: the "new" operator always clears the result object to nulls
        data = new byte[DISK_SIZE * BLOCK_SIZE];
        try {
            if (diskName.exists()) {
                try (FileInputStream is = new FileInputStream("DISK")) {
                    is.read(data);
                    System.out.println("Restored " + data.length + " bytes from file DISK");
                }
            } else {
                System.out.println("Creating new disk");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    /////////////////////////////////////////// Methods

    /**
     * Saves the contents of this Disk.
     * The contents of this disk will be forced out to a file named
     * DISK so that they can be restored on the next run of this program.
     * This file could be quite big, so delete it before you log out.
     * Also prints some statistics on disk operations.
     */
    public void flush() {
        try {
            System.out.println("Saving contents to DISK file...");
            try (FileOutputStream os = new FileOutputStream("DISK")) {
                os.write(data);
            }
            System.out.println(readCount + " read operations and "
                    + writeCount + " write operations performed");
        } catch (IOException e) {
            System.exit(1);
        }
    }

    /**
     * Sleeps for a while to simulate the delay in seeking and transferring
     * data.
     * 
     * @param targetBlock the block number to which we have to seek.
     */
    protected void delay(int targetBlock) {
        int sleepTime = 10 + Math.abs(targetBlock - currentBlock) / 5;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    } // delay

    /**
     * Starts a new read operation.
     * 
     * @param blockNumber The block number to read from.
     * @param buffer      A data area to hold the data read. This array must be
     *                    allocated by the caller and have length of at least
     *                    BLOCK_SIZE. If it is larger, only the first BLOCK_SIZE
     *                    bytes of the array will be modified.
     */
    public synchronized void beginRead(int blockNumber, byte buffer[]) {
        if (blockNumber < 0
                || blockNumber >= DISK_SIZE
                || buffer == null
                || buffer.length < BLOCK_SIZE) {
            throw new DiskException("Illegal disk read request: "
                    + " block number " + blockNumber
                    + " buffer " + Arrays.toString(buffer));
        }

        if (busy) {
            throw new DiskException("Disk read attempted "
                    + " while the disk was still busy.");
        }

        isWriting = false;
        this.buffer = buffer;
        targetBlock = blockNumber;
        requestQueued = true;

        notify();
    } // beginRead

    /**
     * Starts a new write operation.
     * 
     * @param blockNumber The block number to write to.
     * @param buffer      A data area containing the data to be written. This array
     *                    must be allocated by the caller and have length of at
     *                    least
     *                    BLOCK_SIZE. If it is larger, only the first BLOCK_SIZE
     *                    bytes of the array will be sent to the disk.
     */
    public synchronized void beginWrite(int blockNumber, byte buffer[]) {
        if (blockNumber < 0
                || blockNumber >= DISK_SIZE
                || buffer == null
                || buffer.length < BLOCK_SIZE) {
            throw new DiskException("Illegal disk write request: "
                    + " block number " + blockNumber
                    + " buffer " + Arrays.toString(buffer));
        }

        if (busy) {
            throw new DiskException("Disk write attempted "
                    + " while the disk was still busy.");
        }

        isWriting = true;
        this.buffer = buffer;
        targetBlock = blockNumber;
        requestQueued = true;

        notify();
    } // beginWrite

    /** Waits for a call to beginRead or beginWrite. */
    protected synchronized void waitForRequest() {
        while (!requestQueued) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        requestQueued = false;
        busy = true;
    } // waitForRequest

    /** Indicates to the CPU that the current operation has completed. */
    protected void finishOperation() {
        synchronized (this) {
            busy = false;
            currentBlock = targetBlock;
        }
        // NOTE: The interrupt needs to be outside the critical section
        // to avoid a race condition: The interrupt handler in the kernel
        // may wish to call beginRead or beginWrite (perhaps indirectly),
        // which would deadlock if the interrupt handler were invoked with
        // the disk mutex locked.
        Kernel.interrupt(Kernel.INTERRUPT_DISK,
                0, 0, null, null, null);
    } // finishOperation

    /**
     * This method simulates the internal microprocessor of the disk
     * controler. It repeatedly waits for a start signal, does an I/O
     * operation, and sends an interrupt to the CPU.
     * This method should <em>not</em> be called directly.
     */
    public void run() {
        for (;;) {
            waitForRequest();

            // Pause to do the operation
            delay(targetBlock);

            // Move the data.
            if (isWriting) {
                System.arraycopy(
                        buffer, 0,
                        data, targetBlock * BLOCK_SIZE,
                        BLOCK_SIZE);
                writeCount++;
            } else {
                System.arraycopy(
                        data, targetBlock * BLOCK_SIZE,
                        buffer, 0,
                        BLOCK_SIZE);
                readCount++;
            }

            // Signal completion
            finishOperation();
        }
    }

    /**
     * Creates a new disk when the "DISK" file cannot be found.
     */
    public void createNewDisk(){
        int bitsSwitched = 0;
        for (int i = 0; i < freeMap.length; i++) {
            byte myByte = freeMap[i];
            BitSet bits = Utilities.fromByte(myByte);
            for (int j = 0; j < 8; j++) {
                bits.set(j, true);
                if (++bitsSwitched >= freeMap.length) {
                    break;
                }
            }
            freeMap[i] = Utilities.toByteArray(bits)[bits.length() / 8];
            if (bitsSwitched >= freeMap.length) {
                break;
            }   
        }
        System.arraycopy(freeMap, 0, data, 0, freeMap.length);
    }

    /**
     * Loads the disk when the "DISK" file exists.
     */
    public void loadDisk() {
        System.arraycopy(data, 0, freeMap, 0, freeMap.length);
        for (int i = 0; i < DISK_SIZE; i++) {
            if (((freeMap[i / 8] >> (i % 8)) & 1) == 1) {
                int byteIndex = i * BLOCK_SIZE;
                byte[] fileName = new byte[FILE_NAME_OFFSET];
                for (int j = 0; j < fileName.length; j++) {
                    fileName[j] = data[byteIndex];
                    byteIndex++;
                }
                fileTable[i] = new String(fileName).trim();
            }
        }
    }

    /**
     * Returns the block number for the passed file name.
     * 
     * @param fileName
     * @return int
     */
    public int getFileBlock(String fileName) {
        int index = -1;
        for (int i = 0; i < fileTable.length; i++) {
            String systemFile = fileTable[i];
            if (fileName.equals(systemFile)) {
                index = i;
            }
            if (index != -1) {
                break;
            }
        }
        return index;
    }

    /**
     * Returns the next available block number on the fisk.
     * 
     * @return int
     */
    public int getNextBlockIndex() {
        int index = -1;
        for (int i = 0; i < DISK_SIZE; i++) {
            if (((freeMap[i / 8] >> (i % 8)) & 1) == 0) {
                index = i;
            }
            if (index != -1) {
                break;
            }
        }
        return index;
    }
    
    /**
     * Updates the freeMap with the newly allocated block index.
     * 
     * @param blockIndex
     */
    public void setFreeMap(int blockIndex, boolean used) {
        byte usedByte = freeMap[blockIndex / 8];
        BitSet bits = Utilities.fromByte(usedByte);
        int usedIndex = blockIndex % 8;
        bits.set(usedIndex, used);
        freeMap[blockIndex / 8] = Utilities.toByteArray(bits)[0];
        System.arraycopy(freeMap, 0, data, 0, freeMap.length);
    }
} // Disk
