package Java_chatbot;

import java.util.*;

// This is a mock version. Replace `getEmbedding()` with a real API call later.
public class EmbeddingService {

    // Mock: create simple random or token-based vector
    public double[] getEmbedding(String text) {
        List<String> tokens = Utils.tokenize(text);
        double[] vec = new double[64]; // 64-dim mock vector
        for (String t : tokens) {
            int h = Math.abs(t.hashCode() % 64);
            vec[h] += 1.0;
        }
        double len = 0.0;
        for (double v : vec) len += v*v;
        len = Math.sqrt(len);
        if (len > 0) for (int i=0; i<vec.length; i++) vec[i] /= len;
        return vec;
    }

    public double cosine(double[] a, double[] b) {
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i=0; i<a.length; i++) {
            dot += a[i]*b[i];
            na += a[i]*a[i];
            nb += b[i]*b[i];
        }
        if (na==0||nb==0) return 0.0;
        return dot / (Math.sqrt(na)*Math.sqrt(nb));
    }
}
