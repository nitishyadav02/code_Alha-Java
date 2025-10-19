package Java_chatbot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Paths;

public class Main {
    private static final String FAQ_PATH = Paths.get("resources", "faqs.csv").toString();

    public static void main(String[] args) {
        FAQTrainer trainer = new FAQTrainer();
        try {
            trainer.loadFromCsv(FAQ_PATH);
        } catch (Exception e) {
            System.err.println("Failed to load FAQs: " + e.getMessage());
        }

        Chatbot bot = new Chatbot(trainer);
        ChatLogger logger = new ChatLogger();

        SwingUtilities.invokeLater(() -> createAndShowGui(bot, logger));
    }

    private static void createAndShowGui(Chatbot bot, ChatLogger logger) {
        JFrame frame = new JFrame("ChatLite - AI Chatbot with Logs & Embeddings");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        JTextArea convo = new JTextArea();
        convo.setEditable(false);
        JScrollPane scroll = new JScrollPane(convo);
        JTextField input = new JTextField();
        JButton send = new JButton("Send");

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);

        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        ActionListener sendAction = e -> {
            String text = input.getText().trim();
            if (text.isEmpty()) return;
            convo.append("You: " + text + "\\n");
            logger.log("User", text);

            String reply = bot.respond(text);
            convo.append("Bot: " + reply + "\\n\\n");
            logger.log("Bot", reply);

            input.setText("");
            convo.setCaretPosition(convo.getDocument().getLength());
        };

        send.addActionListener(sendAction);
        input.addActionListener(sendAction);

        frame.setVisible(true);
    }
}
