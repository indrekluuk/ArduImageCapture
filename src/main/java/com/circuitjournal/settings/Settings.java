package com.circuitjournal.settings;


import com.circuitjournal.serialreader.JSerialCommSerialReader;

public abstract class Settings {

    String BAUD_RATE = "ArduImageCapture.default.baudRate";
    String SAVE_FOLDER = "ArduImageCapture.default.saveFolder";

    public Integer getDefaultBaudRate() {
        try {
            return Integer.parseInt(getParameter(BAUD_RATE));
        } catch (NumberFormatException e) {
            return JSerialCommSerialReader.BAUD_500000;
        }
    }

    public void saveDefaultBaudRate(Integer baudRate) {
        saveParameter(BAUD_RATE, baudRate.toString());
    }

    public String getDefaultSaveFolder() {
        return getParameter(SAVE_FOLDER);
    }

    public void saveDefaultSaveFolder(String folder) {
        saveParameter(SAVE_FOLDER, folder);
    }

    protected abstract String getParameter(String parameterName);
    protected abstract void saveParameter(String parameterName, String value);
}
