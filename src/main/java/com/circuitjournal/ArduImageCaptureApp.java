package com.circuitjournal;

import com.circuitjournal.serialreader.JSerialCommSerialReader;
import com.circuitjournal.serialreader.SerialReader;
import com.circuitjournal.settings.PropertiesFileSettings;
import com.circuitjournal.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.io.File;

public class ArduImageCaptureApp {


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Setting \"look and feel\" failed!");
            e.printStackTrace(System.err);
        }

        String propertiesFilePath = getPropertiesFilePath();
        File propertiesFile = initPropertiesFile(propertiesFilePath);

        Settings settings = new PropertiesFileSettings(propertiesFile);


        SerialReader serialReader = new JSerialCommSerialReader();
        MainWindow window = new MainWindow(null, serialReader, settings);
        //window.showMessage(propertiesFilePath);
        window.setExitOnClose();
    }


    private static String getPropertiesFilePath() {
        try {
            //String currentDir = System.getProperty("user.dir");
            File classLocation = new File(ArduImageCaptureApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classLocation.isFile()) {
                classLocation = classLocation.getParentFile();
            }
            return classLocation.getPath() + File.separator + "ArduImageCapture.properties";
        } catch (Exception e) {
            System.err.println("Getting path for properties file failed!");
            e.printStackTrace(System.err);
            return null;
        }
    }

    private static File initPropertiesFile(String propertiesFilePath) {
        if (StringUtils.isBlank(propertiesFilePath)) {
            return null;
        }

        try {
            File propertiesFile = new File(propertiesFilePath);
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            } else if (!propertiesFile.canRead()) {
                propertiesFile = null;
            }
            return propertiesFile;
        } catch (Exception e) {
            System.err.println("Initializing properties file failed!");
            e.printStackTrace(System.err);
            return null;
        }
    }



}
