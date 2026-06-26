import java.io.*;
import java.util.*;

/**
 * StudentManager.java
 * A console-based Student Management System.
 *
 * Features:
 *  - Add, View, Update, Delete student records
 *  - Search student by ID or Name
 *  - Sort students by Name or Marks
 *  - Persist data to a text file (students.txt) so records survive restarts
 *
 * Compile : javac StudentManager.java
 * Run     : java StudentManager
 */
public class StudentManager {

    // ---------- Student model (inner class) ----------
    static class Student {
        int id;
        String name;
        int age;
        String course;
        double marks;

        Student(int id, String name, int age, String course, double marks) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.course = course;
            this.marks = marks;
        }

        // Convert to a single line for file storage: id,name,age,course,marks
        String toFileLine() {
            return id + "," + name + "," + age + "," + course + "," + marks;
        }

        static Student fromFileLine(String line) {
            String[] parts = line.split(",");
            return new Student(
                    Integer.parseInt(parts[0].trim()),
                    parts[1].trim(),
                    Integer.parseInt(parts[2].trim()),
                    parts[3].trim(),
                    Double.parseDouble(parts[4].trim())
            );
        }

        @Override
        public String toString() {
            return String.format("%-5d %-20s %-5d %-15s %-6.2f", id, name, age, course, marks);
        }
    }

    // ---------- Fields ----------
    private static final String FILE_NAME = "students.txt";
    private final List<Student> students = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);
    private int nextId = 1;

    public static void main(String[] args) {
        StudentManager manager = new StudentManager();
        manager.loadFromFile();
        manager.run();
    }

    // ---------- Main loop ----------
    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> searchStudent();
                case 4 -> updateStudent();
                case 5 -> deleteStudent();
                case 6 -> sortStudents();
                case 7 -> showStatistics();
                case 8 -> {
                    saveToFile();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("===================================");
        System.out.println("     STUDENT MANAGEMENT SYSTEM      ");
        System.out.println("===================================");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Sort Students");
        System.out.println("7. Show Statistics");
        System.out.println("8. Save & Exit");
        System.out.println("===================================");
    }

    // ---------- CRUD operations ----------
    private void addStudent() {
        System.out.println("\n--- Add New Student ---");
        String name = readString("Enter name: ");
        int age = readInt("Enter age: ");
        String course = readString("Enter course: ");
        double marks = readDouble("Enter marks: ");

        Student s = new Student(nextId++, name, age, course, marks);
        students.add(s);
        System.out.println("Student added successfully with ID: " + s.id + "\n");
    }

    private void viewAllStudents() {
        System.out.println("\n--- All Students ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.\n");
            return;
        }
        printTableHeader();
        for (Student s : students) {
            System.out.println(s);
        }
        System.out.println();
    }

    private void searchStudent() {
        System.out.println("\n--- Search Student ---");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Name");
        int choice = readInt("Enter your choice: ");

        if (choice == 1) {
            int id = readInt("Enter student ID: ");
            Student s = findById(id);
            if (s != null) {
                printTableHeader();
                System.out.println(s + "\n");
            } else {
                System.out.println("Student not found.\n");
            }
        } else if (choice == 2) {
            String name = readString("Enter name (or part of it): ");
            List<Student> results = new ArrayList<>();
            for (Student s : students) {
                if (s.name.toLowerCase().contains(name.toLowerCase())) {
                    results.add(s);
                }
            }
            if (results.isEmpty()) {
                System.out.println("No matching students found.\n");
            } else {
                printTableHeader();
                for (Student s : results) System.out.println(s);
                System.out.println();
            }
        } else {
            System.out.println("Invalid choice.\n");
        }
    }

    private void updateStudent() {
        System.out.println("\n--- Update Student ---");
        int id = readInt("Enter student ID to update: ");
        Student s = findById(id);
        if (s == null) {
            System.out.println("Student not found.\n");
            return;
        }

        System.out.println("Leave field blank to keep current value.");

        System.out.print("Name [" + s.name + "]: ");
        String name = scanner.nextLine();
        if (!name.isBlank()) s.name = name.trim();

        System.out.print("Age [" + s.age + "]: ");
        String ageStr = scanner.nextLine();
        if (!ageStr.isBlank()) {
            try {
                s.age = Integer.parseInt(ageStr.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid age, keeping previous value.");
            }
        }

        System.out.print("Course [" + s.course + "]: ");
        String course = scanner.nextLine();
        if (!course.isBlank()) s.course = course.trim();

        System.out.print("Marks [" + s.marks + "]: ");
        String marksStr = scanner.nextLine();
        if (!marksStr.isBlank()) {
            try {
                s.marks = Double.parseDouble(marksStr.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid marks, keeping previous value.");
            }
        }

        System.out.println("Student updated successfully.\n");
    }

    private void deleteStudent() {
        System.out.println("\n--- Delete Student ---");
        int id = readInt("Enter student ID to delete: ");
        Student s = findById(id);
        if (s == null) {
            System.out.println("Student not found.\n");
            return;
        }
        System.out.print("Are you sure you want to delete " + s.name + "? (y/n): ");
        String confirm = scanner.nextLine();
        if (confirm.trim().equalsIgnoreCase("y")) {
            students.remove(s);
            System.out.println("Student deleted successfully.\n");
        } else {
            System.out.println("Deletion cancelled.\n");
        }
    }

    private void sortStudents() {
        if (students.isEmpty()) {
            System.out.println("No student records to sort.\n");
            return;
        }
        System.out.println("\n--- Sort Students ---");
        System.out.println("1. By Name (A-Z)");
        System.out.println("2. By Marks (High to Low)");
        int choice = readInt("Enter your choice: ");

        if (choice == 1) {
            students.sort(Comparator.comparing(s -> s.name.toLowerCase()));
            System.out.println("Sorted by name.\n");
        } else if (choice == 2) {
            students.sort((a, b) -> Double.compare(b.marks, a.marks));
            System.out.println("Sorted by marks.\n");
        } else {
            System.out.println("Invalid choice.\n");
            return;
        }
        viewAllStudents();
    }

    private void showStatistics() {
        System.out.println("\n--- Statistics ---");
        if (students.isEmpty()) {
            System.out.println("No student records found.\n");
            return;
        }
        double total = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        Student topStudent = null, bottomStudent = null;

        for (Student s : students) {
            total += s.marks;
            if (s.marks > max) { max = s.marks; topStudent = s; }
            if (s.marks < min) { min = s.marks; bottomStudent = s; }
        }

        double average = total / students.size();
        System.out.println("Total Students : " + students.size());
        System.out.printf("Average Marks  : %.2f%n", average);
        System.out.println("Topper         : " + (topStudent != null ? topStudent.name + " (" + topStudent.marks + ")" : "-"));
        System.out.println("Lowest Scorer  : " + (bottomStudent != null ? bottomStudent.name + " (" + bottomStudent.marks + ")" : "-"));
        System.out.println();
    }

    // ---------- Helper methods ----------
    private Student findById(int id) {
        for (Student s : students) {
            if (s.id == id) return s;
        }
        return null;
    }

    private void printTableHeader() {
        System.out.printf("%-5s %-20s %-5s %-15s %-6s%n", "ID", "Name", "Age", "Course", "Marks");
        System.out.println("---------------------------------------------------------");
    }

    // ---------- Input helpers (with validation) ----------
    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // ---------- File persistence ----------
    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                Student s = Student.fromFileLine(line);
                students.add(s);
                if (s.id >= nextId) nextId = s.id + 1;
            }
            System.out.println("Loaded " + students.size() + " student record(s) from " + FILE_NAME + "\n");
        } catch (IOException e) {
            System.out.println("Could not load existing data: " + e.getMessage());
        }
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) {
                writer.println(s.toFileLine());
            }
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }
}