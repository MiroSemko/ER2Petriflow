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
    private JTextField outputFieldTab2;
    private JTextArea inputTextArea;
    private JComboBox<String> inputTypeComboBox;
    private JTabbedPane tabbedPane;


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
        setSize(650, 400);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        inputField = new JTextField();
        outputField = new JTextField();
        outputFieldTab2 = new JTextField();
        inputTextArea = new JTextArea();
        JButton inputBrowseButton = new JButton("Browse");
        JButton outputBrowseButton = new JButton("Browse");
        JButton outputBrowseButtonTab2 = new JButton("Browse");
        JButton convertButton = new JButton("Convert");
        JButton convertButtonTab2 = new JButton("Convert");
        inputTypeComboBox = new JComboBox<>(new String[]{"sql", "erdplus"});

        //Tab 1
        JPanel fileInputOutputPanel = new JPanel();
        fileInputOutputPanel.setLayout(new BoxLayout(fileInputOutputPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.add(new JLabel("Input File: "));
        fileInputOutputPanel.add(inputPanel);

        JPanel inputAreaButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputField.setColumns(40);
        inputAreaButtonPanel.add(inputField);
        inputAreaButtonPanel.add(inputBrowseButton);
        fileInputOutputPanel.add(inputAreaButtonPanel);

        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outputPanel.add(new JLabel("Output File: "));
        fileInputOutputPanel.add(outputPanel);

        JPanel outputAreaButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outputField.setColumns(40);
        outputAreaButtonPanel.add(outputField);
        outputAreaButtonPanel.add(outputBrowseButton);
        fileInputOutputPanel.add(outputAreaButtonPanel);

        JPanel convertButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        convertButtonPanel.add(convertButton);
        fileInputOutputPanel.add(convertButtonPanel);

        //Tab 2
        JPanel secondTab = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel inputPanelTab2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanelTab2.add(new JLabel("Input Text Type:"));
        inputPanelTab2.add(inputTypeComboBox);

        inputTextArea.setRows(12);
        inputTextArea.setColumns(50);
        JScrollPane textAreaScrollPane = new JScrollPane(inputTextArea);
        JPanel textAreaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textAreaPanel.add(textAreaScrollPane);

        JPanel outputPanelTab2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outputPanelTab2.add(new JLabel("Output File:"));
        outputFieldTab2.setColumns(40);
        outputPanelTab2.add(outputFieldTab2);
        outputPanelTab2.add(outputBrowseButtonTab2);

        secondTab.add(inputPanelTab2);
        secondTab.add(textAreaPanel);
        secondTab.add(outputPanelTab2);
        secondTab.add(convertButtonTab2);


        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("File Input/Output", fileInputOutputPanel);
        tabbedPane.addTab("Text Input", secondTab);

        add(tabbedPane);

        inputBrowseButton.addActionListener(new InputBrowseButtonListener());
        outputBrowseButton.addActionListener(new OutputBrowseButtonListener());
        convertButton.addActionListener(new ConvertButtonListener());
        outputBrowseButtonTab2.addActionListener(new OutputBrowseButtonListener());
        convertButtonTab2.addActionListener(new ConvertButtonListener());
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
                outputFieldTab2.setText(selectedFile.getAbsolutePath());

            }
        }
    }

    private class ConvertButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                int selectedTabIndex = tabbedPane.getSelectedIndex();

                Optional<ERDiagram> diagram;
                if (selectedTabIndex == 0) {
//                    FileInputStream inputStream = new FileInputStream(String.valueOf(inputField.getText()));
                    FileInputStream inputStream;
                    try {
                        inputStream = new FileInputStream(String.valueOf(inputField.getText()));
                    } catch (FileNotFoundException exce) {
                        throw new FileNotFoundException("Input file located at '" + inputField.getText() + "' could not be found!");
                    }

                    String extension = inputField.getText().substring(inputField.getText().lastIndexOf('.') + 1);

                    if (extension.equals("sql")) {
                        ImporterSql importerSql = new ImporterSql();
                        diagram = importerSql.convert(inputStream);
                    } else {
                        Importer importer = new Importer();
                        diagram = importer.importDiagram(inputStream);
                    }
                    closeStream(inputStream);
                } else {
                    String inputText = inputTextArea.getText();

                    String extension = (String) inputTypeComboBox.getSelectedItem();

                    if (extension.equals("sql")) {
                        ImporterSql importerSql = new ImporterSql();
                        diagram = importerSql.convert(inputText);
                    } else{
                        Importer importer = new Importer();
                        diagram = importer.importDiagram(inputText);
                    }
                }

                String outputPath = "." + File.separator;
                if(!outputField.getText().isEmpty()){
                    outputPath = outputField.getText();
                }

                File output = new File(outputPath);
                if (output.isDirectory()) {
                    output = new File(output.getPath() + File.separator + DEFAULT_OUTPUT_NAME);
                }
//                FileOutputStream outputStream = new FileOutputStream(output);
                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(output);
                } catch (FileNotFoundException exc) {
                    throw new FileNotFoundException("Output file located at '" + output.getPath() + "' could not be found!");
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
