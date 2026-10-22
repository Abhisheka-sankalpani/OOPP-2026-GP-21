package gui;

import dao.UndergraduateDAO;
import model.Undergraduate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UndergraduateProfileForm extends JFrame {

    private String studentId;
    private UndergraduateDAO undergraduateDAO;
    private Undergraduate currentStudent;

    private JTextField txtEmail;
    private JTextField txtContact;
    private JTextField txtProfilePic;

    public UndergraduateProfileForm(String studentId) {
        this.studentId = studentId;
        this.undergraduateDAO = new UndergraduateDAO();
        this.currentStudent = undergraduateDAO.getStudentById(this.studentId);

        setTitle("Student Profile - " + studentId);
        setSize(550, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("My Profile Information");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(8, 2, 10, 10));

        if (currentStudent != null) {
            centerPanel.add(new JLabel("Student ID:"));
            centerPanel.add(new JLabel(currentStudent.getStudentId()));

            centerPanel.add(new JLabel("Registration No:"));
            centerPanel.add(new JLabel(currentStudent.getRegNo()));

            centerPanel.add(new JLabel("Full Name:"));
            centerPanel.add(new JLabel(currentStudent.getFullName()));

            centerPanel.add(new JLabel("Department:"));
            centerPanel.add(new JLabel(currentStudent.getDepartment()));

            // Email Field
            centerPanel.add(new JLabel("Email:"));
            txtEmail = new JTextField(currentStudent.getEmail());
            centerPanel.add(txtEmail);

            // Contact Number Field
            centerPanel.add(new JLabel("Contact Number:"));
            txtContact = new JTextField(currentStudent.getContactNo());
            centerPanel.add(txtContact);

            // Profile Picture Field
            centerPanel.add(new JLabel("Profile Picture:"));

            JPanel picPanel = new JPanel(new BorderLayout(5, 0));
            txtProfilePic = new JTextField(currentStudent.getProfilePic());
            JButton btnBrowse = new JButton("Browse...");

            btnBrowse.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        txtProfilePic.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });

            picPanel.add(txtProfilePic, BorderLayout.CENTER);
            picPanel.add(btnBrowse, BorderLayout.EAST);

            centerPanel.add(picPanel);

            // Update button
            centerPanel.add(new JLabel(""));
            JButton btnUpdate = new JButton("Update");

            btnUpdate.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String email = txtEmail.getText();
                    String contactNo = txtContact.getText();
                    String profilePicPath = txtProfilePic.getText();

                    boolean isSuccess = undergraduateDAO.updateContactDetails(studentId, email, contactNo, profilePicPath);

                    if (isSuccess == true) {
                        JOptionPane.showMessageDialog(panel, "Updated Successfully!");
                    } else {
                        JOptionPane.showMessageDialog(panel, "Update Failed!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            centerPanel.add(btnUpdate);

        } else {
            centerPanel.add(new JLabel("Student data not found in Database!"));
        }

        panel.add(centerPanel, BorderLayout.CENTER);
        add(panel);
    }
}