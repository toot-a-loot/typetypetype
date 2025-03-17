import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordGenerator {
    private List<String> words;
    private Random random;

    public WordGenerator(String filePath) {
        words = new ArrayList<>();
        random = new Random();
        loadWords(filePath);
    }

    private void loadWords(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.trim().toUpperCase();
                // Check if the word contains only alphabetic characters
                if (word.matches("[A-Z]+")) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRandomWord(int length) {
        List<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if (word.length() == length) {
                filteredWords.add(word);
            }
        }
        if (!filteredWords.isEmpty()) {
            return filteredWords.get(random.nextInt(filteredWords.size()));
        }
        return null;
    }
}