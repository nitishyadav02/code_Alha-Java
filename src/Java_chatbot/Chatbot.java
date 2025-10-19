package Java_chatbot;


import java.util.*;


public class Chatbot {
    private final FAQTrainer trainer;
    private final double THRESHOLD = 0.38; // tuneable


    public Chatbot(FAQTrainer trainer) {
        this.trainer = trainer;
    }


    public String respond(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Please type something so I can help.";


// quick rule-based checks
        String low = raw.toLowerCase();
        if (low.matches(".*\\b(hi|hello|hey|greetings)\\b.*")) return "Hello! How can I help you today?";
        if (low.matches(".*\\b(thank|thanks)\\b.*")) return "You're welcome — glad to help!";
        if (low.matches(".*\\b(bye|goodbye|see ya|see you)\\b.*")) return "Goodbye! Have a nice day.";


        List<String> tokens = Utils.tokenize(raw);
        Map<String, Double> v = Utils.toTfVector(tokens);


        double bestScore = -1; int bestIdx = -1;
        for (int i = 0; i < trainer.size(); i++) {
            double s = Utils.cosine(v, trainer.getQuestionVector(i));
            if (s > bestScore) { bestScore = s; bestIdx = i; }
        }


        if (bestIdx >= 0 && bestScore >= THRESHOLD) {
            return trainer.getAnswer(bestIdx) + " (matched with score=" + String.format("%.2f", bestScore) + ")";
        }


// fallback small-talk rules
        if (low.contains("how are you")) return "I'm a program, but I'm doing fine — thanks for asking!";
        if (low.contains("what can you do")) return "I can answer FAQs loaded from a CSV, and have basic small-talk capabilities.";


// generic fallback
        return "Sorry, I don't know the exact answer to that. Could you rephrase?";
    }
}
