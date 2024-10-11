/*
 * Author: Steven Sousa
 * Date: 10/08/2024
 * Description: This is the cli class that will run the CLI and pass on the command to the CommandFactory.
 * Professor: Abdul Sattar
 * Course: COMP 350-001
 */

import java.io.IOException;
import java.util.Scanner;

public class Cli {

    public static void runCli() {
        try (
            Scanner getCommandLine = new Scanner(System.in)) {
            String commandLine;
            boolean loopFlag = true;
            try {
                while (loopFlag) {
                    System.out.print(commandFunctions.currentDirectory + "/myShell> ");
                    commandLine = getCommandLine.nextLine();
                    String[] commands = commandLine.split(" ");
                    commandFactory.buildCommand(commands);
                }
            } catch (IOException e) {
                System.out.println("\n\nInterrupt was detected. myShell closing.");
                System.exit(0);
            }
        }
    }
}
