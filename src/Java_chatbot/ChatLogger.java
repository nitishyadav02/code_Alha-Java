package Java_chatbot;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatLogger {
    private final File logFile;
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss");

    public ChatLogger() {
        String date = new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date());
        File dir = new File("chatlogs");
        if (!dir.exists()) dir.mkdirs();
        logFile = new File(dir, "session-" + date + ".txt");
    }

    public synchronized void log(String speaker, String message) {
        try (FileWriter fw = new FileWriter(logFile, true)) {
            String line = String.format("[%s] %s: %s%n",
                    timeFmt.format(new Date()), speaker, message);
            fw.write(line);
        } catch (IOException e) {
            System.err.println("⚠️ Failed to write to chat log: " + e.getMessage());
        }
    }

    public File getLogFile() {
        return logFile;
    }
}