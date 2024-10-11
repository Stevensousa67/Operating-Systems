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

        // Check if 'cat' has a file to read
        if (instruction.length == 1) {
            System.out.println("myShell> No file specified.");
        } else {
            // Iterate over multiple files, if requested (cat file1 file2 ...)
            for (int i = 1; i < instruction.length; i++) {
                try {
                    // Resolve the file path relative to the current directory
                    File file = currentDirectory.resolve("resources/" + instruction[i]).toFile();

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        // Read the file line by line
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("myShell> File not found: " + instruction[i]);
                }
            }
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