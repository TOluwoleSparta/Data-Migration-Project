package com.sparta;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.sql.*;
import java.text.SimpleDateFormat;

public class DataMigrator {
    private final int NUMBER_OF_FIELDS = 10;

    private ArrayList<String[]> dataRecords;
    private ArrayList<String[]> corruptedDataRecords;
    private ArrayList<String[]> duplicateDataRecords;

    private long readTime;
    private long uploadTime;
    private long totalTime;

    public DataMigrator() {
        dataRecords = new ArrayList<>();
        corruptedDataRecords = new ArrayList<>();
        duplicateDataRecords = new ArrayList<>();
    }

    public void migrateData() {
        long totalStartTime = System.nanoTime();
        readCSVFile();
        uploadRecordsToDatabase();
        writeCorruptedRecordsFile();
        writeDuplicateRecordsFile();
        totalTime = System.nanoTime() - totalStartTime;
        showTimes();
    }

    public void readCSVFile() {
        BufferedReader bufferedReader;
        FileReader fileReader;
        String[] record;
        String line;

        long readStartTime = System.nanoTime();

        try {
            fileReader = new FileReader("resources/employees.csv");
            bufferedReader = new BufferedReader(fileReader);
        } catch (Exception e) {
            System.out.println("Error: File not found. Please ensure the correct file has been stated.#.");
            return;
        }

        try {
            bufferedReader.readLine();  // reads header from .csv file first
        } catch (Exception e) {
            System.out.println("Error: Problem with reading file header.");
            return;
        }

        try {
            while ((line = bufferedReader.readLine()) != null) {
                record = line.split(",");

                if (isRecordValid(record)) {
                    dataRecords.add(record);
                } else {
                    corruptedDataRecords.add(record);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Problem with reading from file.");
            return;
        }

        try {
            bufferedReader.close();
        } catch (Exception e) {
            System.out.println("Error: Problem trying to close the file.");
        }

        readTime = System.nanoTime();
    }

    private boolean isRecordValid(String[] record) {
        String emailAddressRegex        = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$";
        String honorificsRegex          = "(Dr.|Drs.|Hon.|Mr.|Mrs.|Ms.|Prof.)";
        String nameRegex                = "^[a-zA-Z\\u00C0-\\u024F\\u1E00-\\u1EFF]+[- ]?[a-zA-Z\\u00C0-\\u024F\\u1E00-\\u1EFF]+$"; // including accented letters, spaces and hyphens
        SimpleDateFormat dateFormat     = new SimpleDateFormat("dd/MM/yyyy");

        if (record.length != NUMBER_OF_FIELDS) {
            if (record.length > NUMBER_OF_FIELDS) {
                record = truncateRecord(record);
            }
            return false;
        } else {
            // parsing an int from the Employee ID entry
            try {
                Integer.parseInt(record[0]);
            } catch (Exception e) {
                return false;
            }

            // see if honorific is valid
            if (!record[1].matches(honorificsRegex)) {
                return false;
            }

            // see if first name is valid name
            if (!record[2].matches(nameRegex)) {
                return false;
            }

            // see if initial is only one letter
            if (!record[3].matches("^[A-Z]") || record[3].length() != 1) {
                return false;
            }

            // see if last name is valid name
            if (!record[4].matches(nameRegex)) {
                return false;
            }

            // see if gender is either M or F
            if (!record[5].equals("M") && !record[5].equals("F")) {
                return false;
            }

            // see if email is of valid format
            if (!record[6].matches(emailAddressRegex)) {
                return false;
            }

            // parsing a date from the date of birth entry
            try {
                dateFormat.parse(record[7]);
            } catch (Exception e) {
                return false;
            }

            // parsing a date from the start date entry
            try {
                dateFormat.parse(record[8]);
            } catch (Exception e) {
                return false;
            }

            // parsing a float from the salary entry
            try {
                Float.parseFloat(record[9]);
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }

    private String[] truncateRecord(String[] record) {
        // appends the extra record fields to the 10th one for printing to the corrupted file
        for (int index = 10; index < record.length; index++) {
            record[9] += ", " + record[index];
        }
        // removes the extra fields
        return Arrays.copyOf(record, NUMBER_OF_FIELDS);
    }

    private void uploadRecordsToDatabase() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Properties properties = new Properties();
        PreparedStatement preparedStatement;
        Connection connection;
        String sqlStatement;

        long uploadStartTime = System.nanoTime();

        try {
            properties.load(new FileReader("resources/login.properties"));
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mainschema?serverTimezone=GMT",
                    properties.getProperty("username"),
                    properties.getProperty("password"));

            for (String[] dataRecord : dataRecords) {
                try {
                    sqlStatement = "INSERT INTO Employees VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                    preparedStatement = connection.prepareStatement(sqlStatement);
                    preparedStatement.setInt(1, Integer.parseInt(dataRecord[0]));                        // ID
                    preparedStatement.setString(2, dataRecord[1]);                                       // Honorific
                    preparedStatement.setString(3, dataRecord[2]);                                       // First Name
                    preparedStatement.setString(4, dataRecord[3]);                                       // Middle Initial
                    preparedStatement.setString(5, dataRecord[4]);                                       // Last Name
                    preparedStatement.setString(6, dataRecord[5]);                                       // Gender
                    preparedStatement.setString(7, dataRecord[6]);                                       // E-Mail
                    preparedStatement.setDate(8, new Date(dateFormat.parse(dataRecord[7]).getTime()));   // Date Of Birth
                    preparedStatement.setDate(9, new Date(dateFormat.parse(dataRecord[8]).getTime()));   // Start Date
                    preparedStatement.setFloat(10, Float.parseFloat(dataRecord[9]));                     // Salary
                    preparedStatement.execute();
                } catch (SQLIntegrityConstraintViolationException s) {
                    duplicateDataRecords.add(dataRecord);
                }
            }
            connection.close();

        } catch (Exception e) {
            System.out.println("Error: cannot establish connection with the database.");
        }

        uploadTime = System.nanoTime() - uploadStartTime;
    }

    private void writeCorruptedRecordsFile() {
        if (corruptedDataRecords.size() > 0) {
            try {
                String fileName = "outputs/Corrupted Employee Records.csv";
                FileWriter fileWriter = new FileWriter(fileName);
                String dataString;

                fileWriter.write("== CORRUPTED RECORDS [" + corruptedDataRecords.size() + "] ==\n");
                for (String[] record : corruptedDataRecords) {
                    dataString = String.format("%-8s %-8s %-12s %-4s %-16s %-4s %-36s %-12s %-12s %s\n",
                            ((record.length > 0) ? record[0] + "," : ""),
                            ((record.length > 1) ? record[1] + "," : ""),
                            ((record.length > 2) ? record[2] + "," : ""),
                            ((record.length > 3) ? record[3] + "," : ""),
                            ((record.length > 4) ? record[4] + "," : ""),
                            ((record.length > 5) ? record[5] + "," : ""),
                            ((record.length > 6) ? record[6] + "," : ""),
                            ((record.length > 7) ? record[7] + "," : ""),
                            ((record.length > 8) ? record[8] + "," : ""),
                            ((record.length > 9) ? record[9] : ""));

                    fileWriter.write(dataString);
                }

                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error: There has been an error in writing to the file.");
            }
        }
    }

    private void writeDuplicateRecordsFile() {
        if (duplicateDataRecords.size() > 0) {
            try {
                String fileName = "outputs/Duplicate Employee Records.csv";
                FileWriter fileWriter = new FileWriter(fileName);
                String dataString;

                fileWriter.write("== DUPLICATE RECORDS [" + duplicateDataRecords.size() + "] ==\n");
                for (String[] record : duplicateDataRecords) {
                    dataString = String.format("%-8s %-8s %-12s %-4s %-16s %-4s %-36s %-12s %-12s %s\n",
                            record[0] + ",",
                            record[1] + ",",
                            record[2] + ",",
                            record[3] + ",",
                            record[4] + ",",
                            record[5] + ",",
                            record[6] + ",",
                            record[7] + ",",
                            record[8] + ",",
                            record[9]);

                    fileWriter.write(dataString);
                }

                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error: There has been an error in writing to the file.");
            }
        }
    }

    private void showTimes() {
        System.out.println("The program took " + (((double) readTime) / 1_000_000.0) + "milliseconds to read the records from the .csv file.");
        System.out.println("The program took " + (((double) uploadTime) / 1_000_000.0) + "milliseconds to upload the records to the database.");
        System.out.println("The program took " + (((double) totalTime) / 1_000_000.0) + "milliseconds in total.");
    }
}


