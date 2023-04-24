package org.example.er2petriflow;

import org.example.er2petriflow.er.Converter;
import org.example.er2petriflow.er.Exporter;
import org.example.er2petriflow.er.Importer;
import org.example.er2petriflow.er.ImporterSql;
import org.example.er2petriflow.er.domain.ERDiagram;
import org.example.er2petriflow.generated.petriflow.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
import java.util.Optional;


public class ER2PetriflowGUI extends JFrame {

    private static final String DEFAULT_OUTPUT_NAME = "out.zip";


    private JTextField inputField;
    private JTextField outputField;
    private JTextArea inputTextArea;
    private JButton inputBrowseButton;
    private JButton outputBrowseButton;
    private JButton convertButton;
    private JComboBox<String> inputTypeComboBox;


    public ER2PetriflowGUI() {
        Font customFont = new Font("Segoe UI", Font.PLAIN, 12);
        for (Object key : UIManager.getDefaults().keySet()) {
            if (key.toString().toLowerCase().contains("font")) {
                UIManager.put(key, customFont);
            }
        }

        setTitle("ER2Petriflow Converter");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initComponents();
        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        inputField = new JTextField();
        outputField = new JTextField();
        inputTextArea = new JTextArea();
        inputBrowseButton = new JButton("Browse");
        outputBrowseButton = new JButton("Browse");
        convertButton = new JButton("Convert");
        inputTypeComboBox = new JComboBox<>(new String[]{"sql", "erdplus"});

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Input File:"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(inputBrowseButton, BorderLayout.EAST);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(new JLabel("Output File:"), BorderLayout.WEST);
        outputPanel.add(outputField, BorderLayout.CENTER);
        outputPanel.add(outputBrowseButton, BorderLayout.EAST);


        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(inputPanel);
        topPanel.add(outputPanel);

        JScrollPane textAreaScrollPane = new JScrollPane(inputTextArea);

        JPanel botPanel = new JPanel(new GridLayout(1, 2));

        botPanel.add(inputTypeComboBox, BorderLayout.WEST);
        botPanel.add(convertButton, BorderLayout.EAST);


        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        container.add(topPanel, BorderLayout.NORTH);
        container.add(textAreaScrollPane, BorderLayout.CENTER);
        container.add(botPanel, BorderLayout.SOUTH);

        add(container);

        inputBrowseButton.addActionListener(new InputBrowseButtonListener());
        outputBrowseButton.addActionListener(new OutputBrowseButtonListener());
        convertButton.addActionListener(new ConvertButtonListener());
    }



    private class InputBrowseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(ER2PetriflowGUI.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                inputField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private class OutputBrowseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showSaveDialog(ER2PetriflowGUI.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                outputField.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    private class ConvertButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                String inputText = null;
                FileInputStream inputStream = null;

                if (!inputField.getText().isEmpty()) {
                    inputStream = new FileInputStream(String.valueOf(inputField.getText()));
                } else {
                    inputText = inputTextArea.getText();
                }

                File output = new File(outputField.getText());
                if (output.isDirectory()) {
                    output = new File(output.getPath() + File.separator + DEFAULT_OUTPUT_NAME);
                }
                FileOutputStream outputStream = new FileOutputStream(output);



                Optional<ERDiagram> diagram = null;


                if (inputText != null){
                    String extension = (String) inputTypeComboBox.getSelectedItem();

                    if (extension.equals("sql")) {
                        ImporterSql importerSql = new ImporterSql();
                        diagram = importerSql.convert(inputText);
                    } else{
                        Importer importer = new Importer();
                        diagram = importer.importDiagram(inputText);
                    }
                }
                else if (inputStream != null){
                    String extension = inputField.getText().substring(inputField.getText().lastIndexOf('.') + 1);

                    if (extension.equals("sql")) {
                        ImporterSql importerSql = new ImporterSql();
                        diagram = importerSql.convert(inputStream);
                    } else {
                        Importer importer = new Importer();
                        diagram = importer.importDiagram(inputStream);
                    }

                    closeStream(inputStream);
                }

                if (diagram.isEmpty()) {
                    throw new RuntimeException("The diagram file could not be read!");
                }

                Converter converter = new Converter();
                List<Document> petriflows = converter.convertToPetriflows(diagram.get());

                Exporter exporter = new Exporter();
                try {
                    exporter.exportToZip(outputStream, petriflows);
                } catch (JAXBException ex) {
                    throw new RuntimeException("Petriflows could not be serialized into XML!", ex);
                } catch (IOException ex) {
                    throw new RuntimeException("An error has occurred while writing the output file.", ex);
                } finally {
                    closeStream(outputStream);
                }



                JOptionPane.showMessageDialog(ER2PetriflowGUI.this, "Conversion completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ER2PetriflowGUI.this, "An error occurred during conversion:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ER2PetriflowGUI er2PetriflowGUI = new ER2PetriflowGUI();
            er2PetriflowGUI.setVisible(true);
        });
    }

    private static void closeStream(InputStream s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeStream(OutputStream s) {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
