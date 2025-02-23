
import java.util.Vector;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.awt.print.PrinterException;



public class PaymentSystemForm extends JFrame {
    private JTextField studentIdField, studentNameField, classField, amountField;
    private JComboBox<String> majorCombo, paymentCombo;
    private JButton saveButton, deleteButton, updateButton, printButton;
    private JTable table;
    private JTable footerTable;
    private JScrollPane footerScrollPane;    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_payment";
    private static final String DB_USER = "root"; // Replace with your MySQL username
    private static final String DB_PASSWORD = ""; // Replace with your MySQL password

    public PaymentSystemForm() {
        setTitle("SISTEM PEMBAYARAN SPP SMP JAKENAN");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);

        addStyledLabel(mainPanel, "ID:", labelFont);
        studentIdField = addStyledTextField(fieldFont);
        mainPanel.add(studentIdField);

        addStyledLabel(mainPanel, "Student_Name:", labelFont);
        studentNameField = addStyledTextField(fieldFont);
        mainPanel.add(studentNameField);

        addStyledLabel(mainPanel, "Class:", labelFont);
        classField = addStyledTextField(fieldFont);
        mainPanel.add(classField);

        addStyledLabel(mainPanel, "Major:", labelFont);
        String[] majors = {"Pilih", "Informatics", "Information Systems","Management","Accounting","Communication"};
        majorCombo = new JComboBox<>(majors);
        majorCombo.setFont(fieldFont);
        mainPanel.add(majorCombo);

        addStyledLabel(mainPanel, "Payment:", labelFont);
        String[] payments = {"Pilih", "Cash", "Transfer"};
        paymentCombo = new JComboBox<>(payments);
        paymentCombo.setFont(fieldFont);
        mainPanel.add(paymentCombo);

        addStyledLabel(mainPanel, "Amount:", labelFont);
        amountField = addStyledTextField(fieldFont);
        mainPanel.add(amountField);

        JPanel buttonPanel = new JPanel();
        saveButton = createStyledButton("Save", new Color(34, 139, 34));
        deleteButton = createStyledButton("Delete", new Color(220, 20, 60));
        updateButton = createStyledButton("Update", new Color(255, 165, 0));
        printButton = createStyledButton("Print", new Color(70, 130, 180));

        // Add action listeners to buttons
        saveButton.addActionListener(new SaveButtonListener());
        deleteButton.addActionListener(new DeleteButtonListener());
        updateButton.addActionListener(new UpdateButtonListener());
        printButton.addActionListener(new PrintButtonListener());

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(printButton);

        String[] columnNames = {"ID", "Student_Name", "Class", "Major", "Payment", "Amount"};
        DefaultTableModel mainModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        table = new JTable(mainModel);
        JScrollPane tableScrollPane = new JScrollPane(table);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    studentIdField.setText((String) table.getValueAt(selectedRow, 0));
                    studentNameField.setText((String) table.getValueAt(selectedRow, 1));
                    classField.setText((String) table.getValueAt(selectedRow, 2));
                    majorCombo.setSelectedItem((String) table.getValueAt(selectedRow, 3));
                    paymentCombo.setSelectedItem((String) table.getValueAt(selectedRow, 4));
                    amountField.setText((String) table.getValueAt(selectedRow, 5));
                }
            }
        });
        add(mainPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(tableScrollPane, BorderLayout.SOUTH);

        createFooterPanel();
        loadTableData();
        setupTableSelectionListener(); // Load data from database into table
    }

    private void addStyledLabel(JPanel panel, String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        panel.add(label);
    }

    private JTextField addStyledTextField(Font font) {
        JTextField textField = new JTextField();
        textField.setFont(font);
        return textField;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        return button;
    }
    private JPanel footerPanel;
    private void createFooterPanel() {
        footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a table for the footer instead of individual fields
        String[] columnNames = {"ID", "Student_Name", "Class", "Major", "Amount"};
        DefaultTableModel footerModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        
        footerTable = new JTable(footerModel);
        footerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add selection listener to sync with main table
        footerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = footerTable.getSelectedRow();
                if (selectedRow != -1) {
                    table.setRowSelectionInterval(selectedRow, selectedRow);
                    // Update the input fields with selected row data
                    updateInputFieldsFromFooter(selectedRow);
                }
            }
        });

        footerScrollPane = new JScrollPane(footerTable);
        footerScrollPane.setPreferredSize(new Dimension(780, 150));
        add(footerScrollPane, BorderLayout.SOUTH);
    }
 //new
    private void updateInputFieldsFromFooter(int row) {
        studentIdField.setText((String)footerTable.getValueAt(row, 0));
        studentNameField.setText((String)footerTable.getValueAt(row, 1));
        classField.setText((String)footerTable.getValueAt(row, 2));
        majorCombo.setSelectedItem((String)footerTable.getValueAt(row, 3));
        amountField.setText((String)footerTable.getValueAt(row, 4));
    }

    // Load data from database into table
    private void loadTableData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM student_payment";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Get the models from both tables
            DefaultTableModel mainModel = (DefaultTableModel) table.getModel();
            DefaultTableModel footerModel = (DefaultTableModel) footerTable.getModel();

            // Clear existing data
            mainModel.setRowCount(0);
            footerModel.setRowCount(0);

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("Student_Name");
                String className = rs.getString("Class");
                String major = rs.getString("Major");
                String payment = rs.getString("Payment");
                String amount = rs.getString("Amount");

                // Add data to main table
                mainModel.addRow(new Object[]{id, name, className, major, payment, amount});

                // Add data to footer table
                footerModel.addRow(new Object[]{id, name, className, major, amount});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Save button action listener
    private class SaveButtonListener implements ActionListener {
        @Override
    public void actionPerformed(ActionEvent e) {
        String studentName = studentNameField.getText();
        String studentClass = classField.getText();
        String major = (String) majorCombo.getSelectedItem();
        String paymentMethod = (String) paymentCombo.getSelectedItem();
        String amount = amountField.getText();

        // Validate input fields
        if (studentName.isEmpty() || studentClass.isEmpty() || major.equals("Pilih") || 
            paymentMethod.equals("Pilih") || amount.isEmpty()) {
            JOptionPane.showMessageDialog(PaymentSystemForm.this, 
                "Please fill in all fields and select valid options.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(amount);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(PaymentSystemForm.this, 
                "Amount must be an integer.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO student_payment (Student_Name, Class, Major, Payment, Amount) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, studentName);
            pstmt.setString(2, studentClass);
            pstmt.setString(3, major);
            pstmt.setString(4, paymentMethod);
            pstmt.setString(5, amount);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(PaymentSystemForm.this, 
                "Data saved successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);

            // Clear input fields
            studentIdField.setText("");
            studentNameField.setText("");
            classField.setText("");
            majorCombo.setSelectedIndex(0);
            paymentCombo.setSelectedIndex(0);
            amountField.setText("");

            // Refresh the table data
            loadTableData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(PaymentSystemForm.this, 
                "Error saving data to database.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    }

    // Delete button action listener
    private class DeleteButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Please select a row to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = (String) table.getValueAt(selectedRow, 0);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "DELETE FROM student_payment WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Data deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Remove row from both tables
            DefaultTableModel mainModel = (DefaultTableModel) table.getModel();
            DefaultTableModel footerModel = (DefaultTableModel) footerTable.getModel();
            mainModel.removeRow(selectedRow);
            footerModel.removeRow(selectedRow);

            // Clear input fields
            studentIdField.setText("");
            studentNameField.setText("");
            classField.setText("");
            majorCombo.setSelectedIndex(0);
            paymentCombo.setSelectedIndex(0);
            amountField.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Error deleting data from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private class UpdateButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Please select a row to update.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String id = (String) table.getValueAt(selectedRow, 0);
        String studentName = studentNameField.getText();
        String studentClass = classField.getText();
        String major = (String) majorCombo.getSelectedItem();
        String paymentMethod = (String) paymentCombo.getSelectedItem();
        String amount = amountField.getText();

        try {
            Integer.parseInt(amount);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Amount must be an integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "UPDATE student_payment SET Student_Name = ?, Class = ?, Major = ?, Payment = ?, Amount = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, studentName);
            pstmt.setString(2, studentClass);
            pstmt.setString(3, major);
            pstmt.setString(4, paymentMethod);
            pstmt.setString(5, amount);
            pstmt.setString(6, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Data updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Update both tables
            DefaultTableModel mainModel = (DefaultTableModel) table.getModel();
            DefaultTableModel footerModel = (DefaultTableModel) footerTable.getModel();

            // Update main table
            mainModel.setValueAt(studentName, selectedRow, 1);
            mainModel.setValueAt(studentClass, selectedRow, 2);
            mainModel.setValueAt(major, selectedRow, 3);
            mainModel.setValueAt(paymentMethod, selectedRow, 4);
            mainModel.setValueAt(amount, selectedRow, 5);

            // Update footer table
            footerModel.setValueAt(studentName, selectedRow, 1);
            footerModel.setValueAt(studentClass, selectedRow, 2);
            footerModel.setValueAt(major, selectedRow, 3);
            footerModel.setValueAt(amount, selectedRow, 4);

            // Clear input fields
            studentIdField.setText("");
            studentNameField.setText("");
            classField.setText("");
            majorCombo.setSelectedIndex(0);
            paymentCombo.setSelectedIndex(0);
            amountField.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(PaymentSystemForm.this, "Error updating data in database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    // Print button action listener
    private class PrintButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int rowCount = table.getRowCount();
            if (rowCount == 0) {
                JOptionPane.showMessageDialog(PaymentSystemForm.this, "No data available to print.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            StringBuilder printData = new StringBuilder();
            printData.append("==================== Student Payment Details ====================\n\n");

            // Add header row
            printData.append(String.format("%-5s %-20s %-10s %-10s %-12s %-10s\n",
                "ID", "Student Name", "Class", "Major", "Payment", "Amount"));
            printData.append("----------------------------------------------------------------\n");

            // Add all rows
            for (int row = 0; row < rowCount; row++) {
                printData.append(String.format("%-5s %-20s %-10s %-10s %-12s %-10s\n",
                    table.getValueAt(row, 0),
                    table.getValueAt(row, 1),
                    table.getValueAt(row, 2),
                    table.getValueAt(row, 3),
                    table.getValueAt(row, 4),
                    table.getValueAt(row, 5)));
            }

            printData.append("\n================================================================");

            JTextArea textArea = new JTextArea(printData.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JDialog printDialog = new JDialog(PaymentSystemForm.this, "Print Preview", true);
            printDialog.setLayout(new BorderLayout());
            printDialog.add(scrollPane, BorderLayout.CENTER);

            JButton printButton = new JButton("Print");
            printButton.addActionListener(evt -> {
                try {
                    textArea.print();
                    printDialog.dispose();
                } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(PaymentSystemForm.this, 
                        "Error printing: " + ex.getMessage(), 
                        "Print Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(printButton);
            printDialog.add(buttonPanel, BorderLayout.SOUTH);

            printDialog.pack();
            printDialog.setLocationRelativeTo(PaymentSystemForm.this);
            printDialog.setVisible(true);
        }
    }

    private void setupTableSelectionListener() {
    table.getSelectionModel().addListSelectionListener(e -> {
        int selectedRow = table.getSelectedRow(); // Get selected row index
        if (selectedRow != -1) { // Check if any row is selected
            // Update form fields with data from the selected row
            studentIdField.setText((String) table.getValueAt(selectedRow, 0)); // ID field
            studentNameField.setText((String) table.getValueAt(selectedRow, 1)); // Name field
            classField.setText((String) table.getValueAt(selectedRow, 2)); // Class field
            majorCombo.setSelectedItem((String) table.getValueAt(selectedRow, 3)); // Major field
            amountField.setText((String) table.getValueAt(selectedRow, 5)); // Amount field
        }
    });
}
    public static void main(String[] args) {
        System.out.println("Program started!"); // Debugging line
        SwingUtilities.invokeLater(() -> {
            PaymentSystemForm form = new PaymentSystemForm();
            form.setVisible(true);
        });
    }
    }

 