package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * Reads all lines from a given file.
     * @param filename the name of the file to read
     * @return A list of lines read from the file
     */
    public static List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        File file = new File(filename);
        
        if (!file.exists()) {
            return lines; // Return empty list if file doesn't exist yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Writes a list of lines to a given file, overwriting existing content.
     * @param filename the name of the file
     * @param lines the data to write
     */
    public static void writeLines(String filename, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filename);
            e.printStackTrace();
        }
    }
}
