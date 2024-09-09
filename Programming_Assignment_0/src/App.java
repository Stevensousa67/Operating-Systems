
/* Author: Steven Sousa
 * Date: 9/12/2020
 * Bridgewater State University - COMP350-002 - Prof. Abdul Sattar
 * Description: This program reads a text file, counts word occurrences, stores the line numbers where the words appear, and prints a table with the data.
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class App {
    public static void main(String[] args) {
        File file = new File("resources/alice.txt"); // Read the file
        TreeMap<String, WordData> wordMap = new TreeMap<>(); // Create a TreeMap to store the words and their data
        parseFile(file, wordMap);
        printTable(wordMap);
    }

    private static void parseFile(File file, TreeMap<String, WordData> wordMap) {
        int lineNumber = 0; // Initialize the line number to 0
        try (Scanner scanner = new Scanner(file)) { // Create a scanner object to read the file
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine(); // Read the line
                parseLine(line, lineNumber, wordMap); // Parse the line and store the words and their data in the map
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

    private static void parseLine(String line, int lineNumber, TreeMap<String, WordData> wordMap) {
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

    private static void printTable(TreeMap<String, WordData> wordMap) {
        System.out.printf("%-15s %-10s %-30s%n", "Word", "Count", "Line Numbers"); // Header with column widths
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
    
    static class WordData {
        // Inner class to store the count and line numbers of a word
        int count;
        ArrayList<Integer> lineNumbers = new ArrayList<>();
    
        WordData(int count, ArrayList<Integer> lineNumbers) {
            this.count = count;
            this.lineNumbers = lineNumbers;
        }
    }
}