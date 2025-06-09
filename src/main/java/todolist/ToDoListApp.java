package todolist;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ToDoListApp extends JFrame {

    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JTextField taskInput;
    private JButton addButton;
    private JButton completeButton;
    private JButton removeButton;
    private JButton saveButton;
    private JButton loadButton;
    private ArrayList<Task> tasks;

    
    public ToDoListApp() {
        setTitle("Simple To-Do");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 350);
        setLocationRelativeTo(null); // center the window

        tasks = new ArrayList<>();
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);

        taskInput = new JTextField(20);
        addButton = new JButton("Add Task");
        completeButton = new JButton("Mark as Complete");
        removeButton = new JButton("Remove Task");
        saveButton = new JButton("Save to CSV");
        loadButton = new JButton("Load from CSV");

        // Layout
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("New Task:"));
        inputPanel.add(taskInput);
        inputPanel.add(addButton);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(completeButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Action Listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        completeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                completeTask();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeTask();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTasksToCSV();
            }
        });

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTasksFromCSV();
            }
        });
    }
    private void addTask() {
        String description = taskInput.getText().trim();
        if (!description.isEmpty()) {
            Task newTask = new Task(description);
            tasks.add(newTask);
            taskListModel.addElement(newTask);
            taskInput.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "todolist.Task description cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task task = taskListModel.getElementAt(selectedIndex);
            task.setDone(!task.isDone()); // toggle completion status
            taskList.repaint(); // refresh the list display
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to mark as complete/incomplete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task taskToRemove = taskListModel.getElementAt(selectedIndex);
            tasks.remove(taskToRemove);
            taskListModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to remove", "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveTasksToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Tasks to CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // header row
                writer.println("\"Description\", \"IsDone\"");

                for (Task task : tasks) {
                    String desc = task.getDescription();
                    String escapedDescription = desc.replace("\"", "\"\"");
                    writer.println("\"" + escapedDescription + "\"," + task.isDone());
                }
                JOptionPane.showMessageDialog(this, "Tasks saved successfully to " + fileToSave.getName(), "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

        }
    }

    private void loadTasksFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load tasks from CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(fileToLoad))) {
                String line;
                tasks.clear();
                taskListModel.clear();


                String header = reader.readLine();
                parseCSV(header, reader, fileToLoad);
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error parsing CSV file: " + e.getMessage(), "Parsing Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();            }
        }
    }


    private void parseCSV(String header, BufferedReader reader, File fileToLoad) throws IOException {
        String line;
        if (header != null || !header.trim().equalsIgnoreCase("\"Decription\", \"IsDone\"")) {
            if (header != null && !header.trim().equalsIgnoreCase("\"Decription\", \"IsDone\"")) {
                reader.close();
                BufferedReader newReader = new BufferedReader(new FileReader(fileToLoad));

                if (header != null && !header.trim().equalsIgnoreCase("\"Description\", \"IsDone\"")) {
                    processCsvLine(header);
                }
                while ((line = newReader.readLine()) != null) {
                    processCsvLine(line);
                }
                newReader.close();
            } else if (header == null) {
                // File is empty OR had non-matching header
                JOptionPane.showMessageDialog(this, "CSV file is empty or has an unexpected format.", "Load Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Header was found and skipped -> proceed reading data lines
                while ((line = reader.readLine()) != null) {
                    processCsvLine(line); // TODO: unreachable?
                }
            }
        } else {
            while ((line = reader.readLine()) != null) {
                processCsvLine(line);
            }
        }
        JOptionPane.showMessageDialog(this, "Tasks loaded successfully from " + fileToLoad.getName(), "Load Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    private void processCsvLine(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return; // Skip empty lines
        }

        // Basic CSV parsing: assumes description is quoted and status is not.
        // Example: "Go to gym",false
        // This is a simplified parser. For robust CSV, a library is better.
        if (line.startsWith("\"")) {
            int lastQuoteIndex = line.lastIndexOf("\",");
            if (lastQuoteIndex > 0 && lastQuoteIndex + 2 < line.length()) {
                String description = line.substring(1, lastQuoteIndex);
                // Unescape double quotes
                description = description.replace("\"\"", "\"");
                String statusString = line.substring(lastQuoteIndex + 2); // +2 for the quote and comma

                boolean isDone = Boolean.parseBoolean(statusString.trim());
                Task task = new Task(description);
                task.setDone(isDone);
                tasks.add(task);
                taskListModel.addElement(task);
            } else {
                System.err.println("Skipping malformed line (simple parser): " + line);
            }
        } else {
            // Fallback for lines not starting with a quote (e.g. if saved without quotes)
            String[] parts = line.split(",", 2); // Split into 2 parts at most
            if (parts.length == 2) {
                String description = parts[0].trim();
                boolean isDone = Boolean.parseBoolean(parts[1].trim());
                Task task = new Task(description);
                task.setDone(isDone);
                tasks.add(task);
                taskListModel.addElement(task);
            } else {
                System.err.println("Skipping malformed line (simple parser, no quotes): " + line);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ToDoListApp().setVisible(true);
            }
        });
    }

}
