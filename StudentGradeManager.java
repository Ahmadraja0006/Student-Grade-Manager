/**
 * Student Grade Manager
 * Author: Ahmad Raja Ansari
 * Description: A console-based Java application to manage student records,
 *              calculate grades, generate report cards, and persist data
 *              using file I/O. Demonstrates OOP, ArrayList, and exception handling.
 */

import java.util.*;
import java.io.*;

// ─── Student Class (OOP: Encapsulation) ──────────────────────────────────────

class Student {
    private int id;
    private String name;
    private Map<String, Double> subjects; // subject name → marks

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.subjects = new LinkedHashMap<>();
    }

    // Getters
    public int getId()              { return id; }
    public String getName()         { return name; }
    public Map<String, Double> getSubjects() { return subjects; }

    // Add or update a subject mark
    public void addSubject(String subject, double marks) {
        subjects.put(subject, marks);
    }

    // Calculate average percentage
    public double getAverage() {
        if (subjects.isEmpty()) return 0.0;
        double total = 0;
        for (double m : subjects.values()) total += m;
        return total / subjects.size();
    }

    // Calculate total marks
    public double getTotal() {
        double total = 0;
        for (double m : subjects.values()) total += m;
        return total;
    }

    // Assign letter grade based on average
    public String getGrade() {
        double avg = getAverage();
        if (avg >= 90) return "A+";
        else if (avg >= 80) return "A";
        else if (avg >= 70) return "B";
        else if (avg >= 60) return "C";
        else if (avg >= 50) return "D";
        else return "F";
    }

    // Pass/Fail: all subjects must be >= 40
    public String getStatus() {
        for (double m : subjects.values()) {
            if (m < 40) return "FAIL";
        }
        return subjects.isEmpty() ? "N/A" : "PASS";
    }

    // Serialize to a single CSV line for file storage
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|").append(name).append("|");
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : subjects.entrySet()) {
            entries.add(e.getKey() + ":" + e.getValue());
        }
        sb.append(String.join(",", entries));
        return sb.toString();
    }

    // Deserialize from a CSV line
    public static Student fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 2) return null;
        Student s = new Student(Integer.parseInt(parts[0].trim()), parts[1].trim());
        if (parts.length == 3 && !parts[2].trim().isEmpty()) {
            for (String entry : parts[2].split(",")) {
                String[] kv = entry.split(":");
                if (kv.length == 2) {
                    s.addSubject(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                }
            }
        }
        return s;
    }
}


// ─── GradeManager Class (OOP: Encapsulation + Abstraction) ───────────────────

class GradeManager {
    private List<Student> students;
    private int nextId;
    private static final String DATA_FILE = "students.txt";

    public GradeManager() {
        students = new ArrayList<>();
        nextId = 1;
        loadFromFile();
    }

    // Add a new student
    public void addStudent(String name) {
        students.add(new Student(nextId++, name));
        saveToFile();
        System.out.println("  ✓ Student '" + name + "' added with ID " + (nextId - 1) + ".");
    }

    // Find student by ID
    public Student findById(int id) {
        for (Student s : students) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    // Add marks to a student
    public void addMarks(int id, String subject, double marks) {
        Student s = findById(id);
        if (s == null) {
            System.out.println("  Student with ID " + id + " not found.");
            return;
        }
        if (marks < 0 || marks > 100) {
            System.out.println("  Marks must be between 0 and 100.");
            return;
        }
        s.addSubject(subject, marks);
        saveToFile();
        System.out.println("  ✓ Added " + marks + " for '" + subject + "' to " + s.getName() + ".");
    }

    // Display all students
    public void viewAllStudents() {
        System.out.println("\n─── All Students ───");
        if (students.isEmpty()) {
            System.out.println("  No students added yet.");
            return;
        }
        System.out.printf("\n  %-5s %-20s %-10s %-8s %-8s%n", "ID", "Name", "Subjects", "Average", "Grade");
        System.out.println("  " + "─".repeat(58));
        for (Student s : students) {
            System.out.printf("  %-5d %-20s %-10d %-8.2f %-8s%n",
                s.getId(), s.getName(), s.getSubjects().size(),
                s.getAverage(), s.getGrade());
        }
    }

    // Print a detailed report card for one student
    public void printReportCard(int id) {
        Student s = findById(id);
        if (s == null) {
            System.out.println("  Student not found.");
            return;
        }
        System.out.println("\n  ╔══════════════════════════════════════╗");
        System.out.println("  ║         STUDENT REPORT CARD          ║");
        System.out.println("  ╚══════════════════════════════════════╝");
        System.out.printf("  Name   : %s%n", s.getName());
        System.out.printf("  ID     : %d%n", s.getId());
        System.out.println("  " + "─".repeat(40));

        if (s.getSubjects().isEmpty()) {
            System.out.println("  No subjects added yet.");
        } else {
            System.out.printf("  %-20s %s%n", "Subject", "Marks (/100)");
            System.out.println("  " + "─".repeat(40));
            for (Map.Entry<String, Double> e : s.getSubjects().entrySet()) {
                String bar = "█".repeat((int)(e.getValue() / 5)); // 20-char max bar
                System.out.printf("  %-20s %5.1f  %s%n", e.getKey(), e.getValue(), bar);
            }
            System.out.println("  " + "─".repeat(40));
            System.out.printf("  %-20s %5.1f%n", "Total", s.getTotal());
            System.out.printf("  %-20s %5.1f%%%n", "Average", s.getAverage());
            System.out.printf("  %-20s %s%n", "Grade", s.getGrade());
            System.out.printf("  %-20s %s%n", "Status", s.getStatus());
        }
        System.out.println("  " + "─".repeat(40));
    }

    // Class-wide topper and statistics
    public void classStatistics() {
        System.out.println("\n─── Class Statistics ───");
        if (students.isEmpty()) {
            System.out.println("  No students to analyze.");
            return;
        }

        List<Student> withMarks = new ArrayList<>();
        for (Student s : students) {
            if (!s.getSubjects().isEmpty()) withMarks.add(s);
        }

        if (withMarks.isEmpty()) {
            System.out.println("  No marks recorded yet.");
            return;
        }

        double classTotal = 0;
        Student topper = withMarks.get(0);
        Student lowest = withMarks.get(0);
        int passCount = 0;

        for (Student s : withMarks) {
            classTotal += s.getAverage();
            if (s.getAverage() > topper.getAverage()) topper = s;
            if (s.getAverage() < lowest.getAverage()) lowest = s;
            if (s.getStatus().equals("PASS")) passCount++;
        }

        double classAvg = classTotal / withMarks.size();

        System.out.printf("%n  Total Students   : %d%n", students.size());
        System.out.printf("  Class Average    : %.2f%%%n", classAvg);
        System.out.printf("  Topper           : %s (%.2f%%)%n", topper.getName(), topper.getAverage());
        System.out.printf("  Lowest Average   : %s (%.2f%%)%n", lowest.getName(), lowest.getAverage());
        System.out.printf("  Pass / Fail      : %d / %d%n", passCount, withMarks.size() - passCount);

        // Grade distribution
        Map<String, Integer> gradeDist = new LinkedHashMap<>();
        for (String g : new String[]{"A+","A","B","C","D","F"}) gradeDist.put(g, 0);
        for (Student s : withMarks) {
            gradeDist.put(s.getGrade(), gradeDist.get(s.getGrade()) + 1);
        }
        System.out.println("\n  Grade Distribution:");
        for (Map.Entry<String, Integer> e : gradeDist.entrySet()) {
            System.out.printf("    %-4s : %s (%d)%n", e.getKey(), "■".repeat(e.getValue()), e.getValue());
        }
    }

    // Delete a student by ID
    public void deleteStudent(int id) {
        Student s = findById(id);
        if (s == null) {
            System.out.println("  Student not found.");
            return;
        }
        students.remove(s);
        saveToFile();
        System.out.println("  ✓ Student '" + s.getName() + "' removed.");
    }

    // ─── File I/O ────────────────────────────────────────────────────────────

    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            pw.println(nextId); // save next ID on first line
            for (Student s : students) {
                pw.println(s.toFileString());
            }
        } catch (IOException e) {
            System.out.println("  Warning: Could not save data. " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line = br.readLine();
            if (line != null) nextId = Integer.parseInt(line.trim());
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Student s = Student.fromFileString(line);
                    if (s != null) students.add(s);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("  Warning: Could not load saved data.");
        }
    }
}


// ─── Main Class (Entry Point) ─────────────────────────────────────────────────

public class StudentGradeManager {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("       📚 Student Grade Manager");
        System.out.println("═".repeat(50));

        GradeManager gm = new GradeManager();

        while (true) {
            System.out.println("\n  What would you like to do?");
            System.out.println("  1. Add student");
            System.out.println("  2. Add marks for a student");
            System.out.println("  3. View all students");
            System.out.println("  4. View report card");
            System.out.println("  5. Class statistics");
            System.out.println("  6. Delete student");
            System.out.println("  7. Exit");
            System.out.print("\n  Enter choice (1-7): ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("  Enter student name: ");
                    String name = sc.nextLine().trim();
                    if (!name.isEmpty()) gm.addStudent(name);
                    else System.out.println("  Name cannot be empty.");
                    break;

                case "2":
                    gm.viewAllStudents();
                    System.out.print("\n  Enter Student ID: ");
                    try {
                        int id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("  Enter subject name: ");
                        String subject = sc.nextLine().trim();
                        System.out.print("  Enter marks (0-100): ");
                        double marks = Double.parseDouble(sc.nextLine().trim());
                        gm.addMarks(id, subject, marks);
                    } catch (NumberFormatException e) {
                        System.out.println("  Invalid input. Please enter numbers for ID and marks.");
                    }
                    break;

                case "3":
                    gm.viewAllStudents();
                    break;

                case "4":
                    gm.viewAllStudents();
                    System.out.print("\n  Enter Student ID: ");
                    try {
                        int id = Integer.parseInt(sc.nextLine().trim());
                        gm.printReportCard(id);
                    } catch (NumberFormatException e) {
                        System.out.println("  Invalid ID.");
                    }
                    break;

                case "5":
                    gm.classStatistics();
                    break;

                case "6":
                    gm.viewAllStudents();
                    System.out.print("\n  Enter Student ID to delete: ");
                    try {
                        int id = Integer.parseInt(sc.nextLine().trim());
                        gm.deleteStudent(id);
                    } catch (NumberFormatException e) {
                        System.out.println("  Invalid ID.");
                    }
                    break;

                case "7":
                    System.out.println("\n  Goodbye! Keep learning. 👋\n");
                    return;

                default:
                    System.out.println("  Invalid choice. Enter 1-7.");
            }
        }
    }
}