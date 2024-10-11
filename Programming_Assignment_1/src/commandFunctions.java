import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class commandFunctions {

    public static Path currentDirectory = Paths.get(".").toAbsolutePath().normalize(); // Shared current working directory for all methods

    public static void history(List<String> cmdHistory) {
        System.out.println();
        System.out.printf("%-10s %-10s%n", "Order", "Command");
        System.out.println("----------------------------");
        for (int i = 0; i < cmdHistory.size(); i++) {
            System.out.printf("%-10d %-10s%n", i + 1, cmdHistory.get(i));
        }
        System.out.println();
    }
    
    public static void exit(String cmd) {
        System.out.println("myShell> Exit command found. myShell closing.\n");
        System.exit(0);
    }

    public static void ls(String cmd) {
        File curDir = currentDirectory.toFile();  // Use the tracked current directory
        File[] filesList = curDir.listFiles();
        if (filesList != null) {
            for (File f : filesList) {
                if (f.isDirectory()) {
                    System.out.println(f.getName());
                }
                if (f.isFile()) {
                    System.out.println(f.getName());
                }
            }
        } else {
            System.out.println("myShell> Cannot access current directory.");
        }
    }
    
    public static void pwd(String cmd) {
        System.out.println(currentDirectory.toString());
    }

    public static void cd(String cmd) throws IOException {
        String[] instruction = cmd.split(" ");

        if (instruction.length > 1) {
            if (instruction[1].equals("..")) {
                // Go up one directory
                currentDirectory = currentDirectory.getParent();
                if (currentDirectory == null) {
                    currentDirectory = Paths.get("/").toAbsolutePath().normalize(); // Handle root case
                }
            } else {
                // Resolve the new path
                Path newPath = currentDirectory.resolve(instruction[1]).normalize();

                // Check if the new path is a directory and exists
                if (Files.isDirectory(newPath)) {
                    currentDirectory = newPath;
                } else {
                    System.out.println("myShell> Directory does not exist: " + instruction[1]);
                }
            }
        } else {
            System.out.println("myShell> No directory specified.");
        }
    }

    public static void cat(String cmd) {
        // Split the command by pipes first
        String[] instruction = cmd.split("\\|"); // Use pipe as a separator

        // Initialize variables for grep and lc
        String grepSearchString = null;
        boolean usingGrep = false;
        boolean usingLc = false;
        StringBuilder combinedFileContent = new StringBuilder();

        // Process the first command (the one before any pipes)
        String[] catCommand = instruction[0].trim().split(" ");

        // Check if 'cat' has files to read
        if (catCommand.length < 2) {
            System.out.println("myShell> No file specified.");
            return; // Exit the method
        }

        // Track the number of files
        int fileCount = catCommand.length - 1; // subtract 1 for 'cat'

        // Read the file(s)
        for (int i = 1; i < catCommand.length; i++) {
            String fileToRead = catCommand[i]; // Get each file name

            try {
                File file = commandFunctions.currentDirectory.resolve("resources/" + fileToRead).toFile();

                // Open and read the file
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        combinedFileContent.append(line).append("\n");
                    }
                }

            } catch (IOException e) {
                System.out.println("myShell> File not found: " + fileToRead);
            }
        }

        // Process the subsequent commands (grep and lc)
        for (int i = 1; i < instruction.length; i++) {
            String commandPart = instruction[i].trim();
            if (commandPart.startsWith("grep")) {
                usingGrep = true; // We are using grep
                String[] grepCommand = commandPart.split(" ");
                if (grepCommand.length < 2) {
                    System.out.println("myShell> No search string provided for grep.");
                    return;
                }
                grepSearchString = grepCommand[1];  // Get the search string for grep

                // Ensure only one file is passed to grep
                if (fileCount > 1) {
                    System.out.println("myShell> grep can only be used with one file.");
                    return; // Exit if more than one file is provided with grep
                }

                // Filter the combined content with grep
                String[] lines = combinedFileContent.toString().split("\n");
                StringBuilder grepResult = new StringBuilder();
                for (String line : lines) {
                    if (line.contains(grepSearchString)) {
                        grepResult.append(line).append("\n");
                    }
                }
                combinedFileContent = grepResult;  // Update to the filtered result
            } else if (commandPart.equals("lc")) {
                usingLc = true; // We are using lc here
            }
        }

        // If using lc, count the lines and print the total count
        if (usingLc) {
            String[] lines = combinedFileContent.toString().split("\n");
            // Remove any empty strings that might result from splitting new lines
            int nonEmptyLines = 0;
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    nonEmptyLines++;
                }
            }
            System.out.println(nonEmptyLines);  // Output the number of lines found by grep
        } else {
            // If no lc, print the entire content
            System.out.println(combinedFileContent.toString());
        }
    }

    public static void reRunLastNCommands(String cmd, List<String> cmdHistory) throws IOException {
        // Get the number of commands to rerun
        int n = Integer.parseInt(cmd.substring(1));
        // If n > history size, rerun entire history starting at the end of the list
        if (n > cmdHistory.size()) {
            n = cmdHistory.size();
        }

        // Execute the last n commands from history
        for (int i = cmdHistory.size() - n; i < cmdHistory.size(); i++) {
            // Directly call the buildCommand method without logging this command
            executeCommandFromHistory(cmdHistory.get(i)); // Execute the command from history
        }
    }

    private static void executeCommandFromHistory(String command) throws IOException {
        // Directly execute the command from history without adding it to history again
        // Here you can parse the command and call respective functions, just like in buildCommand

        // Breakdown command and argument(s)
        String[] cmdPlusArgsArray = command.split(" ");
        String cmd = cmdPlusArgsArray[0].toLowerCase();
        String[] args = new String[cmdPlusArgsArray.length - 1];
        System.arraycopy(cmdPlusArgsArray, 1, args, 0, args.length);

        // Check what the command is and call the proper function passing along any arguments
        switch (cmd) {
            case "exit" ->
                commandFunctions.exit(cmd);
            case "ls" ->
                commandFunctions.ls(cmd);
            case "pwd" ->
                commandFunctions.pwd(cmd);
            case "cd" ->
                commandFunctions.cd(commandFactory.cmdHistory.get(commandFactory.cmdHistory.size() - 1)); // pass the last command in the history list
            case "cat" ->
                commandFunctions.cat(commandFactory.cmdHistory.get(commandFactory.cmdHistory.size() - 1)); // pass the last command in the history list
            case "date" ->
                commandFunctions.date(cmd);
            default ->
                System.out.println("myShell> Command not found. myShell closing.\n");
        }
    }

    public static void date(String cmd) {
        // Get day of the week, month, day, year, and time
        String dayOfWeek = LocalDate.now().getDayOfWeek().name().subSequence(0, 3).toString();
        dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
        String month = LocalDate.now().getMonth().name().subSequence(0, 3).toString();
        month = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        int day = LocalDate.now().getDayOfMonth();
        int year = LocalDate.now().getYear();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = LocalTime.now().format(timeFormatter);
        System.out.println(dayOfWeek + " " + month + " " + day + " " + formattedTime + " " + year);
    }
}