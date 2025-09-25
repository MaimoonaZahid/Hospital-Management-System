package com.example;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

public class App {
    JFrame frame;
    private MongoDBConnection dbConnection;

    // Define constants for collection names (exact matches)
    private static final String COLLECTION_PERSON = "Person";
    private static final String COLLECTION_PATIENT = "Patient";
    private static final String COLLECTION_DISEASE = "Disease";
    private static final String COLLECTION_DOCTORS = "Doctors";
    private static final String COLLECTION_SPECIALTIES = "Specialties";
    private static final String COLLECTION_STAFF = "Staff";
    private static final String COLLECTION_DEPARTMENTS = "Departments";
    private static final String COLLECTION_APPOINTMENT = "Appointment";
    private static final String COLLECTION_BILLS = "Bills";
    private static final String COLLECTION_MEDICAL_RECORD = "Medical record";
    private static final String COLLECTION_COUNTERS = "counters";

    // Define constants for field names
    private static final String FIELD_ID = "_id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_PERSON_ID = "person_id";
    private static final String FIELD_EMERGENCY_CONTACT = "emergency_contact";
    private static final String FIELD_DISEASE_IDS = "disease_ids";
    private static final String FIELD_SCHEDULE = "schedule";
    private static final String FIELD_SPECIALTY_ID = "specialty_id";
    private static final String FIELD_DISEASE_NAME = "disease_name";
    private static final String FIELD_SPECIALTY_NAME = "specialty_name";
    private static final String FIELD_POSITION = "position";
    private static final String FIELD_DEPARTMENT_ID = "department_id";
    private static final String FIELD_DEPARTMENT_NAME = "department_name";
    private static final String FIELD_APPOINTMENT_DATE = "appointment_date";
    private static final String FIELD_APPOINTMENT_TIME = "appointment_time";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_BILL_DATE = "bill_date";
    private static final String FIELD_PAID_STATUS = "paid_status";
    private static final String FIELD_APPOINTMENT_ID = "appointment_id";
    private static final String FIELD_DIAGNOSIS = "diagnosis";
    private static final String FIELD_TREATMENT = "treatment";

    public static void main(String[] args) {
        // Set the Look and Feel to the system's default for better UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        EventQueue.invokeLater(() -> {
            try {
                App window = new App();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     */
    public App() {
        dbConnection = new MongoDBConnection();
        if (dbConnection.testConnection()) {
            System.out.println("Successfully connected to MongoDB.");
        } else {
            System.out.println("Failed to connect to MongoDB.");
            JOptionPane.showMessageDialog(null, "Failed to connect to MongoDB.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Exit if connection fails
        }
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Hospital Management System");
        frame.setBounds(100, 100, 1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Tabbed Pane for Persons, Patients, Doctors, Staff, Medical Records, Appointments
        JTabbedPane tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // Persons Tab
        JPanel personPanel = new JPanel();
        tabbedPane.addTab("Persons", null, personPanel, null);
        personPanel.setLayout(new BorderLayout());

        JScrollPane personScrollPane = new JScrollPane();
        personPanel.add(personScrollPane, BorderLayout.CENTER);
        JTable personTable = new JTable();
        personScrollPane.setViewportView(personTable);

        JPanel personButtonPanel = new JPanel();
        personPanel.add(personButtonPanel, BorderLayout.NORTH);

        JButton btnShowPersons = new JButton("Show Persons");
        personButtonPanel.add(btnShowPersons);
        btnShowPersons.addActionListener(e -> loadPersonData(personTable));

        JButton btnAddPerson = new JButton("Add Person");
        personButtonPanel.add(btnAddPerson);
        btnAddPerson.addActionListener(e -> openPersonDialog(null));

        JButton btnEditPerson = new JButton("Edit Person");
        personButtonPanel.add(btnEditPerson);
        btnEditPerson.addActionListener(e -> {
            int selectedRow = personTable.getSelectedRow();
            if (selectedRow >= 0) {
                String personId = personTable.getValueAt(selectedRow, 0).toString();
                openPersonDialog(personId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a person to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDeletePerson = new JButton("Delete Person");
        personButtonPanel.add(btnDeletePerson);
        btnDeletePerson.addActionListener(e -> {
            int selectedRow = personTable.getSelectedRow();
            if (selectedRow >= 0) {
                String personId = personTable.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Person ID: " + personId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePerson(personId);
                    loadPersonData(personTable);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a person to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Search Panel (for searching Persons by name)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        personPanel.add(searchPanel, BorderLayout.SOUTH);

        JLabel lblSearch = new JLabel("Search by Name:");
        searchPanel.add(lblSearch);

        JTextField txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        searchPanel.add(btnSearch);
        btnSearch.addActionListener(e -> searchPersonByName(txtSearch.getText(), personTable));

        // Patients Tab
        JPanel patientPanel = new JPanel();
        tabbedPane.addTab("Patients", null, patientPanel, null);
        patientPanel.setLayout(new BorderLayout());

        JScrollPane patientScrollPane = new JScrollPane();
        patientPanel.add(patientScrollPane, BorderLayout.CENTER);
        JTable patientTable = new JTable();
        patientScrollPane.setViewportView(patientTable);

        JPanel patientButtonPanel = new JPanel();
        patientPanel.add(patientButtonPanel, BorderLayout.NORTH);

        JButton btnShowPatients = new JButton("Show Patients");
        patientButtonPanel.add(btnShowPatients);
        btnShowPatients.addActionListener(e -> loadPatientData(patientTable));

        JButton btnAddPatient = new JButton("Add Patient");
        patientButtonPanel.add(btnAddPatient);
        btnAddPatient.addActionListener(e -> openPatientDialog(null));

        JButton btnEditPatient = new JButton("Edit Patient");
        patientButtonPanel.add(btnEditPatient);
        btnEditPatient.addActionListener(e -> {
            int selectedRow = patientTable.getSelectedRow();
            if (selectedRow >= 0) {
                String patientId = patientTable.getValueAt(selectedRow, 0).toString();
                openPatientDialog(patientId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a patient to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDeletePatient = new JButton("Delete Patient");
        patientButtonPanel.add(btnDeletePatient);
        btnDeletePatient.addActionListener(e -> {
            int selectedRow = patientTable.getSelectedRow();
            if (selectedRow >= 0) {
                String patientId = patientTable.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Patient ID: " + patientId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePatient(patientId);
                    loadPatientData(patientTable);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a patient to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Doctors Tab
        JPanel doctorPanel = new JPanel();
        tabbedPane.addTab("Doctors", null, doctorPanel, null);
        doctorPanel.setLayout(new BorderLayout());

        JScrollPane doctorScrollPane = new JScrollPane();
        doctorPanel.add(doctorScrollPane, BorderLayout.CENTER);
        JTable doctorTable = new JTable();
        doctorScrollPane.setViewportView(doctorTable);

        JPanel doctorButtonPanel = new JPanel();
        doctorPanel.add(doctorButtonPanel, BorderLayout.NORTH);

        JButton btnShowDoctors = new JButton("Show Doctors");
        doctorButtonPanel.add(btnShowDoctors);
        btnShowDoctors.addActionListener(e -> loadDoctorData(doctorTable));

        JButton btnAddDoctor = new JButton("Add Doctor");
        doctorButtonPanel.add(btnAddDoctor);
        btnAddDoctor.addActionListener(e -> openDoctorDialog(null));

        JButton btnEditDoctor = new JButton("Edit Doctor");
        doctorButtonPanel.add(btnEditDoctor);
        btnEditDoctor.addActionListener(e -> {
            int selectedRow = doctorTable.getSelectedRow();
            if (selectedRow >= 0) {
                String doctorId = doctorTable.getValueAt(selectedRow, 0).toString();
                openDoctorDialog(doctorId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a doctor to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDeleteDoctor = new JButton("Delete Doctor");
        doctorButtonPanel.add(btnDeleteDoctor);
        btnDeleteDoctor.addActionListener(e -> {
            int selectedRow = doctorTable.getSelectedRow();
            if (selectedRow >= 0) {
                String doctorId = doctorTable.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Doctor ID: " + doctorId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteDoctor(doctorId);
                    loadDoctorData(doctorTable);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a doctor to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Staff Tab
        JPanel staffPanel = new JPanel();
        tabbedPane.addTab("Staff", null, staffPanel, null);
        staffPanel.setLayout(new BorderLayout());

        JScrollPane staffScrollPane = new JScrollPane();
        staffPanel.add(staffScrollPane, BorderLayout.CENTER);
        JTable staffTable = new JTable();
        staffScrollPane.setViewportView(staffTable);

        JPanel staffButtonPanel = new JPanel();
        staffPanel.add(staffButtonPanel, BorderLayout.NORTH);

        JButton btnShowStaff = new JButton("Show Staff");
        staffButtonPanel.add(btnShowStaff);
        btnShowStaff.addActionListener(e -> loadStaffData(staffTable));

        JButton btnAddStaff = new JButton("Add Staff");
        staffButtonPanel.add(btnAddStaff);
        btnAddStaff.addActionListener(e -> openStaffDialog(null));

        JButton btnEditStaff = new JButton("Edit Staff");
        staffButtonPanel.add(btnEditStaff);
        btnEditStaff.addActionListener(e -> {
            int selectedRow = staffTable.getSelectedRow();
            if (selectedRow >= 0) {
                String staffId = staffTable.getValueAt(selectedRow, 0).toString();
                openStaffDialog(staffId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a staff member to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDeleteStaff = new JButton("Delete Staff");
        staffButtonPanel.add(btnDeleteStaff);
        btnDeleteStaff.addActionListener(e -> {
            int selectedRow = staffTable.getSelectedRow();
            if (selectedRow >= 0) {
                String staffId = staffTable.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Staff ID: " + staffId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteStaff(staffId);
                    loadStaffData(staffTable);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a staff member to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Medical Records Tab
        JPanel medicalRecordPanel = new JPanel();
        tabbedPane.addTab("Medical Records", null, medicalRecordPanel, null);
        medicalRecordPanel.setLayout(new BorderLayout());

        JScrollPane medicalRecordScrollPane = new JScrollPane();
        medicalRecordPanel.add(medicalRecordScrollPane, BorderLayout.CENTER);
        JTable medicalRecordTable = new JTable();
        medicalRecordScrollPane.setViewportView(medicalRecordTable);

        JPanel medicalRecordButtonPanel = new JPanel();
        medicalRecordPanel.add(medicalRecordButtonPanel, BorderLayout.NORTH);

        JButton btnShowMedicalRecords = new JButton("Show Medical Records");
        medicalRecordButtonPanel.add(btnShowMedicalRecords);
        btnShowMedicalRecords.addActionListener(e -> loadMedicalRecordData(medicalRecordTable));

        JButton btnAddMedicalRecord = new JButton("Add Medical Record");
        medicalRecordButtonPanel.add(btnAddMedicalRecord);
        btnAddMedicalRecord.addActionListener(e -> openMedicalRecordDialog(null));

        JButton btnEditMedicalRecord = new JButton("Edit Medical Record");
        medicalRecordButtonPanel.add(btnEditMedicalRecord);
        btnEditMedicalRecord.addActionListener(e -> {
            int selectedRow = medicalRecordTable.getSelectedRow();
            if (selectedRow >= 0) {
                String recordId = medicalRecordTable.getValueAt(selectedRow, 0).toString();
                openMedicalRecordDialog(recordId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a medical record to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDeleteMedicalRecord = new JButton("Delete Medical Record");
        medicalRecordButtonPanel.add(btnDeleteMedicalRecord);
        btnDeleteMedicalRecord.addActionListener(e -> {
            int selectedRow = medicalRecordTable.getSelectedRow();
            if (selectedRow >= 0) {
                String recordId = medicalRecordTable.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Medical Record ID: " + recordId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteMedicalRecord(recordId);
                    loadMedicalRecordData(medicalRecordTable);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a medical record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Appointments Tab
        JPanel appointmentPanel = new JPanel();
        tabbedPane.addTab("Appointments", null, appointmentPanel, null);
        appointmentPanel.setLayout(new BorderLayout());

        JScrollPane appointmentScrollPane = new JScrollPane();
        appointmentPanel.add(appointmentScrollPane, BorderLayout.CENTER);
        JTable appointmentTable = new JTable();
        appointmentScrollPane.setViewportView(appointmentTable);

        JPanel appointmentButtonPanel = new JPanel();
        appointmentPanel.add(appointmentButtonPanel, BorderLayout.NORTH);

        JButton btnShowAppointments = new JButton("Show Appointments");
        appointmentButtonPanel.add(btnShowAppointments);
        btnShowAppointments.addActionListener(e -> loadAppointmentData(appointmentTable));

        JButton btnAddAppointment = new JButton("Add Appointment");
        appointmentButtonPanel.add(btnAddAppointment);
        btnAddAppointment.addActionListener(e -> openAppointmentDialog(null));

        JButton btnEditAppointment = new JButton("Edit Appointment");
        appointmentButtonPanel.add(btnEditAppointment);
        btnEditAppointment.addActionListener(e -> {
            int selectedRow = appointmentTable.getSelectedRow();
            if (selectedRow >= 0) {
                String appointmentId = appointmentTable.getValueAt(selectedRow, 0).toString();
                openAppointmentDialog(appointmentId);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an appointment to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDeleteAppointment = new JButton("Delete Appointment");
        appointmentButtonPanel.add(btnDeleteAppointment);
        btnDeleteAppointment.addActionListener(e -> {
            int selectedRow = appointmentTable.getSelectedRow();
            if (selectedRow >= 0) {
                String appointmentId = appointmentTable.getValueAt(selectedRow, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete Appointment ID: " + appointmentId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteAppointment(appointmentId);
                    loadAppointmentData(appointmentTable);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an appointment to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Optional: Confirm that the frame is initialized
        System.out.println("GUI initialized successfully.");
    }

    /**
     * Loads person data from MongoDB and displays it in the personTable.
     */
    private void loadPersonData(JTable personTable) {
        MongoDatabase db = dbConnection.getDatabase();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Person ID");
        model.addColumn("Name");
        model.addColumn("DOB");
        model.addColumn("Gender");
        model.addColumn("Address");
        model.addColumn("Phone");

        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);

        try {
            long personCount = personCollection.countDocuments();
            System.out.println("Number of persons found: " + personCount);

            for (Document personDoc : personCollection.find()) {
                Object personIdObj = personDoc.get(FIELD_ID);
                if (personIdObj == null) {
                    System.err.println("Person document has no _id.");
                    continue; // Skip if _id is missing
                }
                String personIdStr = personIdObj.toString();

                String name = personDoc.getString(FIELD_NAME);
                String dob = personDoc.getString("dob");
                String gender = personDoc.getString("gender");
                String address = personDoc.getString("address");
                String phone = personDoc.getString("phone");

                model.addRow(new Object[]{
                        personIdStr,
                        name != null ? name : "N/A",
                        dob != null ? dob : "N/A",
                        gender != null ? gender : "N/A",
                        address != null ? address : "N/A",
                        phone != null ? phone : "N/A"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading person data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        personTable.setModel(model);
        personTable.revalidate();
    }


    private void openPersonDialog(String personId) {
        JDialog dialog = new JDialog(frame, personId == null ? "Add Person" : "Edit Person", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setLocationRelativeTo(frame);

        JLabel lblName = new JLabel("Name:");
        JTextField txtName = new JTextField();
        dialog.add(lblName);
        dialog.add(txtName);

        JLabel lblDOB = new JLabel("DOB (YYYY-MM-DD):");
        JTextField txtDOB = new JTextField();
        dialog.add(lblDOB);
        dialog.add(txtDOB);

        JLabel lblGender = new JLabel("Gender (M/F):");
        JTextField txtGender = new JTextField();
        dialog.add(lblGender);
        dialog.add(txtGender);

        JLabel lblAddress = new JLabel("Address:");
        JTextField txtAddress = new JTextField();
        dialog.add(lblAddress);
        dialog.add(txtAddress);

        JLabel lblPhone = new JLabel("Phone:");
        JTextField txtPhone = new JTextField();
        dialog.add(lblPhone);
        dialog.add(txtPhone);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnSave);
        dialog.add(btnCancel);

        // If editing, load existing data
        if (personId != null) {
            loadPersonDetails(personId, txtName, txtDOB, txtGender, txtAddress, txtPhone);
        }

        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String dob = txtDOB.getText().trim();
            String gender = txtGender.getText().trim().toUpperCase();
            String address = txtAddress.getText().trim();
            String phone = txtPhone.getText().trim();

            if (name.isEmpty() || dob.isEmpty() || gender.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!gender.equals("M") && !gender.equals("F")) {
                JOptionPane.showMessageDialog(dialog, "Gender must be 'M' or 'F'.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MongoDatabase db = dbConnection.getDatabase();
            MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);

            try {
                if (personId == null) {
                    // Create new person with auto-increment integer _id
                    int newId = getNextSequenceValue("personid");
                    Document newPerson = new Document(FIELD_ID, newId)
                            .append(FIELD_NAME, name)
                            .append("dob", dob)
                            .append("gender", gender)
                            .append("address", address)
                            .append("phone", phone);
                    personCollection.insertOne(newPerson);
                    JOptionPane.showMessageDialog(dialog, "Person added successfully with ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Update existing person
                    int id = Integer.parseInt(personId);
                    Document updatedPerson = new Document(FIELD_NAME, name)
                            .append("dob", dob)
                            .append("gender", gender)
                            .append("address", address)
                            .append("phone", phone);
                    personCollection.updateOne(Filters.eq(FIELD_ID, id), new Document("$set", updatedPerson));
                    JOptionPane.showMessageDialog(dialog, "Person updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving person data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }


    private void loadPersonDetails(String personId, JTextField txtName, JTextField txtDOB, JTextField txtGender, JTextField txtAddress, JTextField txtPhone) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);
        try {
            int id = Integer.parseInt(personId);
            Document personDoc = personCollection.find(Filters.eq(FIELD_ID, id)).first();
            if (personDoc != null) {
                txtName.setText(personDoc.getString(FIELD_NAME));
                txtDOB.setText(personDoc.getString("dob"));
                txtGender.setText(personDoc.getString("gender"));
                txtAddress.setText(personDoc.getString("address"));
                txtPhone.setText(personDoc.getString("phone"));
            } else {
                JOptionPane.showMessageDialog(frame, "Person not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Person ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading person details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deletePerson(String personId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);
        try {
            int id = Integer.parseInt(personId);
            personCollection.deleteOne(Filters.eq(FIELD_ID, id));
            JOptionPane.showMessageDialog(frame, "Person deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Person ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting person.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPersonByName(String name, JTable personTable) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a name to search.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Person ID");
        model.addColumn("Name");
        model.addColumn("DOB");
        model.addColumn("Gender");
        model.addColumn("Address");
        model.addColumn("Phone");

        try {
            FindIterable<Document> results = personCollection.find(Filters.regex(FIELD_NAME, ".*" + name + ".*", "i"));
            java.util.Iterator<Document> iterator = results.iterator();
            boolean hasResults = false;

            while (iterator.hasNext()) {
                hasResults = true;
                Document personDoc = iterator.next();
                Object personIdObj = personDoc.get(FIELD_ID);
                if (personIdObj == null) {
                    continue; // Skip if _id is missing
                }
                String personIdStr = personIdObj.toString();

                String personName = personDoc.getString(FIELD_NAME);
                String dob = personDoc.getString("dob");
                String gender = personDoc.getString("gender");
                String address = personDoc.getString("address");
                String phone = personDoc.getString("phone");

                model.addRow(new Object[]{
                        personIdStr,
                        personName != null ? personName : "N/A",
                        dob != null ? dob : "N/A",
                        gender != null ? gender : "N/A",
                        address != null ? address : "N/A",
                        phone != null ? phone : "N/A"
                });
            }

            if (!hasResults) {
                JOptionPane.showMessageDialog(frame, "No persons found with the name containing: " + name, "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error searching for persons.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        personTable.setModel(model);
        personTable.revalidate();
    }

    private void loadPatientData(JTable patientTable) {
        MongoDatabase db = dbConnection.getDatabase();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Patient ID");
        model.addColumn("Name");
        model.addColumn("Emergency Contact");
        model.addColumn("Diseases");

        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);
        MongoCollection<Document> patientCollection = db.getCollection(COLLECTION_PATIENT);
        MongoCollection<Document> diseaseCollection = db.getCollection(COLLECTION_DISEASE);

        try {
            long patientCount = patientCollection.countDocuments();
            System.out.println("Number of patients found: " + patientCount);

            for (Document patientDoc : patientCollection.find()) {
                Object patientIdObj = patientDoc.get(FIELD_ID);
                if (patientIdObj == null) {
                    System.err.println("Patient document has no _id.");
                    continue; // Skip if _id is missing
                }
                String patientIdStr = patientIdObj.toString();

                Integer personId = patientDoc.getInteger(FIELD_PERSON_ID);
                if (personId == null) {
                    System.err.println("Patient with _id " + patientIdStr + " has no person_id.");
                    continue; // Skip if person_id is missing
                }

                System.out.println("Processing Patient ID: " + patientIdStr + ", Person ID: " + personId);

                Document personDoc = getDocumentById(personCollection, personId);
                if (personDoc == null) {
                    System.err.println("No person found with _id: " + personId + " for patient _id: " + patientIdStr);
                } else {
                    System.out.println("Retrieved Person: " + personDoc.toJson());
                }

                String name = (personDoc != null) ? personDoc.getString(FIELD_NAME) : "Unknown";

                // Retrieve emergency contact and handle null
                String emergencyContact = patientDoc.getString(FIELD_EMERGENCY_CONTACT);
                if (emergencyContact == null) {
                    emergencyContact = "N/A";
                }

                StringBuilder diseases = new StringBuilder();

                if (patientDoc.containsKey(FIELD_DISEASE_IDS)) {
                    List<Integer> diseaseIds = patientDoc.getList(FIELD_DISEASE_IDS, Integer.class);
                    for (Integer diseaseId : diseaseIds) {
                        Document diseaseDoc = getDocumentById(diseaseCollection, diseaseId);
                        if (diseaseDoc != null) {
                            String diseaseName = diseaseDoc.getString(FIELD_DISEASE_NAME);
                            if (diseaseName != null) {
                                diseases.append(diseaseName).append(", ");
                            } else {
                                System.err.println("Disease with _id: " + diseaseId + " has no disease_name.");
                            }
                        } else {
                            System.err.println("No disease found with _id: " + diseaseId + " for patient _id: " + patientIdStr);
                        }
                    }
                }

                model.addRow(new Object[]{
                        patientIdStr,
                        name,
                        emergencyContact,
                        (diseases.length() > 0) ? diseases.substring(0, diseases.length() - 2) : "None"
                });

                // Optional: Print to console for debugging
                System.out.println("Patient ID: " + patientIdStr + ", Name: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading patient data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        patientTable.setModel(model);
        patientTable.revalidate();
    }

    private void openPatientDialog(String patientId) {
        JDialog dialog = new JDialog(frame, patientId == null ? "Add Patient" : "Edit Patient", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setLocationRelativeTo(frame);

        JLabel lblPersonId = new JLabel("Person ID:");
        JTextField txtPersonId = new JTextField();
        dialog.add(lblPersonId);
        dialog.add(txtPersonId);

        JLabel lblEmergencyContact = new JLabel("Emergency Contact:");
        JTextField txtEmergencyContact = new JTextField();
        dialog.add(lblEmergencyContact);
        dialog.add(txtEmergencyContact);

        JLabel lblDiseaseIds = new JLabel("Disease IDs (comma-separated):");
        JTextField txtDiseaseIds = new JTextField();
        dialog.add(lblDiseaseIds);
        dialog.add(txtDiseaseIds);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnSave);
        dialog.add(btnCancel);

        // If editing, load existing data
        if (patientId != null) {
            loadPatientDetails(patientId, txtPersonId, txtEmergencyContact, txtDiseaseIds);
        }

        btnSave.addActionListener(e -> {
            String personIdStr = txtPersonId.getText().trim();
            String emergencyContact = txtEmergencyContact.getText().trim();
            String diseaseIdsStr = txtDiseaseIds.getText().trim();

            if (personIdStr.isEmpty() || emergencyContact.isEmpty() || diseaseIdsStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer personId = Integer.parseInt(personIdStr);
                // Split disease IDs and parse as integers
                String[] diseaseIdsArray = diseaseIdsStr.split(",");
                List<Integer> diseaseIds = new java.util.ArrayList<>();
                for (String id : diseaseIdsArray) {
                    diseaseIds.add(Integer.parseInt(id.trim()));
                }

                MongoDatabase db = dbConnection.getDatabase();
                MongoCollection<Document> patientCollection = db.getCollection(COLLECTION_PATIENT);

                if (patientId == null) {
                    // Create new patient
                    Document newPatient = new Document(FIELD_PERSON_ID, personId)
                            .append(FIELD_EMERGENCY_CONTACT, emergencyContact)
                            .append(FIELD_DISEASE_IDS, diseaseIds);
                    patientCollection.insertOne(newPatient);
                    JOptionPane.showMessageDialog(dialog, "Patient added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Update existing patient
                    Document updatedPatient = new Document(FIELD_PERSON_ID, personId)
                            .append(FIELD_EMERGENCY_CONTACT, emergencyContact)
                            .append(FIELD_DISEASE_IDS, diseaseIds);
                    patientCollection.updateOne(Filters.eq(FIELD_ID, Integer.parseInt(patientId)), new Document("$set", updatedPatient));
                    JOptionPane.showMessageDialog(dialog, "Patient updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Person ID and Disease IDs must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving patient data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    
    private void loadPatientDetails(String patientId, JTextField txtPersonId, JTextField txtEmergencyContact, JTextField txtDiseaseIds) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> patientCollection = db.getCollection(COLLECTION_PATIENT);
        try {
            int id = Integer.parseInt(patientId);
            Document patientDoc = patientCollection.find(Filters.eq(FIELD_ID, id)).first();
            if (patientDoc != null) {
                Integer personId = patientDoc.getInteger(FIELD_PERSON_ID);
                String emergencyContact = patientDoc.getString(FIELD_EMERGENCY_CONTACT);
                List<Integer> diseaseIds = patientDoc.getList(FIELD_DISEASE_IDS, Integer.class);

                txtPersonId.setText(personId != null ? personId.toString() : "");
                txtEmergencyContact.setText(emergencyContact != null ? emergencyContact : "");
                txtDiseaseIds.setText(diseaseIds != null ? diseaseIds.toString().replaceAll("[\\[\\]\\s]", "") : "");
            } else {
                JOptionPane.showMessageDialog(frame, "Patient not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Patient ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading patient details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deletePatient(String patientId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> patientCollection = db.getCollection(COLLECTION_PATIENT);
        try {
            int id = Integer.parseInt(patientId);
            patientCollection.deleteOne(Filters.eq(FIELD_ID, id));
            JOptionPane.showMessageDialog(frame, "Patient deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Patient ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting patient.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadDoctorData(JTable doctorTable) {
        MongoDatabase db = dbConnection.getDatabase();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Doctor ID");
        model.addColumn("Name");
        model.addColumn("Schedule");
        model.addColumn("Specialty");

        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);
        MongoCollection<Document> doctorCollection = db.getCollection(COLLECTION_DOCTORS);
        MongoCollection<Document> specialtyCollection = db.getCollection(COLLECTION_SPECIALTIES);

        try {
            long doctorCount = doctorCollection.countDocuments();
            System.out.println("Number of doctors found: " + doctorCount);

            for (Document doctorDoc : doctorCollection.find()) {
                Object doctorIdObj = doctorDoc.get(FIELD_ID);
                if (doctorIdObj == null) {
                    System.err.println("Doctor document has no _id.");
                    continue; // Skip if _id is missing
                }
                String doctorIdStr = doctorIdObj.toString(); // Convert to string regardless of actual type

                Integer personId = doctorDoc.getInteger(FIELD_PERSON_ID);
                if (personId == null) {
                    System.err.println("Doctor with _id " + doctorIdStr + " has no person_id.");
                    continue; // Skip if person_id is missing
                }

                System.out.println("Processing Doctor ID: " + doctorIdStr + ", Person ID: " + personId);

                Document personDoc = getDocumentById(personCollection, personId);
                if (personDoc == null) {
                    System.err.println("No person found with _id: " + personId + " for doctor _id: " + doctorIdStr);
                } else {
                    System.out.println("Retrieved Person: " + personDoc.toJson());
                }

                String name = (personDoc != null) ? personDoc.getString(FIELD_NAME) : "Unknown";

                // Retrieve schedule and handle null
                String schedule = doctorDoc.getString(FIELD_SCHEDULE);
                if (schedule == null) {
                    schedule = "N/A";
                }

                String specialty = "None";

                if (doctorDoc.containsKey(FIELD_SPECIALTY_ID) && doctorDoc.get(FIELD_SPECIALTY_ID) != null) {
                    Integer specialtyId = doctorDoc.getInteger(FIELD_SPECIALTY_ID);
                    Document specialtyDoc = getDocumentById(specialtyCollection, specialtyId);
                    if (specialtyDoc != null) {
                        String specialtyName = specialtyDoc.getString(FIELD_SPECIALTY_NAME);
                        if (specialtyName != null) {
                            specialty = specialtyName;
                        } else {
                            System.err.println("Specialty with _id: " + specialtyId + " has no specialty_name.");
                        }
                    } else {
                        System.err.println("No specialty found with _id: " + specialtyId + " for doctor _id: " + doctorIdStr);
                    }
                }

                model.addRow(new Object[]{
                        doctorIdStr,
                        name,
                        schedule,
                        specialty
                });

                // Optional: Print to console for debugging
                System.out.println("Doctor ID: " + doctorIdStr + ", Name: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading doctor data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        doctorTable.setModel(model);
        doctorTable.revalidate();
    }

    private void openDoctorDialog(String doctorId) {
        JDialog dialog = new JDialog(frame, doctorId == null ? "Add Doctor" : "Edit Doctor", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setLocationRelativeTo(frame);

        JLabel lblPersonId = new JLabel("Person ID:");
        JTextField txtPersonId = new JTextField();
        dialog.add(lblPersonId);
        dialog.add(txtPersonId);

        JLabel lblSchedule = new JLabel("Schedule:");
        JTextField txtSchedule = new JTextField();
        dialog.add(lblSchedule);
        dialog.add(txtSchedule);

        JLabel lblSpecialtyId = new JLabel("Specialty ID (optional):");
        JTextField txtSpecialtyId = new JTextField();
        dialog.add(lblSpecialtyId);
        dialog.add(txtSpecialtyId);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnSave);
        dialog.add(btnCancel);

        // If editing, load existing data
        if (doctorId != null) {
            loadDoctorDetails(doctorId, txtPersonId, txtSchedule, txtSpecialtyId);
        }

        btnSave.addActionListener(e -> {
            String personIdStr = txtPersonId.getText().trim();
            String schedule = txtSchedule.getText().trim();
            String specialtyIdStr = txtSpecialtyId.getText().trim();

            if (personIdStr.isEmpty() || schedule.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Person ID and Schedule are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer personId = Integer.parseInt(personIdStr);
                Integer specialtyId = null;
                if (!specialtyIdStr.isEmpty()) {
                    specialtyId = Integer.parseInt(specialtyIdStr);
                }

                MongoDatabase db = dbConnection.getDatabase();
                MongoCollection<Document> doctorCollection = db.getCollection(COLLECTION_DOCTORS);

                if (doctorId == null) {
                    // Create new doctor
                    Document newDoctor = new Document(FIELD_PERSON_ID, personId)
                            .append(FIELD_SCHEDULE, schedule);
                    if (specialtyId != null) {
                        newDoctor.append(FIELD_SPECIALTY_ID, specialtyId);
                    }
                    doctorCollection.insertOne(newDoctor);
                    JOptionPane.showMessageDialog(dialog, "Doctor added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Update existing doctor
                    int id = Integer.parseInt(doctorId);
                    Document updatedDoctor = new Document(FIELD_PERSON_ID, personId)
                            .append(FIELD_SCHEDULE, schedule);
                    if (specialtyId != null) {
                        updatedDoctor.append(FIELD_SPECIALTY_ID, specialtyId);
                    } else {
                        updatedDoctor.append(FIELD_SPECIALTY_ID, null);
                    }
                    doctorCollection.updateOne(Filters.eq(FIELD_ID, id), new Document("$set", updatedDoctor));
                    JOptionPane.showMessageDialog(dialog, "Doctor updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Person ID and Specialty ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving doctor data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void loadDoctorDetails(String doctorId, JTextField txtPersonId, JTextField txtSchedule, JTextField txtSpecialtyId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> doctorCollection = db.getCollection(COLLECTION_DOCTORS);
        try {
            int id = Integer.parseInt(doctorId);
            Document doctorDoc = doctorCollection.find(Filters.eq(FIELD_ID, id)).first();
            if (doctorDoc != null) {
                Integer personId = doctorDoc.getInteger(FIELD_PERSON_ID);
                String schedule = doctorDoc.getString(FIELD_SCHEDULE);
                Integer specialtyId = doctorDoc.getInteger(FIELD_SPECIALTY_ID);

                txtPersonId.setText(personId != null ? personId.toString() : "");
                txtSchedule.setText(schedule != null ? schedule : "");
                txtSpecialtyId.setText(specialtyId != null ? specialtyId.toString() : "");
            } else {
                JOptionPane.showMessageDialog(frame, "Doctor not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Doctor ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading doctor details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor(String doctorId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> doctorCollection = db.getCollection(COLLECTION_DOCTORS);
        try {
            int id = Integer.parseInt(doctorId);
            doctorCollection.deleteOne(Filters.eq(FIELD_ID, id));
            JOptionPane.showMessageDialog(frame, "Doctor deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Doctor ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting doctor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadStaffData(JTable staffTable) {
        MongoDatabase db = dbConnection.getDatabase();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Staff ID");
        model.addColumn("Name");
        model.addColumn("Position");
        model.addColumn("Department");

        MongoCollection<Document> personCollection = db.getCollection(COLLECTION_PERSON);
        MongoCollection<Document> staffCollection = db.getCollection(COLLECTION_STAFF);
        MongoCollection<Document> departmentCollection = db.getCollection(COLLECTION_DEPARTMENTS);

        try {
            long staffCount = staffCollection.countDocuments();
            System.out.println("Number of staff members found: " + staffCount);

            for (Document staffDoc : staffCollection.find()) {
                Object staffIdObj = staffDoc.get(FIELD_ID);
                if (staffIdObj == null) {
                    System.err.println("Staff document has no _id.");
                    continue; // Skip if _id is missing
                }
                String staffIdStr = staffIdObj.toString(); // Convert to string regardless of actual type

                Integer personId = staffDoc.getInteger(FIELD_PERSON_ID);
                if (personId == null) {
                    System.err.println("Staff with _id " + staffIdStr + " has no person_id.");
                    continue; // Skip if person_id is missing
                }

                System.out.println("Processing Staff ID: " + staffIdStr + ", Person ID: " + personId);

                Document personDoc = getDocumentById(personCollection, personId);
                if (personDoc == null) {
                    System.err.println("No person found with _id: " + personId + " for staff _id: " + staffIdStr);
                } else {
                    System.out.println("Retrieved Person: " + personDoc.toJson());
                }

                String name = (personDoc != null) ? personDoc.getString(FIELD_NAME) : "Unknown";

                // Retrieve position and handle null
                String position = staffDoc.getString(FIELD_POSITION);
                if (position == null) {
                    position = "N/A";
                }

                String department = "None";

                if (staffDoc.containsKey(FIELD_DEPARTMENT_ID) && staffDoc.get(FIELD_DEPARTMENT_ID) != null) {
                    Integer departmentId = staffDoc.getInteger(FIELD_DEPARTMENT_ID);
                    Document departmentDoc = getDocumentById(departmentCollection, departmentId);
                    if (departmentDoc != null) {
                        String departmentName = departmentDoc.getString(FIELD_DEPARTMENT_NAME);
                        if (departmentName != null) {
                            department = departmentName;
                        } else {
                            System.err.println("Department with _id: " + departmentId + " has no department_name.");
                        }
                    } else {
                        System.err.println("No department found with _id: " + departmentId + " for staff _id: " + staffIdStr);
                    }
                }

                model.addRow(new Object[]{
                        staffIdStr,
                        name,
                        position,
                        department
                });

                // Optional: Print to console for debugging
                System.out.println("Staff ID: " + staffIdStr + ", Name: " + name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading staff data.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        staffTable.setModel(model);
        staffTable.revalidate();
    }

    private void openStaffDialog(String staffId) {
        JDialog dialog = new JDialog(frame, staffId == null ? "Add Staff" : "Edit Staff", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setLocationRelativeTo(frame);

        JLabel lblPersonId = new JLabel("Person ID:");
        JTextField txtPersonId = new JTextField();
        dialog.add(lblPersonId);
        dialog.add(txtPersonId);

        JLabel lblPosition = new JLabel("Position:");
        JTextField txtPosition = new JTextField();
        dialog.add(lblPosition);
        dialog.add(txtPosition);

        JLabel lblDepartmentId = new JLabel("Department ID:");
        JTextField txtDepartmentId = new JTextField();
        dialog.add(lblDepartmentId);
        dialog.add(txtDepartmentId);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnSave);
        dialog.add(btnCancel);

        // If editing, load existing data
        if (staffId != null) {
            loadStaffDetails(staffId, txtPersonId, txtPosition, txtDepartmentId);
        }

        btnSave.addActionListener(e -> {
            String personIdStr = txtPersonId.getText().trim();
            String position = txtPosition.getText().trim();
            String departmentIdStr = txtDepartmentId.getText().trim();

            if (personIdStr.isEmpty() || position.isEmpty() || departmentIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer personId = Integer.parseInt(personIdStr);
                Integer departmentId = Integer.parseInt(departmentIdStr);

                MongoDatabase db = dbConnection.getDatabase();
                MongoCollection<Document> staffCollection = db.getCollection(COLLECTION_STAFF);

                if (staffId == null) {
                    // Create new staff
                    Document newStaff = new Document(FIELD_PERSON_ID, personId)
                            .append(FIELD_POSITION, position)
                            .append(FIELD_DEPARTMENT_ID, departmentId);
                    staffCollection.insertOne(newStaff);
                    JOptionPane.showMessageDialog(dialog, "Staff member added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Update existing staff
                    int id = Integer.parseInt(staffId);
                    Document updatedStaff = new Document(FIELD_PERSON_ID, personId)
                            .append(FIELD_POSITION, position)
                            .append(FIELD_DEPARTMENT_ID, departmentId);
                    staffCollection.updateOne(Filters.eq(FIELD_ID, id), new Document("$set", updatedStaff));
                    JOptionPane.showMessageDialog(dialog, "Staff member updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Person ID and Department ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving staff data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void loadStaffDetails(String staffId, JTextField txtPersonId, JTextField txtPosition, JTextField txtDepartmentId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> staffCollection = db.getCollection(COLLECTION_STAFF);
        try {
            int id = Integer.parseInt(staffId);
            Document staffDoc = staffCollection.find(Filters.eq(FIELD_ID, id)).first();
            if (staffDoc != null) {
                Integer personId = staffDoc.getInteger(FIELD_PERSON_ID);
                String position = staffDoc.getString(FIELD_POSITION);
                Integer departmentId = staffDoc.getInteger(FIELD_DEPARTMENT_ID);

                txtPersonId.setText(personId != null ? personId.toString() : "");
                txtPosition.setText(position != null ? position : "");
                txtDepartmentId.setText(departmentId != null ? departmentId.toString() : "");
            } else {
                JOptionPane.showMessageDialog(frame, "Staff member not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Staff ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading staff details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStaff(String staffId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> staffCollection = db.getCollection(COLLECTION_STAFF);
        try {
            int id = Integer.parseInt(staffId);
            staffCollection.deleteOne(Filters.eq(FIELD_ID, id));
            JOptionPane.showMessageDialog(frame, "Staff member deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Staff ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting staff member.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMedicalRecordData(JTable medicalRecordTable) {
        MongoDatabase db = dbConnection.getDatabase();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Medical Record ID");
        model.addColumn("Patient ID");
        model.addColumn("Appointment ID");
        model.addColumn("Diagnosis");
        model.addColumn("Treatment");

        MongoCollection<Document> medicalRecordCollection = db.getCollection(COLLECTION_MEDICAL_RECORD);

        try {
            long recordCount = medicalRecordCollection.countDocuments();
            System.out.println("Number of medical records found: " + recordCount);

            for (Document recordDoc : medicalRecordCollection.find()) {
                Object recordIdObj = recordDoc.get(FIELD_ID);
                if (recordIdObj == null) {
                    System.err.println("Medical record document has no _id.");
                    continue; // Skip if _id is missing
                }
                String recordIdStr = recordIdObj.toString();

                Integer patientId = recordDoc.getInteger("patient_id");
                Integer appointmentId = recordDoc.getInteger(FIELD_APPOINTMENT_ID);
                String diagnosis = recordDoc.getString(FIELD_DIAGNOSIS);
                String treatment = recordDoc.getString(FIELD_TREATMENT);

                model.addRow(new Object[]{
                        recordIdStr,
                        patientId != null ? patientId.toString() : "N/A",
                        appointmentId != null ? appointmentId.toString() : "N/A",
                        diagnosis != null ? diagnosis : "N/A",
                        treatment != null ? treatment : "N/A"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading medical records.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        medicalRecordTable.setModel(model);
        medicalRecordTable.revalidate();
    }


    private void openMedicalRecordDialog(String recordId) {
        JDialog dialog = new JDialog(frame, recordId == null ? "Add Medical Record" : "Edit Medical Record", true);
        dialog.setSize(500, 450);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setLocationRelativeTo(frame);

        JLabel lblPatientId = new JLabel("Patient ID:");
        JTextField txtPatientId = new JTextField();
        dialog.add(lblPatientId);
        dialog.add(txtPatientId);

        JLabel lblAppointmentId = new JLabel("Appointment ID:");
        JTextField txtAppointmentId = new JTextField();
        dialog.add(lblAppointmentId);
        dialog.add(txtAppointmentId);

        JLabel lblDiagnosis = new JLabel("Diagnosis:");
        JTextField txtDiagnosis = new JTextField();
        dialog.add(lblDiagnosis);
        dialog.add(txtDiagnosis);

        JLabel lblTreatment = new JLabel("Treatment:");
        JTextField txtTreatment = new JTextField();
        dialog.add(lblTreatment);
        dialog.add(txtTreatment);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnSave);
        dialog.add(btnCancel);

        // If editing, load existing data
        if (recordId != null) {
            loadMedicalRecordDetails(recordId, txtPatientId, txtAppointmentId, txtDiagnosis, txtTreatment);
        }

        btnSave.addActionListener(e -> {
            String patientIdStr = txtPatientId.getText().trim();
            String appointmentIdStr = txtAppointmentId.getText().trim();
            String diagnosis = txtDiagnosis.getText().trim();
            String treatment = txtTreatment.getText().trim();

            if (patientIdStr.isEmpty() || appointmentIdStr.isEmpty() || diagnosis.isEmpty() || treatment.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer patientId = Integer.parseInt(patientIdStr);
                Integer appointmentId = Integer.parseInt(appointmentIdStr);

                MongoDatabase db = dbConnection.getDatabase();
                MongoCollection<Document> medicalRecordCollection = db.getCollection(COLLECTION_MEDICAL_RECORD);

                if (recordId == null) {
                    // Create new medical record with auto-increment integer _id
                    int newId = getNextSequenceValue("medicalrecordid");
                    Document newRecord = new Document(FIELD_ID, newId)
                            .append("patient_id", patientId)
                            .append(FIELD_APPOINTMENT_ID, appointmentId)
                            .append(FIELD_DIAGNOSIS, diagnosis)
                            .append(FIELD_TREATMENT, treatment);
                    medicalRecordCollection.insertOne(newRecord);
                    JOptionPane.showMessageDialog(dialog, "Medical Record added successfully with ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Update existing medical record
                    int id = Integer.parseInt(recordId);
                    Document updatedRecord = new Document("patient_id", patientId)
                            .append(FIELD_APPOINTMENT_ID, appointmentId)
                            .append(FIELD_DIAGNOSIS, diagnosis)
                            .append(FIELD_TREATMENT, treatment);
                    medicalRecordCollection.updateOne(Filters.eq(FIELD_ID, id), new Document("$set", updatedRecord));
                    JOptionPane.showMessageDialog(dialog, "Medical Record updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Patient ID and Appointment ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving medical record data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
 
    private void loadAppointmentData(JTable appointmentTable) {
        MongoDatabase db = dbConnection.getDatabase();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Appointment ID");
        model.addColumn("Patient ID");
        model.addColumn("Doctor ID");
        model.addColumn("Date");
        model.addColumn("Time");
        model.addColumn("Status");

        MongoCollection<Document> appointmentCollection = db.getCollection(COLLECTION_APPOINTMENT);

        try {
            long appointmentCount = appointmentCollection.countDocuments();
            System.out.println("Number of appointments found: " + appointmentCount);

            for (Document appointmentDoc : appointmentCollection.find()) {
                Object appointmentIdObj = appointmentDoc.get(FIELD_ID);
                if (appointmentIdObj == null) {
                    System.err.println("Appointment document has no _id.");
                    continue; // Skip if _id is missing
                }
                String appointmentIdStr = appointmentIdObj.toString();

                Integer patientId = appointmentDoc.getInteger("patient_id");
                Integer doctorId = appointmentDoc.getInteger("doctor_id");
                String date = appointmentDoc.getString(FIELD_APPOINTMENT_DATE);
                String time = appointmentDoc.getString(FIELD_APPOINTMENT_TIME);
                String status = appointmentDoc.getString(FIELD_STATUS);

                model.addRow(new Object[]{
                        appointmentIdStr,
                        patientId != null ? patientId.toString() : "N/A",
                        doctorId != null ? doctorId.toString() : "N/A",
                        date != null ? date : "N/A",
                        time != null ? time : "N/A",
                        status != null ? status : "N/A"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading appointments.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        appointmentTable.setModel(model);
        appointmentTable.revalidate();
    }


    private void openAppointmentDialog(String appointmentId) {
        JDialog dialog = new JDialog(frame, appointmentId == null ? "Add Appointment" : "Edit Appointment", true);
        dialog.setSize(500, 500);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setLocationRelativeTo(frame);

        JLabel lblPatientId = new JLabel("Patient ID:");
        JTextField txtPatientId = new JTextField();
        dialog.add(lblPatientId);
        dialog.add(txtPatientId);

        JLabel lblDoctorId = new JLabel("Doctor ID:");
        JTextField txtDoctorId = new JTextField();
        dialog.add(lblDoctorId);
        dialog.add(txtDoctorId);

        JLabel lblAppointmentDate = new JLabel("Appointment Date (YYYY-MM-DD):");
        JTextField txtAppointmentDate = new JTextField();
        dialog.add(lblAppointmentDate);
        dialog.add(txtAppointmentDate);

        JLabel lblAppointmentTime = new JLabel("Appointment Time (HH:MM AM/PM):");
        JTextField txtAppointmentTime = new JTextField();
        dialog.add(lblAppointmentTime);
        dialog.add(txtAppointmentTime);

        JLabel lblStatus = new JLabel("Status:");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});
        dialog.add(lblStatus);
        dialog.add(cbStatus);

        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        dialog.add(btnSave);
        dialog.add(btnCancel);

        // If editing, load existing data
        if (appointmentId != null) {
            loadAppointmentDetails(appointmentId, txtPatientId, txtDoctorId, txtAppointmentDate, txtAppointmentTime, cbStatus);
        }

        btnSave.addActionListener(e -> {
            String patientIdStr = txtPatientId.getText().trim();
            String doctorIdStr = txtDoctorId.getText().trim();
            String appointmentDate = txtAppointmentDate.getText().trim();
            String appointmentTime = txtAppointmentTime.getText().trim();
            String status = (String) cbStatus.getSelectedItem();

            if (patientIdStr.isEmpty() || doctorIdStr.isEmpty() || appointmentDate.isEmpty() || appointmentTime.isEmpty() || status.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer patientId = Integer.parseInt(patientIdStr);
                Integer doctorId = Integer.parseInt(doctorIdStr);

                MongoDatabase db = dbConnection.getDatabase();
                MongoCollection<Document> appointmentCollection = db.getCollection(COLLECTION_APPOINTMENT);

                if (appointmentId == null) {
                    // Create new appointment with auto-increment integer _id
                    int newId = getNextSequenceValue("appointmentid");
                    Document newAppointment = new Document(FIELD_ID, newId)
                            .append("patient_id", patientId)
                            .append("doctor_id", doctorId)
                            .append(FIELD_APPOINTMENT_DATE, appointmentDate)
                            .append(FIELD_APPOINTMENT_TIME, appointmentTime)
                            .append(FIELD_STATUS, status);
                    appointmentCollection.insertOne(newAppointment);
                    JOptionPane.showMessageDialog(dialog, "Appointment added successfully with ID: " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Update existing appointment
                    int id = Integer.parseInt(appointmentId);
                    Document updatedAppointment = new Document("patient_id", patientId)
                            .append("doctor_id", doctorId)
                            .append(FIELD_APPOINTMENT_DATE, appointmentDate)
                            .append(FIELD_APPOINTMENT_TIME, appointmentTime)
                            .append(FIELD_STATUS, status);
                    appointmentCollection.updateOne(Filters.eq(FIELD_ID, id), new Document("$set", updatedAppointment));
                    JOptionPane.showMessageDialog(dialog, "Appointment updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Patient ID and Doctor ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving appointment data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void loadAppointmentDetails(String appointmentId, JTextField txtPatientId, JTextField txtDoctorId, JTextField txtAppointmentDate, JTextField txtAppointmentTime, JComboBox<String> cbStatus) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> appointmentCollection = db.getCollection(COLLECTION_APPOINTMENT);
        try {
            int id = Integer.parseInt(appointmentId);
            Document appointmentDoc = appointmentCollection.find(Filters.eq(FIELD_ID, id)).first();
            if (appointmentDoc != null) {
                Integer patientId = appointmentDoc.getInteger("patient_id");
                Integer doctorId = appointmentDoc.getInteger("doctor_id");
                String date = appointmentDoc.getString(FIELD_APPOINTMENT_DATE);
                String time = appointmentDoc.getString(FIELD_APPOINTMENT_TIME);
                String status = appointmentDoc.getString(FIELD_STATUS);

                txtPatientId.setText(patientId != null ? patientId.toString() : "");
                txtDoctorId.setText(doctorId != null ? doctorId.toString() : "");
                txtAppointmentDate.setText(date != null ? date : "");
                txtAppointmentTime.setText(time != null ? time : "");
                cbStatus.setSelectedItem(status != null ? status : "Scheduled");
            } else {
                JOptionPane.showMessageDialog(frame, "Appointment not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Appointment ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading appointment details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteAppointment(String appointmentId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> appointmentCollection = db.getCollection(COLLECTION_APPOINTMENT);
        try {
            int id = Integer.parseInt(appointmentId);
            appointmentCollection.deleteOne(Filters.eq(FIELD_ID, id));
            JOptionPane.showMessageDialog(frame, "Appointment deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Appointment ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting appointment.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadMedicalRecordDetails(String recordId, JTextField txtPatientId, JTextField txtAppointmentId, JTextField txtDiagnosis, JTextField txtTreatment) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> medicalRecordCollection = db.getCollection(COLLECTION_MEDICAL_RECORD);
        try {
            int id = Integer.parseInt(recordId);
            Document recordDoc = medicalRecordCollection.find(Filters.eq(FIELD_ID, id)).first();
            if (recordDoc != null) {
                Integer patientId = recordDoc.getInteger("patient_id");
                Integer appointmentId = recordDoc.getInteger(FIELD_APPOINTMENT_ID);
                String diagnosis = recordDoc.getString(FIELD_DIAGNOSIS);
                String treatment = recordDoc.getString(FIELD_TREATMENT);

                txtPatientId.setText(patientId != null ? patientId.toString() : "");
                txtAppointmentId.setText(appointmentId != null ? appointmentId.toString() : "");
                txtDiagnosis.setText(diagnosis != null ? diagnosis : "");
                txtTreatment.setText(treatment != null ? treatment : "");
            } else {
                JOptionPane.showMessageDialog(frame, "Medical Record not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Medical Record ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading medical record details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteMedicalRecord(String recordId) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> medicalRecordCollection = db.getCollection(COLLECTION_MEDICAL_RECORD);
        try {
            int id = Integer.parseInt(recordId);
            medicalRecordCollection.deleteOne(Filters.eq(FIELD_ID, id));
            JOptionPane.showMessageDialog(frame, "Medical Record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid Medical Record ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting medical record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private Document getDocumentById(MongoCollection<Document> collection, Object id) {
        if (id == null) return null;
        return collection.find(Filters.eq(FIELD_ID, id)).first();
    }


    private int getNextSequenceValue(String sequenceName) {
        MongoDatabase db = dbConnection.getDatabase();
        MongoCollection<Document> counters = db.getCollection(COLLECTION_COUNTERS);

        Document filter = new Document("_id", sequenceName);
        Document update = new Document("$inc", new Document("sequence_value", 1));

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(true);

        Document updatedCounter = counters.findOneAndUpdate(filter, update, options);
        if (updatedCounter != null && updatedCounter.containsKey("sequence_value")) {
            return updatedCounter.getInteger("sequence_value");
        } else {
            throw new RuntimeException("Failed to get sequence value for " + sequenceName);
        }
    }
}


class MongoDBConnection {
    private MongoClient mongoClient;
    private MongoDatabase database;


    public MongoDBConnection() {
        try {
            // Ensure the connection string matches your MongoDB setup
            mongoClient = MongoClients.create("mongodb://localhost:27017/");
            database = mongoClient.getDatabase("Hospital");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to initialize MongoDB connection.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Returns the MongoDatabase instance.
     *
     * @return The MongoDatabase.
     */
    public MongoDatabase getDatabase() {
        return database;
    }

    /**
     * Tests the MongoDB connection by attempting to list collection names.
     *
     * @return true if the connection is successful, false otherwise.
     */
    public boolean testConnection() {
        try {
            database.listCollectionNames().first(); // Test connection
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
