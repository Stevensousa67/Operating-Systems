import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class commandFunctions {

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
        File curDir = new File("./Programming_Assignment_1/src");
        File[] filesList = curDir.listFiles();
        for (File f : filesList) {
            if (f.isDirectory()) {
                System.out.println(f.getName());
            }
            if (f.isFile()) {
                System.out.println(f.getName());
            }
        }
    }
    
    public static void pwd(String cmd) {
        try {
            Path currentPath = Paths.get(".").toRealPath();
            System.out.println(currentPath.toString());
        } catch (IOException e) {
            System.err.println("Error retrieving current directory: " + e.getMessage());
        }
    }

    public static void cd(String cmd) {

    }
    
    public static void mkdir(String cmd) {

    }

    public static void rmdir(String cmd) {

    }

    public static void mv(String cmd) {

    }

    public static void cp(String cmd) {

    }

    public static void cat(String cmd) {

    }

    public static void more(String cmd) {

    }

    public static void rm(String cmd) {

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