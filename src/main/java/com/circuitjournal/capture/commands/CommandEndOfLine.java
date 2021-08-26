package com.circuitjournal.capture.commands;

import com.circuitjournal.capture.ImageCapture;

public class CommandEndOfLine extends AbstractCommand {

  private ImageCapture imageCapture;


  public CommandEndOfLine(ImageCapture imageCapture) {
    super(0);
    this.imageCapture = imageCapture;
  }


  @Override
  public AbstractCommand commandReceived() {
    imageCapture.endOfLine();
    return null;
  }
}
