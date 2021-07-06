package com.circuitjournal.settings;

import processing.app.PreferencesData;

public class ArduinoPreferencesSettings extends Settings {

    @Override
    protected String getParameter(String parameterName) {
        return PreferencesData.get(parameterName);
    }

    @Override
    protected void saveParameter(String parameterName, String value) {
        PreferencesData.set(parameterName, value);
    }

}
