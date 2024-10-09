/*
 * Author: Steven Sousa
 * Date: 10/08/2024
 * Description: This is the main class that will run the program.
 * Professor: Abdul Sattar
 * Course: COMP 350-001
 */

import java.util.Scanner;
import java.util.regex.*;

public class myShell {

    public static void main(String args[]) {
        Scanner getCommandLine = new Scanner(System.in);
        String commandLine;
        /* Flag to allow looping for additional command lines */
        boolean loopFlag = true;
        /* Loop through commands on command line */
        try {
            while (loopFlag) {
                /* Display Shell cursor */
                System.out.print("\nmyShell> ");
                /* Get new command line input */
                commandLine = getCommandLine.nextLine();
                /* Is the command EXIT detected? */
                Pattern exitCommand = Pattern.compile("exit");
                Matcher exitMatched = exitCommand.matcher(commandLine);
                /* If EXIT is detected, close myShell */
                if (exitMatched.find()) {
                    System.out.println("myShell> Exit command found.myShell closing.\n");
                    System.exit(0);
                } else {
                    // create a CommandFactory object
                    // then execute that command
                }
            }
        }
        catch (Exception e) {
            System.out.println("\n\nInterrupt was detected. myShell closing.");
            System.exit(0);
        }
    }
}

// class CommandFactory {

//     private final String cmdPlusArgs;
//     /**
//      * Constructor with the given command line.
//     */
//     public CommandFactory(String cmdPlusArgs) {
//         this.cmdPlusArgs = cmdPlusArgs;
//         // break up the command and any of its arguments
//         // add this command to history
//         // Implement the command
//     }
// }