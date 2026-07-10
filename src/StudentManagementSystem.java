import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
public class StudentManagementSystem extends JFrame {
    // Personal Details
private JTextField txtStudentID;
private JTextField txtFirstName;
private JTextField txtLastName;
private JTextField txtDOB;
private JTextArea txtAddress;
private JTextField txtMobile;
private JTextField txtEmail;
private JComboBox<String> cmbGender;

// Academic Details
private JComboBox<String> cmbCourse;
private JTextField txtCourseCode;
private JTextField txtDepartment;
private JComboBox<String> cmbSemester;
private JComboBox<String> cmbYear;
private JTextField txtSection;
private JTextField txtCGPA;
private JTextField txtAttendance;

// Guardian Details
private JTextField txtGuardianName;
private JTextField txtRelationship;
private JTextField txtGuardianMobile;
private JTextField txtGuardianEmail;
private JTextArea txtGuardianAddress;

// Additional Details
private JComboBox<String> cmbHostel;
private JComboBox<String> cmbScholarship;
private JComboBox<String> cmbTransport;
private JComboBox<String> cmbPlacement;

// Buttons
private JButton btnAdd;
private JButton btnUpdate;
private JButton btnDelete;
private JButton btnSearch;
private JButton btnReset;
private JButton btnExit;

// Table
private JTable table;
private DefaultTableModel model;
Connection con;
PreparedStatement pst;
ResultSet rs;
public StudentManagementSystem() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback
        }

        setTitle("Student Information System");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        createGUI();
        
        // 🔥 DATABASE INITIALIZATION (PUT HERE)
        con = DBConnection.getConnection();
        if (con == null) {
            JOptionPane.showMessageDialog(this, "Database connection failed");
            return;
        }

        // 🔥 LOAD DATA INTO TABLE
        loadTable();

        // 🔥 ADD BUTTON + TABLE EVENT LISTENERS
        addListeners();

        setVisible(true);
    }

    private void stylePanel(JPanel panel, String title) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
    }

    private void styleComponent(Component c) {
        if (c instanceof JLabel) {
            JLabel lbl = (JLabel) c;
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(new Color(74, 85, 104));
        } else if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setForeground(Color.BLACK);
            tf.setBackground(Color.WHITE);
            tf.setEditable(true);
            tf.setEnabled(true);
            tf.setFocusable(true);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 185, 195), 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
        } else if (c instanceof JTextArea) {
            JTextArea ta = (JTextArea) c;
            ta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ta.setForeground(Color.BLACK);
            ta.setBackground(Color.WHITE);
            ta.setEditable(true);
            ta.setEnabled(true);
            ta.setFocusable(true);
            ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        } else if (c instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) c;
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cb.setBackground(Color.WHITE);
            cb.setEnabled(true);
            cb.setFocusable(true);
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 224), 1, true));
            if (sp.getViewport().getView() != null) {
                styleComponent(sp.getViewport().getView());
            }
        } else if (c instanceof JPanel) {
            JPanel jp = (JPanel) c;
            jp.setBackground(Color.WHITE);
            for (Component child : jp.getComponents()) {
                styleComponent(child);
            }
        }
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(145, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker(), 1, true));
    }

    private void createGUI() {
        JLabel title = new JLabel("STUDENT INFORMATION SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setOpaque(true);
        title.setForeground(Color.WHITE);
        title.setBackground(new Color(41, 98, 255));
        title.setPreferredSize(new Dimension(100, 80));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.setBackground(new Color(245, 247, 250));

        tabbedPane.addTab("Personal Details", createScrollWrapper(createPersonalPanel()));
        tabbedPane.addTab("Academic Details", createScrollWrapper(createAcademicPanel()));
        tabbedPane.addTab("Guardian Details", createScrollWrapper(createGuardianPanel()));
        tabbedPane.addTab("Additional Details", createScrollWrapper(createAdditionalPanel()));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 247, 250));
        wrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        wrapper.add(tabbedPane, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createScrollWrapper(JPanel panel) {
        JScrollPane sp = new JScrollPane(panel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel createPersonalPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));

        txtStudentID = new JTextField();
        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtDOB = new JTextField();
        txtAddress = new JTextArea(3, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        txtMobile = new JTextField();
        txtEmail = new JTextField();
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        panel.add(new JLabel("Student ID"));
        panel.add(txtStudentID);
        panel.add(new JLabel("First Name"));
        panel.add(txtFirstName);
        panel.add(new JLabel("Last Name"));
        panel.add(txtLastName);
        panel.add(new JLabel("DOB (YYYY-MM-DD)"));
        panel.add(txtDOB);
        panel.add(new JLabel("Gender"));
        panel.add(cmbGender);
        panel.add(new JLabel("Address"));
        panel.add(new JScrollPane(txtAddress));
        panel.add(new JLabel("Mobile"));
        panel.add(txtMobile);
        panel.add(new JLabel("Email"));
        panel.add(txtEmail);

        stylePanel(panel, "Personal Details");
        styleComponent(panel);
        return panel;
    }

    private JPanel createAcademicPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 10, 10));

        cmbCourse = new JComboBox<>(new String[]{"B.Tech", "B.Sc", "BCA", "MCA", "MBA"});
        txtCourseCode = new JTextField();
        txtDepartment = new JTextField();
        cmbSemester = new JComboBox<>(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        cmbYear = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        txtSection = new JTextField();
        txtCGPA = new JTextField();
        txtAttendance = new JTextField();

        panel.add(new JLabel("Course"));
        panel.add(cmbCourse);
        panel.add(new JLabel("Course Code"));
        panel.add(txtCourseCode);
        panel.add(new JLabel("Department"));
        panel.add(txtDepartment);
        panel.add(new JLabel("Semester"));
        panel.add(cmbSemester);
        panel.add(new JLabel("Year"));
        panel.add(cmbYear);
        panel.add(new JLabel("Section"));
        panel.add(txtSection);
        panel.add(new JLabel("CGPA"));
        panel.add(txtCGPA);
        panel.add(new JLabel("Attendance"));
        panel.add(txtAttendance);

        stylePanel(panel, "Academic Details");
        styleComponent(panel);
        return panel;
    }

    private JPanel createGuardianPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        txtGuardianName = new JTextField();
        txtRelationship = new JTextField();
        txtGuardianMobile = new JTextField();
        txtGuardianEmail = new JTextField();
        txtGuardianAddress = new JTextArea(3, 20);
        txtGuardianAddress.setLineWrap(true);
        txtGuardianAddress.setWrapStyleWord(true);

        panel.add(new JLabel("Guardian Name"));
        panel.add(txtGuardianName);
        panel.add(new JLabel("Relationship"));
        panel.add(txtRelationship);
        panel.add(new JLabel("Mobile"));
        panel.add(txtGuardianMobile);
        panel.add(new JLabel("Email"));
        panel.add(txtGuardianEmail);
        panel.add(new JLabel("Address"));
        panel.add(new JScrollPane(txtGuardianAddress));

        stylePanel(panel, "Guardian Details");
        styleComponent(panel);
        return panel;
    }

    private JPanel createAdditionalPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        cmbHostel = new JComboBox<>(new String[]{"Yes", "No"});
        cmbScholarship = new JComboBox<>(new String[]{"Yes", "No"});
        cmbTransport = new JComboBox<>(new String[]{"Yes", "No"});
        cmbPlacement = new JComboBox<>(new String[]{"Yes", "No"});

        panel.add(new JLabel("Hostel"));
        panel.add(cmbHostel);
        panel.add(new JLabel("Scholarship"));
        panel.add(cmbScholarship);
        panel.add(new JLabel("Transport"));
        panel.add(cmbTransport);
        panel.add(new JLabel("Placement Eligible"));
        panel.add(cmbPlacement);

        stylePanel(panel, "Additional Details");
        styleComponent(panel);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnSearch = new JButton("Search");
        btnReset = new JButton("Reset");
        btnExit = new JButton("Exit");

        styleButton(btnAdd, new Color(46, 125, 50), Color.WHITE);
        styleButton(btnUpdate, new Color(245, 124, 0), Color.WHITE);
        styleButton(btnDelete, new Color(198, 40, 40), Color.WHITE);
        styleButton(btnSearch, new Color(21, 101, 192), Color.WHITE);
        styleButton(btnReset, new Color(117, 117, 117), Color.WHITE);
        styleButton(btnExit, new Color(33, 33, 33), Color.WHITE);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnExit);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.addColumn("ID");
        model.addColumn("Name");
        model.addColumn("Department");
        model.addColumn("Course");
        model.addColumn("Semester");
        model.addColumn("CGPA");

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
                }
                return c;
            }
        };

        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(225, 235, 255));
        table.setSelectionForeground(new Color(41, 98, 255));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(230, 235, 245));
        table.getTableHeader().setForeground(new Color(41, 98, 255));
        table.getTableHeader().setPreferredSize(new Dimension(100, 30));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1000, 250));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224), 1, true));

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        return mainPanel;
    }
private void addListeners() {

    btnAdd.addActionListener(e -> addStudent());
    btnUpdate.addActionListener(e -> updateStudent());
    btnDelete.addActionListener(e -> deleteStudent());
    btnSearch.addActionListener(e -> searchStudent());
    btnReset.addActionListener(e -> clearFields());
    btnExit.addActionListener(e -> System.exit(0));

    // Table row click → fill form
    table.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent e) {
            int row = table.getSelectedRow();
            if (row != -1) {
                String studentId = model.getValueAt(row, 0).toString();
                fetchAndPopulateStudent(studentId);
            }
        }
    });
}
private void fetchAndPopulateStudent(String studentId) {
    try {
        String sql = "SELECT * FROM students WHERE student_id=?";
        pst = con.prepareStatement(sql);
        pst.setString(1, studentId);
        rs = pst.executeQuery();
        if (rs.next()) {
            populateForm(rs);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
private void populateForm(ResultSet rs) throws SQLException {
    txtStudentID.setText(rs.getString("student_id"));
    txtFirstName.setText(rs.getString("first_name"));
    txtLastName.setText(rs.getString("last_name"));
    txtDOB.setText(rs.getString("dob"));
    cmbGender.setSelectedItem(rs.getString("gender"));
    txtAddress.setText(rs.getString("address"));
    txtMobile.setText(rs.getString("mobile"));
    txtEmail.setText(rs.getString("email"));

    cmbCourse.setSelectedItem(rs.getString("course"));
    txtCourseCode.setText(rs.getString("course_code"));
    txtDepartment.setText(rs.getString("department"));
    cmbSemester.setSelectedItem(rs.getString("semester"));
    cmbYear.setSelectedItem(rs.getString("year"));
    txtSection.setText(rs.getString("section"));
    txtCGPA.setText(rs.getString("cgpa"));
    txtAttendance.setText(rs.getString("attendance"));

    txtGuardianName.setText(rs.getString("guardian_name"));
    txtRelationship.setText(rs.getString("relationship"));
    txtGuardianMobile.setText(rs.getString("guardian_mobile"));
    txtGuardianEmail.setText(rs.getString("guardian_email"));
    txtGuardianAddress.setText(rs.getString("guardian_address"));

    cmbHostel.setSelectedItem(rs.getString("hostel"));
    cmbScholarship.setSelectedItem(rs.getString("scholarship"));
    cmbTransport.setSelectedItem(rs.getString("transport"));
    cmbPlacement.setSelectedItem(rs.getString("placement"));
}
private boolean validateInput() {

    if (txtStudentID.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Student ID is required");
        return false;
    }

    if (txtFirstName.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "First Name is required");
        return false;
    }

    String dob = txtDOB.getText().trim();
    if (!dob.matches("\\d{4}-\\d{2}-\\d{2}")) {
        JOptionPane.showMessageDialog(this, "DOB must be in YYYY-MM-DD format");
        return false;
    }

    if (!txtEmail.getText().contains("@")) {
        JOptionPane.showMessageDialog(this, "Invalid Email");
        return false;
    }

    if (txtMobile.getText().length() != 10) {
        JOptionPane.showMessageDialog(this, "Invalid Mobile Number");
        return false;
    }

    try {
        double cgpa = Double.parseDouble(txtCGPA.getText());
        if (cgpa < 0 || cgpa > 10) {
            JOptionPane.showMessageDialog(this, "CGPA must be 0–10");
            return false;
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Invalid CGPA");
        return false;
    }

    try {
        double attendance = Double.parseDouble(txtAttendance.getText());
        if (attendance < 0 || attendance > 100) {
            JOptionPane.showMessageDialog(this, "Attendance must be between 0 and 100");
            return false;
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Invalid Attendance");
        return false;
    }

    return true;
}
private void addStudent() {
    if (!validateInput()) return; 
    try {
        String sql = "INSERT INTO students (student_id, first_name, last_name, dob, gender, address, mobile, email, " +
                     "course, course_code, department, semester, year, section, cgpa, attendance, guardian_name, " +
                     "relationship, guardian_mobile, guardian_email, guardian_address, hostel, scholarship, transport, placement) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        pst = con.prepareStatement(sql);

        pst.setString(1, txtStudentID.getText());
        pst.setString(2, txtFirstName.getText());
        pst.setString(3, txtLastName.getText());
        pst.setString(4, txtDOB.getText());
        pst.setString(5, cmbGender.getSelectedItem().toString());
        pst.setString(6, txtAddress.getText());
        pst.setString(7, txtMobile.getText());
        pst.setString(8, txtEmail.getText());

        pst.setString(9, cmbCourse.getSelectedItem().toString());
        pst.setString(10, txtCourseCode.getText());
        pst.setString(11, txtDepartment.getText());
        pst.setInt(12, Integer.parseInt(cmbSemester.getSelectedItem().toString()));
        pst.setInt(13, Integer.parseInt(cmbYear.getSelectedItem().toString()));
        pst.setString(14, txtSection.getText());
        pst.setDouble(15, Double.parseDouble(txtCGPA.getText()));
        pst.setDouble(16, Double.parseDouble(txtAttendance.getText()));

        pst.setString(17, txtGuardianName.getText());
        pst.setString(18, txtRelationship.getText());
        pst.setString(19, txtGuardianMobile.getText());
        pst.setString(20, txtGuardianEmail.getText());
        pst.setString(21, txtGuardianAddress.getText());

        pst.setString(22, cmbHostel.getSelectedItem().toString());
        pst.setString(23, cmbScholarship.getSelectedItem().toString());
        pst.setString(24, cmbTransport.getSelectedItem().toString());
        pst.setString(25, cmbPlacement.getSelectedItem().toString());

        pst.executeUpdate();

        JOptionPane.showMessageDialog(this, "Student Added Successfully!");

        loadTable();
        clearFields();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
private void updateStudent() {
    if (!validateInput()) return;
    try {
        String sql = "UPDATE students SET first_name=?, last_name=?, dob=?, gender=?, address=?, mobile=?, email=?, " +
                     "course=?, course_code=?, department=?, semester=?, year=?, section=?, cgpa=?, attendance=?, " +
                     "guardian_name=?, relationship=?, guardian_mobile=?, guardian_email=?, guardian_address=?, " +
                     "hostel=?, scholarship=?, transport=?, placement=? WHERE student_id=?";

        pst = con.prepareStatement(sql);

        pst.setString(1, txtFirstName.getText());
        pst.setString(2, txtLastName.getText());
        pst.setString(3, txtDOB.getText());
        pst.setString(4, cmbGender.getSelectedItem().toString());
        pst.setString(5, txtAddress.getText());
        pst.setString(6, txtMobile.getText());
        pst.setString(7, txtEmail.getText());

        pst.setString(8, cmbCourse.getSelectedItem().toString());
        pst.setString(9, txtCourseCode.getText());
        pst.setString(10, txtDepartment.getText());
        pst.setInt(11, Integer.parseInt(cmbSemester.getSelectedItem().toString()));
        pst.setInt(12, Integer.parseInt(cmbYear.getSelectedItem().toString()));
        pst.setString(13, txtSection.getText());
        pst.setDouble(14, Double.parseDouble(txtCGPA.getText()));
        pst.setDouble(15, Double.parseDouble(txtAttendance.getText()));

        pst.setString(16, txtGuardianName.getText());
        pst.setString(17, txtRelationship.getText());
        pst.setString(18, txtGuardianMobile.getText());
        pst.setString(19, txtGuardianEmail.getText());
        pst.setString(20, txtGuardianAddress.getText());

        pst.setString(21, cmbHostel.getSelectedItem().toString());
        pst.setString(22, cmbScholarship.getSelectedItem().toString());
        pst.setString(23, cmbTransport.getSelectedItem().toString());
        pst.setString(24, cmbPlacement.getSelectedItem().toString());
        
        pst.setString(25, txtStudentID.getText());

        pst.executeUpdate();

        JOptionPane.showMessageDialog(this, "Updated Successfully!");

        loadTable();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
private void deleteStudent() {
    try {
        String sql = "DELETE FROM students WHERE student_id=?";
        pst = con.prepareStatement(sql);

        pst.setString(1, txtStudentID.getText());

        pst.executeUpdate();

        JOptionPane.showMessageDialog(this, "Deleted Successfully!");

        loadTable();
        clearFields();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
private void searchStudent() {
    String studentId = txtStudentID.getText().trim();
    if (studentId.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter Student ID to search");
        return;
    }
    try {
        String sql = "SELECT * FROM students WHERE student_id=?";
        pst = con.prepareStatement(sql);

        pst.setString(1, studentId);

        rs = pst.executeQuery();

        if (rs.next()) {
            populateForm(rs);
        } else {
            JOptionPane.showMessageDialog(this, "Student Not Found");
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
private void loadTable() {
    try {
        model.setRowCount(0);

        String sql = "SELECT student_id, first_name, department, course, semester, cgpa FROM students";
        Statement st = con.createStatement();
        rs = st.executeQuery(sql);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6)
            });
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
    }
}
private void clearFields() {

    txtStudentID.setText("");
    txtFirstName.setText("");
    txtLastName.setText("");
    txtDOB.setText("");
    txtAddress.setText("");
    txtMobile.setText("");
    txtEmail.setText("");

    txtCourseCode.setText("");
    txtDepartment.setText("");
    txtSection.setText("");
    txtCGPA.setText("");
    txtAttendance.setText("");

    txtGuardianName.setText("");
    txtRelationship.setText("");
    txtGuardianMobile.setText("");
    txtGuardianEmail.setText("");
    txtGuardianAddress.setText("");

    cmbGender.setSelectedIndex(0);
    cmbCourse.setSelectedIndex(0);
    cmbSemester.setSelectedIndex(0);
    cmbYear.setSelectedIndex(0);
    cmbHostel.setSelectedIndex(0);
    cmbScholarship.setSelectedIndex(0);
    cmbTransport.setSelectedIndex(0);
    cmbPlacement.setSelectedIndex(0);
}
}
