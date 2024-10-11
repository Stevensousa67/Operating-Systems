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
        String[] instruction = cmd.split(" ");

        // Check if 'cat' has files to read
        if (instruction.length < 2) {
            System.out.println("myShell> No file specified.");
            return; // Exit the method
        }

        // Initialize variables for grep
        String grepSearchString = null;
        boolean usingGrep = false;
        StringBuilder combinedFileContent = new StringBuilder();

        // Check if the command contains a pipe '|grep'
        for (int i = 1; i < instruction.length; i++) {
            if (instruction[i].equals("|grep")) {
                usingGrep = true;

                // Ensure there's a search string after '|grep'
                if (i + 1 < instruction.length) {
                    grepSearchString = instruction[i + 1];
                } else {
                    System.out.println("myShell> No search string provided for grep.");
                    return;
                }
                break; // Exit loop after finding the pipe
            }
        }

        // If using 'grep', check if only one file is provided
        int fileCount = usingGrep ? instruction.length - 3 : instruction.length - 1;  // Exclude command, grep, and search term
        if (usingGrep && fileCount > 1) {
            System.out.println("myShell> grep can only be used with one file.");
            return; 
        }

        // Read the file(s)
        for (int i = 1; i < instruction.length; i++) {
            if (instruction[i].equals("|grep")) {
                break;  // Stop processing files when we reach the pipe
            }
            String fileToRead = instruction[i];

            try {
                File file = commandFunctions.currentDirectory.resolve("resources/" + fileToRead).toFile();
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

        // Process with grep if needed
        if (usingGrep && grepSearchString != null) {
            String[] lines = combinedFileContent.toString().split("\n");
            for (String line : lines) {
                if (line.contains(grepSearchString)) {
                    System.out.println(line);  // Print matching line
                }
            }
        } else {
            // If no grep, print the entire combined content of all files
            System.out.println(combinedFileContent.toString());
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