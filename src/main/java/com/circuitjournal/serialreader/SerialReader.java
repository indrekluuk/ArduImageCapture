package com.circuitjournal.serialreader;

import java.util.List;

public interface SerialReader {

    interface SerialDataReceived {
        void serialDataReceived(byte [] bytes);
    }

    void setReceivedDataHandler(SerialDataReceived callback);

    List<String> getAvailablePorts();
    List<Integer> getAvailableBaudRates();
    Integer getDefaultBaudRate(Integer overrideBaudRate);

    void startListening(String portName, Integer baudRate);
    void stopListening();
    boolean isListening();

}
