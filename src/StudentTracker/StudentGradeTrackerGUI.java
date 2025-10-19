package StudentTracker;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

// --- ENUM FOR GRADE CATEGORIES ---
enum GradeCategory {
    TEST, ASSIGNMENT, PROJECT
}

// --- GRADE CLASS ---
class Grade {
    private GradeCategory category;
    private double score;

    public Grade(GradeCategory category, double score) {
        this.category = category;
        this.score = score;
    }

    public GradeCategory getCategory() { return category; }
    public double getScore() { return score; }

    @Override
    public String toString() {
        return category + ": " + score;
    }
}

// --- STUDENT CLASS ---
class Student {
    private String name;
    private ArrayList<Grade> grades;

    public Student(String name) {
        this.name = name;
        this.grades = new ArrayList<>();
    }

    public String getName() { return name; }
    public ArrayList<Grade> getGrades() { return grades; }

    public void addGrade(Grade grade) {
        grades.add(grade);
    }

    public double getAverageGrade() {
        if (grades.isEmpty()) return 0;
        double total = 0;
        for (Grade g : grades) total += g.getScore();
        return total / grades.size();
    }

    public double getHighestGrade() {
        return grades.stream().mapToDouble(Grade::getScore).max().orElse(0);
    }

    public double getLowestGrade() {
        return grades.stream().mapToDouble(Grade::getScore).min().orElse(0);
    }
}

// --- FILE MANAGER FOR SAVE/LOAD ---
class FileManager {
    private static final String FILE_NAME = "students.txt";

    public static void saveData(ArrayList<Student> students) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) {
                writer.println("STUDENT:" + s.getName());
                for (Grade g : s.getGrades()) {
                    writer.println(g.getCategory() + "," + g.getScore());
                }
                writer.println("END");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving data: " + e.getMessage());
        }
    }

    public static ArrayList<Student> loadData() {
        ArrayList<Student> students = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            Student current = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("STUDENT:")) {
                    current = new Student(line.substring(8));
                } else if (line.equals("END")) {
                    if (current != null) students.add(current);
                } else if (!line.isEmpty() && current != null) {
                    String[] parts = line.split(",");
                    GradeCategory category = GradeCategory.valueOf(parts[0]);
                    double score = Double.parseDouble(parts[1]);
                    current.addGrade(new Grade(category, score));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
        return students;
    }
}

// --- MAIN GUI CLASS ---
public class StudentGradeTrackerGUI extends JFrame {
    private ArrayList<Student> students;
    private DefaultTableModel tableModel;
    private JTable table;

    public StudentGradeTrackerGUI() {
        setTitle("🎓 Student Grade Tracker");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        students = FileManager.loadData();

        // --- TABLE SETUP ---
        String[] columns = {"Student Name", "Grades", "Average", "Highest", "Lowest"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        refreshTable();

        // --- BUTTONS ---
        JButton addBtn = new JButton("➕ Add Student");
        JButton sortBtn = new JButton("🏆 Sort by Average");
        JButton saveBtn = new JButton("💾 Save Data");
        JButton exitBtn = new JButton("🚪 Exit");

        addBtn.addActionListener(e -> addStudent());
        sortBtn.addActionListener(e -> sortStudents());
        saveBtn.addActionListener(e -> saveData());
        exitBtn.addActionListener(e -> exitProgram());

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(sortBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(exitBtn);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // --- REFRESH TABLE DATA ---
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : students) {
            StringBuilder gradeStr = new StringBuilder();
            for (Grade g : s.getGrades()) {
                gradeStr.append(g.toString()).append(" | ");
            }
            Object[] row = {
                    s.getName(),
                    gradeStr.toString(),
                    String.format("%.2f", s.getAverageGrade()),
                    String.format("%.2f", s.getHighestGrade()),
                    String.format("%.2f", s.getLowestGrade())
            };
            tableModel.addRow(row);
        }
    }

    // --- ADD STUDENT FUNCTION ---
    private void addStudent() {
        String name = JOptionPane.showInputDialog(this, "Enter Student Name:");
        if (name == null || name.isBlank()) return;

        Student student = new Student(name);

        while (true) {
            String[] categories = {"TEST", "ASSIGNMENT", "PROJECT", "Done"};
            String choice = (String) JOptionPane.showInputDialog(
                    this, "Select Grade Category:", "Grade Type",
                    JOptionPane.PLAIN_MESSAGE, null, categories, categories[0]);

            if (choice == null || choice.equals("Done")) break;

            GradeCategory category = GradeCategory.valueOf(choice);
            String gradeStr = JOptionPane.showInputDialog(this, "Enter Grade (0-100):");

            try {
                double grade = Double.parseDouble(gradeStr);
                if (grade >= 0 && grade <= 100) {
                    student.addGrade(new Grade(category, grade));
                } else {
                    JOptionPane.showMessageDialog(this, "Enter a valid grade between 0 and 100!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter a number.");
            }
        }

        students.add(student);
        refreshTable();
        JOptionPane.showMessageDialog(this, "✅ Student added successfully!");
    }

    // --- SORT FUNCTION ---
    private void sortStudents() {
        students.sort((a, b) -> Double.compare(b.getAverageGrade(), a.getAverageGrade()));
        refreshTable();
        JOptionPane.showMessageDialog(this, "🏆 Sorted by average score (highest first).");
    }

    // --- SAVE FUNCTION ---
    private void saveData() {
        FileManager.saveData(students);
        JOptionPane.showMessageDialog(this, "💾 Data saved successfully!");
    }

    // --- EXIT FUNCTION ---
    private void exitProgram() {
        FileManager.saveData(students);
        JOptionPane.showMessageDialog(this, "👋 Data saved. Exiting...");
        System.exit(0);
    }

    // --- MAIN METHOD ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentGradeTrackerGUI app = new StudentGradeTrackerGUI();
            app.setVisible(true);
        });
    }
}
