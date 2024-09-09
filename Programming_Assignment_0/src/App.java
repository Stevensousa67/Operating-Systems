
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class App {

    public static void main(String[] args) {
        File file = new File("resources/alice.txt"); // Read the file
        TreeMap<String, WordData> wordMap = new TreeMap<>(); // Create a TreeMap to store the words and their data
        int lineNumber = 0; // Initialize the line number to 0
        try (Scanner scanner = new Scanner(file)) { // Create a scanner object to read the file
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine(); // Read the line
                String[] words = line.split("[^a-zA-Z]"); // Split the line into words
                for (String word : words) { // Iterate through the words in the line
                    if (word.length() > 0) { // Check if the word is not empty
                        word = word.toLowerCase(); // Convert the word to lowercase
                        if (wordMap.containsKey(word)) { // Check if the word is already in the map
                            WordData wordData = wordMap.get(word); // Get the data of the word
                            wordData.count++; // Increment the count of the word
                            wordData.lineNumbers.add(lineNumber); // Add the line number to the list of line numbers
                        } else {
                            ArrayList<Integer> lineNumbers = new ArrayList<>(); // Create a new list of line numbers
                            lineNumbers.add(lineNumber); // Add the line number to the list of line numbers
                            wordMap.put(word, new WordData(1, lineNumbers)); // Add the word to the map
                        }
                    }
                }
            }
        }   
        catch (FileNotFoundException e){
            System.out.println(e);
        }

        // Print the words and their data in a table format with aligned columns: word, count, line numbers.
        System.out.printf (
        "%-15s %-10s %-30s%n", "Word", "Count", "Line Numbers"); // Header with column widths
        for (String word : wordMap.keySet()) {
            WordData wordData = wordMap.get(word);
            System.out.printf("%-15s %-10d ", word, wordData.count); // Adjust the width of word and count columns

            // Print the line numbers
            for (int i = 0; i < wordData.lineNumbers.size(); i++) {
                System.out.print(wordData.lineNumbers.get(i));
                if (i < wordData.lineNumbers.size() - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println(); // Newline after each word
        }
    }
    // Inner class to store the count and line numbers of a word
    static class WordData {
        int count;
        ArrayList<Integer> lineNumbers = new ArrayList<>();
    
        WordData(int count, ArrayList<Integer> lineNumbers) {
            this.count = count;
            this.lineNumbers = lineNumbers;
        }
    }
}