import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TodoApp {
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private ArrayList<Task> tasks;
    private ArrayList<Task> archivedTasks = new ArrayList<>();
    private JTextField taskEntry;
    private JComboBox<String> priorityBox;
    private JTextField dueDateEntry;
    private boolean addingNewTask = true;
    private int selectedRow = -1;
    private JFrame archiveFrame;
    private DefaultTableModel archiveTableModel;

    private class Task implements Comparable<Task> {
        boolean isComplete;
        String name;
        String priority;
        Date dueDate;

        Task(boolean isComplete, String name, String priority, String dueDateStr)
                throws ParseException {
            this.isComplete = isComplete;
            this.name = name;
            this.priority = priority;
            this.dueDate = new SimpleDateFormat("MM/dd/yyyy").parse(dueDateStr);
        }

        @Override
        public int compareTo(Task other) {
            int dateComparison = this.dueDate.compareTo(other.dueDate);
            if (dateComparison != 0) {
                return dateComparison;
            }
            return this.priority.compareTo(other.priority);
        }

    }

    public TodoApp() {
        JFrame frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        JOptionPane.showMessageDialog(frame, "This is a To-Do List application. \n"
                + "- To add a task, enter the details and click 'Add Task'. \n"
                + "- To edit a task, select a task from the table and click 'Edit Task'. Update the fields and click 'Update Task'. \n"
                + "- To delete a task, select a task from the table and click 'Delete Task'. \n"
                + "- To archive completed tasks, click 'Archive Data'. \n"
                + "- To view archived tasks, click 'View Archived'.", "How to use To-Do List",
                JOptionPane.INFORMATION_MESSAGE);

        tasks = new ArrayList<>();

        String[] columnNames = { "Task", "Priority", "Due Date", "Complete" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 3) ? Boolean.class : String.class;
            }
        };
        tableModel.addTableModelListener(e -> {
            if (e.getColumn() == 3 && e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                boolean isChecked = (boolean) tableModel.getValueAt(row, 3);
                tasks.get(row).isComplete = isChecked;
            }
        });
        taskTable = new JTable(tableModel);
        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                if (column < 3) {
                    String dueDateStr = table.getModel().getValueAt(row, 2).toString();
                    try {
                        Date dueDate = new SimpleDateFormat("MM/dd/yyyy").parse(dueDateStr);
                        Date today = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                        today = sdf.parse(sdf.format(today));

                        if (dueDate.before(today)) {
                            c.setForeground(Color.RED);
                        } else if (dueDate.equals(today)) {
                            c.setForeground(new Color(139, 139, 0)); // Darker yellow
                        } else {
                            c.setForeground(Color.GREEN);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel(new FlowLayout());

        taskEntry = new JTextField(20);
        priorityBox = new JComboBox<>(new String[] { "High", "Medium", "Low" });
        dueDateEntry = new JTextField("MM/DD/YYYY", 10);

        JButton addTaskButton = new JButton("Add Task");
        JButton editTaskButton = new JButton("Edit Task");
        JButton deleteTaskButton = new JButton("Delete Task");
        JButton archiveButton = new JButton("Archive Data");
        archiveButton.addActionListener(e -> {
            archiveCompletedTasks(e);
        });
        JButton viewArchiveButton = new JButton("View Archived");
        viewArchiveButton.addActionListener(this::viewArchivedTasks); // Link the button to the
                                                                      // method

        addTaskButton.addActionListener(e -> {
            addOrEditTask(addingNewTask, selectedRow);
            if (!addingNewTask) {
                addingNewTask = true;
                addTaskButton.setText("Add Task");
            }
        });

        editTaskButton.addActionListener(e -> {
            selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                taskEntry.setText(tasks.get(selectedRow).name);
                priorityBox.setSelectedItem(tasks.get(selectedRow).priority);
                dueDateEntry.setText(
                        new SimpleDateFormat("MM/dd/yyyy").format(tasks.get(selectedRow).dueDate));
                addingNewTask = false;
                addTaskButton.setText("Update Task");
            }
        });

        deleteTaskButton.addActionListener(e -> {
            selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                tasks.remove(selectedRow);
                updateTable();
            }
        });

//        archiveButton.addActionListener(e -> {
//            tasks.removeIf(task -> task.isComplete);
//            updateTable();
//        });

        panel.add(taskEntry);
        panel.add(priorityBox);
        panel.add(dueDateEntry);
        panel.add(addTaskButton);
        panel.add(editTaskButton);
        panel.add(deleteTaskButton);
        panel.add(archiveButton);
        panel.add(viewArchiveButton);

        frame.add(panel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void viewArchivedTasks(ActionEvent e) {
        archiveFrame = new JFrame("Archived Tasks");
        archiveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        archiveFrame.setSize(1500, 1500);

        String[] columnNames = { "Task", "Priority", "Due Date" };
        archiveTableModel = new DefaultTableModel(columnNames, 0);
        JTable archiveTable = new JTable(archiveTableModel);

        JScrollPane archiveScrollPane = new JScrollPane(archiveTable);
        archiveFrame.add(archiveScrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(ev -> archiveFrame.dispose());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);
        archiveFrame.add(bottomPanel, BorderLayout.SOUTH);

        for (Task task : archivedTasks) {
            Object[] rowData = { task.name, task.priority,
                    new SimpleDateFormat("MM/dd/yyyy").format(task.dueDate) };
            archiveTableModel.addRow(rowData);
        }

        archiveFrame.setVisible(true);
    }

    private void archiveCompletedTasks(ActionEvent e) {
        Iterator<Task> taskIterator = tasks.iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.isComplete) {
                archivedTasks.add(task);
                taskIterator.remove();
            }
        }
        updateTable();
    }

    private void addOrEditTask(boolean isAdd, int rowIndex) {
        String taskName = taskEntry.getText();
        String priority = (String) priorityBox.getSelectedItem();
        String dueDate = dueDateEntry.getText();

        if (taskName.trim().isEmpty() || priority == null || dueDate.equals("MM/DD/YYYY")) {
            JOptionPane.showMessageDialog(null, "Please enter valid task details.");
            return;
        }

        try {
            if (isAdd) {
                tasks.add(new Task(false, taskName, priority, dueDate));
            } else {
                tasks.get(rowIndex).name = taskName;
                tasks.get(rowIndex).priority = priority;
                tasks.get(rowIndex).dueDate = new SimpleDateFormat("MM/dd/yyyy").parse(dueDate);
            }
            Collections.sort(tasks);
            updateTable();

            taskEntry.setText("");
            dueDateEntry.setText("MM/DD/YYYY");

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid date.");
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            Object[] rowData = { task.name, task.priority,
                    new SimpleDateFormat("MM/dd/yyyy").format(task.dueDate), task.isComplete };
            tableModel.addRow(rowData);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoApp::new);
    }
}
