package com.circuitjournal.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropertiesFileSettings extends Settings {


    private Properties prop = new Properties();
    private File file;

    public PropertiesFileSettings(File file) {
        if (file != null) {
            this.file = file;
            try {
                prop.load(new FileInputStream(file));
            } catch (Exception e) {
                System.err.println("Reading properties file failed!");
                e.printStackTrace(System.err);
            }

        }
    }

    @Override
    protected String getParameter(String parameterName) {
        return (String)prop.get(parameterName);
    }

    @Override
    protected void saveParameter(String parameterName, String value) {
        prop.put(parameterName, value);
        if (file != null) {
            try {
                prop.store(new FileOutputStream(file), null);
            } catch (Exception e) {
                System.err.println("Saving properties file failed!");
                e.printStackTrace(System.err);
            }
        }
    }

}
