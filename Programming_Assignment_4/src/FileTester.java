import java.io.*;
import java.util.*;

/**
 * Basic driver program to be used as a shell for the MiniKernel for the final
 * project.
 * It can be run in two modes:
 * <dl compact>
 * <dt>Interactive:
 * <dd>java Boot ... FileTester
 * <dt>With a test script file:
 * <dd>java Boot ... FileTester script
 * </dl>
 * To get a list of supported commands, type 'help' at the command prompt.
 * <p>
 * The testfile consists of commands to the driver program (one per line) as
 * well as comments. Comments beginning with /* will be ignored completely by
 * the driver. Comments beginning with // will be echoed to the output.
 * <p>
 * See the test files test*.data for examples.
 */
public class FileTester {
    /** Synopsis of commands. */
    private static final String[] helpInfo = {
            "help",
            "quit",
            "format dsize isize",
            "create fname",
            "read fname offset bytes",
            "write fname offset bytes pattern",
            "writeln fname offset",
            "create fname",
            "link oldName newName",
            "unlink fname",
            "list",
            "sync"
    };

    /**
     * Main program.
     * 
     * @param args command-line arguments (there should be at most one:
     *             the name of a test file from which to read commands).
     */
    public static void main(String[] args) {
        // NB: This program is designed only to test the file system support
        // of the kernel, so it "cheats" in using non-kernel operations to
        // read commands and write diagnostics.
        if (args.length > 1) {
            System.err.println("usage: FileTester [ script-file ]");
            System.exit(0);
        }

        // Is the input coming from a file?
        boolean fromFile = (args.length == 1);

        // Create a stream for input
        BufferedReader input = null;

        // Open our input stream
        if (fromFile) {
            try {
                input = new BufferedReader(new FileReader(args[0]));
            } catch (FileNotFoundException e) {
                System.err.println("Error: Script file "
                        + args[0] + " not found.");
                System.exit(1);
            }
        } else {
            input = new BufferedReader(new InputStreamReader(System.in));
        }

        // Cycle through user or file input
        for (;;) {
            String cmd = null;
            try {
                // Print out the prompt for the user
                if (!fromFile) {
                    pr("--> ");
                    System.out.flush();
                }

                // Read in a line
                String line = input.readLine();

                // Check for EOF and empty lines
                if (line == null) {
                    // End of file (Ctrl-D for interactive input)
                    return;
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                // Handle comments and echoing
                if (line.startsWith("//")) {
                    if (fromFile) {
                        pl(line);
                    }
                    continue;
                }
                if (line.startsWith("/*")) {
                    continue;
                }

                // Echo the command line
                if (fromFile) {
                    pl("--> " + line);
                }

                // Parse the command line
                StringTokenizer st = new StringTokenizer(line);
                cmd = st.nextToken();

                // Call the function that corresponds to the command
                int result;
                if (cmd.equalsIgnoreCase("quit")) {
                    return;
                } else if (cmd.equalsIgnoreCase("help") || cmd.equals("?")) {
                    help();
                    continue;
                } else if (cmd.equalsIgnoreCase("format")) {
                    result = Library.format();
                } else if (cmd.equalsIgnoreCase("create")) {
                    result = Library.create(st.nextToken());
                } else if (cmd.equalsIgnoreCase("read")) {
                    String fname = st.nextToken();
                    byte[] dataReturned = new byte[512];
                    result = Library.read(fname, dataReturned);
                    String myData = Utilities.unpackString(dataReturned, 0);
                    myData = myData.trim();
                    System.out.println(myData);
                } else if (cmd.equalsIgnoreCase("write")) {
                    String fname = st.nextToken();
                    String data = st.nextToken();
                    while (st.hasMoreTokens()) {
                        data += " " + st.nextToken();
                    }
                    byte[] buffer = new byte[512];
                    Utilities.pack(data, buffer, 0);
                    result = Library.write(fname, buffer);
                } else if (cmd.equalsIgnoreCase("delete")) {
                    String fileName = st.nextToken();
                    result = Library.delete(fileName);
                } else if (cmd.equalsIgnoreCase("ls") || cmd.equalsIgnoreCase("dir")) {
                    result = Library.list();
                } else {
                    pl("unknown command");
                    continue;
                }
                // Print out the result of the function call
                switch (result) {
                    case 0 -> {
                    }
                    case -1 -> pl("*** System call failed");
                    default -> pl("*** Result " + result + " from system call");
                }
            } catch (NumberFormatException e) {
                pl("Invalid argument: " + e);
            } catch (NoSuchElementException e) {
                // Handler for nextToken()
                pl("Incorrect number of arguments");
                help(cmd);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } // for (;;)
    } // main(String[])

    /** Prints a list of available commands. */
    private static void help() {
        pl("Commands are:");
        for (String helpInfo1 : helpInfo) {
            pl("    " + helpInfo1);
        }
    } // help()

    /**
     * Prints help for command "cmd".
     * 
     * @param cmd the name of the command.
     */
    private static void help(String cmd) {
        for (int i = 0; i < helpInfo.length; i++) {
            if (helpInfo[i].startsWith(cmd)) {
                pl("usage: " + helpInfo[i]);
                return;
            }
        }
        pl("unknown command '" + cmd + "'");
    } // help(String)

    /**
     * Prints a line to System.out followed by a newline.
     * 
     * @param o the message to print.
     */
    private static void pl(Object o) {
        System.out.println(o);
    } // pl(Object)

    /**
     * Prints a line to System.out.
     * 
     * @param o the message to print.
     */
    private static void pr(Object o) {
        System.out.print(o);
    } // pl(Object)
} // FileTester
