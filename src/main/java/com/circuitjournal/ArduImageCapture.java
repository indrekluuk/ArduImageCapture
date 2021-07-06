package com.circuitjournal;

import com.circuitjournal.serialreader.JSerialCommSerialReader;
import com.circuitjournal.serialreader.SerialReader;
import com.circuitjournal.settings.ArduinoPreferencesSettings;
import com.circuitjournal.settings.Settings;
import processing.app.Editor;
import processing.app.tools.Tool;
import processing.app.PreferencesData;

/*
    Arduino Tool plugin implementation
 */
public class ArduImageCapture implements Tool {

    private Editor editor;


    public void init(Editor editor) {
        this.editor = editor;
    }


    public void run() {

        Settings settings = new ArduinoPreferencesSettings();

        //editor.statusNotice("ArduImageCapture started!");
        SerialReader serialReader = new JSerialCommSerialReader();
        MainWindow mainWindow = new MainWindow(editor.getContentPane(), serialReader, settings);

        String selectedPort = PreferencesData.get("serial.port");
        mainWindow.setSelectedComPort(selectedPort);

        editor.addCompilerProgressListener((donePercentage)->{
            if (donePercentage == 100) {
                if (mainWindow.isListening()) {
                    mainWindow.stopListening();
                    editor.statusError("Compilation done. Disconnecting ArduImageCapture.");
                }
            }
        });
    }


    public String getMenuTitle() {
        return "ArduImageCapture";
    }

}
