package Java_chatbot;

import java.util.*;
import java.util.regex.*;


public class Utils {
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a","an","the","is","are","was","were","in","on","at","for","to","of","and","or","but","if","then","this","that","it","i","you","we","they","he","she","do","does","did","has","have","had"
    ));


    public static List<String> tokenize(String text) {
        if (text == null) return Collections.emptyList();
        text = text.toLowerCase(Locale.ROOT);
        text = text.replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = text.split("\\s+");
        List<String> out = new ArrayList<>();
        for (String t : tokens) {
            if (t.isEmpty() || STOPWORDS.contains(t)) continue;
            out.add(stem(t));
        }
        return out;
    }


    // Very small, heuristic stemmer (not a full Porter stemmer) — sufficient for demo
    public static String stem(String word) {
        if (word.length() <= 3) return word;
        if (word.endsWith("ing") && word.length() > 4) return word.substring(0, word.length()-3);
        if (word.endsWith("ed") && word.length() > 3) return word.substring(0, word.length()-2);
        if (word.endsWith("s") && word.length() > 3) return word.substring(0, word.length()-1);
        return word;
    }


    public static Map<String, Double> toTfVector(List<String> tokens) {
        Map<String, Double> freq = new HashMap<>();
        for (String t : tokens) freq.put(t, freq.getOrDefault(t, 0.0) + 1.0);
// normalize by length
        double len = tokens.size() == 0 ? 1.0 : tokens.size();
        for (Map.Entry<String, Double> e : new HashMap<>(freq).entrySet()) {
            freq.put(e.getKey(), e.getValue() / len);
        }
        return freq;
    }


    public static double cosine(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0.0;
        for (Map.Entry<String, Double> e : a.entrySet()) {
            dot += e.getValue() * b.getOrDefault(e.getKey(), 0.0);
        }
        double na = 0.0, nb = 0.0;
        for (double v : a.values()) na += v*v;
        for (double v : b.values()) nb += v*v;
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
