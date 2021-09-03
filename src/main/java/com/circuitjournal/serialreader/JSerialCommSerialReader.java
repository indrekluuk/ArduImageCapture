package com.circuitjournal.serialreader;
/**
 * Created by indrek on 1.05.2016.
 */

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.InputStream;
import java.io.OutputStream;

import java.util.*;



public class JSerialCommSerialReader implements SerialReader, SerialPortDataListener {

  private SerialPort serialPort;
  private InputStream serialInput;
  private OutputStream serialOutput;

  private static final int TIME_OUT = 2000;

  public static final int BAUD_2000000 = 2000000;
  public static final int BAUD_1000000 = 1000000;
  public static final int BAUD_500000 = 500000;
  public static final int BAUD_250000 = 250000;
  public static final int BAUD_230400 = 230400;
  public static final int BAUD_115200 = 115200;
  public static final int BAUD_57600 = 57600;
  public static final int BAUD_38400 = 38400;
  public static final int BAUD_19200 = 19200;
  public static final int BAUD_9600 = 9600;

  private List<Integer> baudRateList = Arrays.asList(
      BAUD_2000000,
      BAUD_1000000,
      BAUD_500000,
      BAUD_250000,
      BAUD_230400,
      BAUD_115200/*,
      BAUD_57600,
      BAUD_38400,
      BAUD_19200,
      BAUD_9600*/);



  private SerialDataReceived serialReceivedCallback;


  public JSerialCommSerialReader() {
  }


  @Override
  public void setReceivedDataHandler(SerialDataReceived callback) {
    serialReceivedCallback = callback;
  }


  public void startListening(String portName, Integer baudRate) {
    SerialPort serialPort = getSerialPorts().get(portName);
    if (serialPort == null) {
      throw new SerialReaderException("'" + portName + "' not found");
    } else {
      openPort(serialPort, baudRate);
    }
  }



  private synchronized void openPort(
          SerialPort openSerialPort,
          Integer baudRate
  ) {
    try {
      stopListening();

      serialPort = openSerialPort;
      serialPort.openPort();

      serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, TIME_OUT, TIME_OUT);
      serialPort.setComPortParameters(
              baudRate,
              8,
              SerialPort.ONE_STOP_BIT,
              SerialPort.NO_PARITY);

      serialInput = serialPort.getInputStream();
      serialOutput = serialPort.getOutputStream();

      serialPort.addDataListener(this);
    } catch (Exception e) {
      throw new SerialReaderException("Connect failed " + e.getMessage());
    }
  }


  @Override
  public int getListeningEvents() {
    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
  }

  @Override
  public synchronized void serialEvent(SerialPortEvent oEvent) {
    if (oEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
      try {
        if (serialReceivedCallback != null) {
          int byteCount = serialInput.available();
          if (byteCount > 0) {
            byte [] data = new byte[byteCount];
            serialInput.read(data, 0, byteCount);
            serialReceivedCallback.serialDataReceived(data);
          }
        }
      } catch (Exception e) {
        e.printStackTrace(System.err);
      }
    }
  }


  public synchronized void stopListening() {
    if (serialPort != null) {
      serialPort.removeDataListener();
      serialPort.closePort();
      serialPort = null;
    }
  }

  public synchronized boolean isListening() {
    return serialPort != null;
  }


  public List<String> getAvailablePorts() {
    List<String> ports = new ArrayList<>(getSerialPorts().keySet());
    Collections.reverse(ports);
    return ports;
  }

  public List<Integer> getAvailableBaudRates() {
    return baudRateList;
  }

  public Integer getDefaultBaudRate(Integer overrideBaudRate) {
    if (overrideBaudRate != null && baudRateList.contains(overrideBaudRate)) {
      return overrideBaudRate;
    } else {
      return BAUD_500000;
    }
  }

  private Map<String, SerialPort> getSerialPorts() {
    Map<String, SerialPort> portIdentifierMap = new LinkedHashMap<>();
    SerialPort [] serialPorts = SerialPort.getCommPorts();
    for(SerialPort serialPort : serialPorts) {
      portIdentifierMap.put(serialPort.getSystemPortName(), serialPort);
    }
    return portIdentifierMap;
  }



}