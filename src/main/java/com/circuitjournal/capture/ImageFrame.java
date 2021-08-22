package com.circuitjournal.capture;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by indrek on 7.05.2016.
 */
public class ImageFrame {


  private Pixel[][] pixelMatrix;
  private int lineIndex;
  private int colIndex;


  public ImageFrame(int w, int h) {
    pixelMatrix = new Pixel[h][w];
    lineIndex = 0;
    colIndex = 0;
  }


  public void newLine() {
    if (lineIndex < getLineCount() - 1) {
      lineIndex ++;
    }
    colIndex = 0;
  }


  public void addPixel(Pixel pixel) {
    if (lineIndex < pixelMatrix.length
        && colIndex < pixelMatrix[lineIndex].length) {
      pixelMatrix[lineIndex][colIndex] = pixel;
      colIndex++;
    }
  }


  public int getLineLength() {
    return pixelMatrix.length > 0 ? pixelMatrix[0].length : 0;
  }

  public int getLineCount() {
    return pixelMatrix.length;
  }

  public int getCurrentLineIndex() {
    return lineIndex;
  }


  public Color getPixelColor(int x, int y) {
    Pixel pixel = getPixel(x, y);
    if (pixel != null) {
      if (pixel.hasInvalidColors()) {
        fixPixel(pixel, x, y);
      }
      return pixel.getColor();
    } else {
      return Color.BLACK;
    }
  }

  private void fixPixel(Pixel pixel, int x, int y) {
    Collection<Pixel> surroundingPixels = new ArrayList<>();
    Pixel topPixel = getPixel(x, y-1);
    if (topPixel != null) surroundingPixels.add(topPixel);
    Pixel bottomPixel = getPixel(x, y+1);
    if (bottomPixel != null) surroundingPixels.add(bottomPixel);
    Pixel leftPixel = getPixel(x-1, y);
    if (leftPixel != null) surroundingPixels.add(leftPixel);
    Pixel rightPixel = getPixel(x+1, y);
    if (rightPixel != null) surroundingPixels.add(rightPixel);
    pixel.fixColors(surroundingPixels);
  }

  private Pixel getPixel(int x, int y) {
    return x >= 0 && x < getLineLength() &&
            y >= 0 && y < getLineCount() ? pixelMatrix[y][x] : null;
  }


}
