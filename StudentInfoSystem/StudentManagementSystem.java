import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class StudentManagementSystem extends JFrame {

    private JTextField idField, nameField, ageField, deptField;
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Student> studentList = new ArrayList<>();

    public StudentManagementSystem() {

        setTitle("Student Information System");
        setSize(850, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center screen
        setLayout(new BorderLayout(10, 10));

        // ================= HEADER =================
        JLabel header = new JLabel("📘 Student Information System", JLabel.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setOpaque(true);
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(100, 60));
        add(header, BorderLayout.NORTH);

        // ================= INPUT PANEL =================
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        inputPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(15);
        nameField = new JTextField(15);
        ageField = new JTextField(15);
        deptField = new JTextField(15);

        addInput(inputPanel, gbc, "ID:", idField, 0);
        addInput(inputPanel, gbc, "Name:", nameField, 1);
        addInput(inputPanel, gbc, "Age:", ageField, 2);
        addInput(inputPanel, gbc, "Department:", deptField, 3);

        // ================= BUTTONS =================
        JButton addBtn = createButton("Add", new Color(46, 204, 113));
        JButton updateBtn = createButton("Update", new Color(241, 196, 15));
        JButton deleteBtn = createButton("Delete", new Color(231, 76, 60));

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(btnPanel, gbc);

        add(inputPanel, BorderLayout.WEST);

        // ================= TABLE =================
        model = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Department"}, 0);
        table = new JTable(model);

        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(44, 62, 80));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // ================= ACTIONS =================
        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());

        table.getSelectionModel().addListSelectionListener(e -> fillFields());

        getContentPane().setBackground(new Color(245, 245, 245));
        setVisible(true);
    }

    // ---------- Helper: Input Row ----------
    private void addInput(JPanel panel, GridBagConstraints gbc, String label, JTextField field, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    // ---------- Styled Button ----------
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    // ================= CRUD METHODS =================
    private void addStudent() {
        Student s = new Student(
                Integer.parseInt(idField.getText()),
                nameField.getText(),
                Integer.parseInt(ageField.getText()),
                deptField.getText()
        );

        studentList.add(s);
        model.addRow(new Object[]{s.getId(), s.getName(), s.getAge(), s.getDepartment()});
        clearFields();
    }

    private void updateStudent() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Student s = studentList.get(row);

            s.setName(nameField.getText());
            s.setAge(Integer.parseInt(ageField.getText()));
            s.setDepartment(deptField.getText());

            model.setValueAt(s.getName(), row, 1);
            model.setValueAt(s.getAge(), row, 2);
            model.setValueAt(s.getDepartment(), row, 3);
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            studentList.remove(row);
            model.removeRow(row);
        }
    }

    private void fillFields() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            idField.setText(model.getValueAt(row, 0).toString());
            nameField.setText(model.getValueAt(row, 1).toString());
            ageField.setText(model.getValueAt(row, 2).toString());
            deptField.setText(model.getValueAt(row, 3).toString());
        }
    }

    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        ageField.setText("");
        deptField.setText("");
    }
}