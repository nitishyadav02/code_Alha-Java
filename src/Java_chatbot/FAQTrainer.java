package Java_chatbot;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FAQTrainer {
    private final List<String> questions = new ArrayList<>();
    private final List<String> answers = new ArrayList<>();
    private final List<Map<String, Double>> questionVectors = new ArrayList<>();


    public void loadFromCsv(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        boolean headerSkipped = false;
        for (String line : lines) {
            if (!headerSkipped) { headerSkipped = true; continue; }
            if (line.trim().isEmpty()) continue;
// naive split on first comma — in production, use a CSV parser
            int idx = line.indexOf(',');
            if (idx < 0) continue;
            String q = line.substring(0, idx).trim();
            String r = line.substring(idx+1).trim();
            questions.add(q);
            answers.add(r);
            questionVectors.add(Utils.toTfVector(Utils.tokenize(q)));
        }
    }


    public int size() { return questions.size(); }
    public String getQuestion(int i) { return questions.get(i); }
    public String getAnswer(int i) { return answers.get(i); }
    public Map<String, Double> getQuestionVector(int i) { return questionVectors.get(i); }
}