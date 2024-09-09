import java.io.*;
import java.util.*;

/** Basic driver program to be used as a shell for the MiniKernel for the final project.
 * It can be run in two modes:
 * <dl compact>
 *        <dt>Interactive:              <dd>java Boot ... FileTester
 *        <dt>With a test script file:  <dd>java Boot ... FileTester script
 * </dl>
 * To get a list of supported commands, type 'help' at the command prompt.
 * <p>
 * The testfile consists of commands to the driver program (one per line) as
 * well as comments.  Comments beginning with /* will be ignored completely by
 * the driver.  Comments beginning with // will be echoed to the output.
 * <p>
 * See the test files test*.data for examples.
 */
public class FileTester {
    /** Synopsis of commands. */
    private static String[] helpInfo = {
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

    /** Main program.
     * @param args command-line arguments (there should be at most one:
     *      the name of a test file from which to read commands).
     */
    public static void main(String [] args){
        // NB:  This program is designed only to test the file system support
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
                int result = 0;
                if (cmd.equalsIgnoreCase("quit")) {
                    return;
                } else if (cmd.equalsIgnoreCase("help") || cmd.equals("?")) {
                    help();
                    continue;
                } else if (cmd.equalsIgnoreCase("format")) {
                    int dsize = Integer.parseInt(st.nextToken());
                    int isize = Integer.parseInt(st.nextToken());
                    result = Library.format(dsize, isize);
                } else if (cmd.equalsIgnoreCase("create")) {
                    result = Library.create(st.nextToken());
                } else if (cmd.equalsIgnoreCase("read")) {
                    String fname = st.nextToken();
                    int offset = Integer.parseInt(st.nextToken());
                    int size = Integer.parseInt(st.nextToken());
                    result = readTest(fname, offset, size);
                } else if (cmd.equalsIgnoreCase("write")) {
                    String fname = st.nextToken();
                    int offset = Integer.parseInt(st.nextToken());
                    int size = Integer.parseInt(st.nextToken());
                    String pattern = st.nextToken();
                    result = writeTest(fname, offset, size, pattern);
                } else if (cmd.equalsIgnoreCase("writeln")) {
                    String fname = st.nextToken();
                    int offset = Integer.parseInt(st.nextToken());
                    result = writeTest(fname, offset, input);
                } else if (cmd.equalsIgnoreCase("link")) {
                    String oldName = st.nextToken();
                    String newName = st.nextToken();
                    result = Library.link(oldName, newName);
                } else if (cmd.equalsIgnoreCase("unlink")) {
                    result = Library.unlink(st.nextToken());
                } else if (cmd.equalsIgnoreCase("list")) {
                    result = Library.list();
                } else if (cmd.equalsIgnoreCase("sync")) {
                    result = Library.sync();
                } else {
                    pl("unknown command");
                    continue;
                }

                // Print out the result of the function call
                switch (result) {
                case 0:
                    break;
                case -1:
                    pl("*** System call failed");
                    break;
                default:
                    pl("*** Result " + result + " from system call");
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
        for (int i = 0; i < helpInfo.length; i++) {
            pl("    " + helpInfo[i]);
        }
    } // help()

    /** Prints help for command "cmd".
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

    /** Reads data from a (simulated) file using Library.read
     * and displays the results.
     * @param fname the name of the file.
     * @param offset the starting position in the file.
     * @param size the number of bytes to read.
     * @return the result of the Library.read call.
     */
    private static int readTest(String fname, int offset, int size) {
        byte[] buf = new byte[size];
        int n = Library.read(fname, offset, buf);
        boolean needNewline = false;
        if (n < 0) {
            return n;
        }
        for (int i = 0; i < n; i++) {
            showChar(buf[i] & 0xff);
            needNewline = (buf[i] != '\n');
        }
        if (needNewline) {
            pl("");
        }
        return n;
    } // readTest(String, int, int)

    /** Writes data to a (simulated) file using Library.write.
     * @param fname the name of the file.
     * @param offset the starting location in the file.
     * @param size the number of bytes to write.
     * @param data a source of data.
     * @return the result of the Library.write call.
     */
    private static int writeTest(
                String fname, int offset, int size, String data)
    {
        byte[] buf = new byte[size];
        int p = 0;
        for (int i = 0; i < size; i++) {
            buf[i] = (byte) data.charAt(p++);
            if (p >= data.length()) {
                p = 0;
            }
        }
        return Library.write(fname, offset, buf);
    } // writeTest(String, int, int, String)

    /** Write data to a (simulated) file using Library.write.
     * Data comes from the following lines in the input stream.
     * @param fname the name of the file.
     * @param offset the starting offset in the file.
     * @param in the source of data.
     * @return the result of the Library.write call.
     */
    private static int writeTest(String fname, int offset, BufferedReader in) {
        StringBuffer sb = new StringBuffer();
        try {
            for (;;) {
                String line = in.readLine();
                if (line == null || line.equals(".")) {
                    break;
                }
                sb.append(line).append('\n');
            }
            byte[] buf = new byte[sb.length()];
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) sb.charAt(i);
            }
            return Library.write(fname, offset, buf);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    } // writeTest(String, int, BufferedReader)

    /** Display a readable representation of a byte.
     * @param b the byte to display as a number in the range 0..255.
     */
    private static void showChar(int b) {
        if (b >= ' ' && b <= '~') {
            pr((char)b);
            return;
        }
        if (b == '\n') {
            pl("\\n");
            return;
        }
        if (b == '\\') {
            pr("\\\\");
            return;
        }
        pr('\\');
        pr(Integer.toString(b, 8));
    } // showChar(int)

    /** Prints a line to System.out followed by a newline.
     * @param o the message to print.
     */
    private static void pl(Object o) {
        System.out.println(o);
    } // pl(Object)

    /** Prints a line to System.out.
     * @param o the message to print.
     */
    private static void pr(Object o) {
        System.out.print(o);
    } // pl(Object)

    /** Prints a character to System.out.
     * @param c the character to print.
     */
    private static void pr(char c) {
        System.out.print(c);
    } // pl(char)
} // FileTester
