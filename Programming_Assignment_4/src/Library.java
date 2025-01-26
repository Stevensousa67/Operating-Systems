/* $Id: Library.p4.java,v 1.12 2004/03/31 17:12:22 solomon Exp solomon $ */

/** Convenience calls for using the Kernel.
 * Each function in this class makes a system call.  Sometimes, the arguments
 * are manipulated to make their user representation more convenient.
 * Note that this class contains only static methods.
 * All methods return integers.  Negative return values are error codes.
 * Some methods return positive values; others simply return 0 to mean "ok".
 *
 * @see Kernel
 */
public class Library {
    /** This private constructor ensures that no instances of Library are
     * ever created.
     */
    private Library() {}

    /** A table of error messages corresponding to Kernel error return codes.
     * This table should be indexed by the negative of rc, where
     * <pre>
     *          rc = Kernel.interrupt(Kernel.INTERRUPT_USER, ... )
     * </pre>
     * and rc is less than 0.
     */
    public static final String[] errorMessage = {
        "OK",                           // 0
        "Invalid argument",             // ERROR_BAD_ARGUMENT = -1
        "No such class",                // ERROR_NO_CLASS = -2
        "Class has no main method",     // ERROR_NO_MAIN = -3
        "Command aborted",              // ERROR_BAD_COMMAND = -4
        "Argument out of range",        // ERROR_OUT_OF_RANGE = -5
        "End of file on console input", // ERROR_END_OF_FILE = -6
        "I/O error on console input",   // ERROR_IO = -7
        "Exception in user program",    // ERROR_IN_CHILD = -8
        "No such process"               // ERROR_NO_SUCH_PROCESS = -9
    };

    /** Performs SYSCALL_OUTPUT.
     * Displays text on the console.
     * @param s a String to display
     * @return zero
     */
    public static int output(String s) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
            Kernel.SYSCALL_OUTPUT, 0, s, null, null);
    } // output

    /** Performs SYSCALL_INPUT.
     * Waits for the user to type some text and hit [return].
     * The input line is returned in the supplied StringBuffer
     * @param result a place to put the result
     * @return zero on success, or one of the error codes Kernel.END_OF_FILE or
     * Kernel.ERROR_IO.
     */
    public static int input(StringBuffer result) {
        result.setLength(0);
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
                            Kernel.SYSCALL_INPUT, 0, result, null, null);
    } // input

    /** Performs SYSCALL_EXEC.
     * Launchs the named program, and lets it run in parallel
     * to the current program.
     * @param command The name of a Java class to execute.
     * @param args The arguments to give the new program
     * @return a non-negative process id, or ERROR_BAD_COMMAND.
     */
    public static int exec(String command, String args[]) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
            Kernel.SYSCALL_EXEC, 0, command, args, null);
    } // exec

    /** Performs SYSCALL_JOIN.
     * Waits for a process to terminate
     * @param pid a process id returned by a previous call to exec.
     * @return zero or ERROR_NO_SUCH_PROCESS
     */
    public static int join(int pid) {
        return Kernel.interrupt(Kernel.INTERRUPT_USER,
            Kernel.SYSCALL_JOIN, pid, null, null, null);
    } // join

    /**
     * Formats the disk. If the disk is already formatted, this system call
     * will destroy all data on it.
     * @return 0 on sucess and -1 on failure
     */
    public static int format() {
        int rv = Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.FORMAT_DISK, 0, null, null, null);
        if (rv == 0) {
            System.out.println("\nDisk formatted successfully.");
        } else {
            System.out.println("\nDisk format failed.");
        }
        return rv;
    }
    
    /**
     * Creates a new empty file (size 0).
     * @param fname the same name of the new file being created. 
     * @return 0 on success and -1 on failure. 
     */
    public static int create(String fname) {
        int rv = Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.CREATE_FILE, 0, fname, null, null);
        if (rv == 0) {
            System.out.println("\nFile " + fname + " created successfully.");
        } else {
            System.out.println("\nFile " + fname + " creation failed.");
        }
        return rv;
    }

    /**
     * Reads from a file.
     * @param fname the name of the file to read from.
     * @param buffer the destination for the data. 
     * @return 0 on success and -1 on failure. 
     */
    public static int read(String fname, byte[] buffer) {
        int rv = Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.READ_FILE, 0, fname, buffer, null);
        if (rv == 0) {
            System.out.println("\nFile " + fname + " read successfully.");
        } else {
            System.out.println("\nFile " + fname + " read failed.");
        }
        return rv;
    }

    /**
     * Writes to a file.
     * @param fname the name of the file to write to. 
     * @param buffer the source of the data. 
     * @return 0 on success and -1 on failure. 
     */
    public static int write(String fname, byte[] buffer) {
        int rv = Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.WRITE_FILE, 0, fname, buffer, null);
        if (rv == 0) {
            System.out.println("\nFile " + fname + " written to successfully.");
        } else {
            System.out.println("\nFile " + fname + " write failed.");
        }
        return rv;
    }

    /**
     * Reports information about the file system to System.out
     * @return 0 on success and -1 on failure. 
     */
    public static int list() {
        int rv = Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.LIST_FILES, 0, 0, null, null);
        if (rv == 0) {
            System.out.println("\nFile system information listed successfully.");
        } else {
            System.out.println("\nFile system information listing failed.");
        }
        return rv;
    }

    /**
     * Deletes the file from the disk.
     * @return 0 on success and -1 on failure. 
     */
    public static int delete(String fname) {
        int rv = Kernel.interrupt(Kernel.INTERRUPT_USER, Kernel.DELETE_FILE, 0, fname, null, null);
        if (rv == 0) {
            System.out.println("\nFile " + fname + " deleted successfully.");
        } else {
            System.out.println("\nFile " + fname + " deletion failed.");
        }
        return rv;
    }
    
} // Library
