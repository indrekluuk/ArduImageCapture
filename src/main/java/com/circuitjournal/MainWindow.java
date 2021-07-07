package com.circuitjournal;


import com.circuitjournal.capture.ImageCapture;
import com.circuitjournal.capture.ImageFrame;
import com.circuitjournal.serialreader.SerialReader;
import com.circuitjournal.serialreader.SerialReaderException;
import com.circuitjournal.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by indrek on 06.07.2021.
 */
public class MainWindow {

    private static Integer MAX_IMAGE_W = 640;
    private static Integer MAX_IMAGE_H = 480;

    private static final String WINDOW_TITLE = "Arduino Image Capture";
    private static final String BUTTON_NAME_LISTEN = "Listen";
    private static final String BUTTON_NAME_STOP = "Stop";
    private static final String BUTTON_NAME_SELECT_SAVE_FOLDER = "Select save folder";
    private static final String SELECT_SAVE_FOLDER_TILE = "Save images to";

    private static final String DEFAULT_IMAGE_DIRECTORY = "/img";

    private JFrame windowFrame = new JFrame(WINDOW_TITLE);
    private JPanel mainPanel;
    private File selectedFolder;
    private JLabel saveCountLabel = new JLabel();
    private Integer saveCounter = 0;
    private BufferedImage imageBuffer;
    private JLabel imageContainer;
    private TextArea debugWindow;
    private JComboBox<String> comPortSelection;
    private JComboBox<Integer> baudRateSelection;
    private JButton startListenButton;
    private JButton stopListenButton;

    private Settings settings;
    private SerialReader serialReader;
    private ImageCapture imageCapture;



    public MainWindow(Component showRelativeTo, SerialReader serialReader, Settings settings) {
        this.imageCapture = new ImageCapture(this::drawImage, this::debugTextReceived);

        this.serialReader = serialReader;
        this.serialReader.setReceivedDataHandler((bytes)-> imageCapture.addReceivedBytes(bytes));
        this.settings = settings;

        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.add(createToolbar(), BorderLayout.PAGE_START);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(createImagePanel(), BorderLayout.PAGE_START);
        contentPanel.add(createDebugWindow());
        this.mainPanel.add(new JScrollPane(contentPanel));

        this.mainPanel.add(createSavePanel(), BorderLayout.PAGE_END);


        windowFrame.setContentPane(mainPanel);
        windowFrame.pack();
        //windowFrame.setSize(1000, 600);
        windowFrame.setLocationRelativeTo(showRelativeTo);
        windowFrame.setVisible(true);

        windowFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                stopListening();
            }
        });
    }

    public void setExitOnClose() {
        windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setSelectedComPort(String portName) {
        comPortSelection.setSelectedItem(portName);
        startListening();
    }


    private JComponent createSavePanel() {
        JPanel saveBar = new JPanel ();
        saveBar.setLayout(new BoxLayout(saveBar, BoxLayout.X_AXIS));
        JLabel filePathLabel = new JLabel();

        saveBar.add(createSelectFolderButton(filePathLabel));
        saveBar.add(Box.createHorizontalStrut(10));
        saveBar.add(filePathLabel);
        saveBar.add(saveCountLabel);

        return saveBar;
    }

    private JButton createSelectFolderButton(JLabel filePathLabel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(SELECT_SAVE_FOLDER_TILE);

        JButton listenButton = new JButton(BUTTON_NAME_SELECT_SAVE_FOLDER);
        listenButton.addActionListener((event)->{
            fileChooser.setCurrentDirectory(selectedFolder == null ? getDefaultSaveDirectory() : selectedFolder);

            if (fileChooser.showOpenDialog(listenButton) == JFileChooser.APPROVE_OPTION) {
                selectedFolder = fileChooser.getSelectedFile();
                String selectedFolderPath = selectedFolder.getAbsolutePath();
                filePathLabel.setText(selectedFolderPath);
                settings.saveDefaultSaveFolder(selectedFolderPath);
            }
        });
        return listenButton;
    }

    private File getDefaultSaveDirectory() {
        String defaultSaveFolder = settings.getDefaultSaveFolder();
        if (StringUtils.isNotBlank(defaultSaveFolder)) {
            File dir = new File(defaultSaveFolder);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }

        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir + DEFAULT_IMAGE_DIRECTORY);
        return dir.exists() ? dir : new File(currentDir);
    }


    private JComponent createImagePanel() {
        imageBuffer = new BufferedImage(MAX_IMAGE_W,MAX_IMAGE_H, BufferedImage.TYPE_INT_ARGB);
        imageContainer = new JLabel(new ImageIcon(imageBuffer));
        return imageContainer;
    }


    private Component createDebugWindow() {
        debugWindow = new TextArea();
        return debugWindow;
    }


    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        createComPortOption(toolBar);
        createBaudRateOption(toolBar);
        createListeningButtons(toolBar);
        return toolBar;
    }


    private void createComPortOption(JToolBar toolBar) {
        comPortSelection = new JComboBox<>();
        serialReader.getAvailablePorts().forEach(comPortSelection::addItem);
        toolBar.add(comPortSelection);
    }


    private void createBaudRateOption(JToolBar toolBar) {
        baudRateSelection = new JComboBox<>();
        serialReader.getAvailableBaudRates().forEach(baudRateSelection::addItem);
        baudRateSelection.setSelectedItem(serialReader.getDefaultBaudRate(settings.getDefaultBaudRate()));
        toolBar.add(baudRateSelection);
    }


    private void createListeningButtons(JToolBar toolBar) {
        startListenButton = new JButton(BUTTON_NAME_LISTEN);
        stopListenButton = new JButton(BUTTON_NAME_STOP);
        stopListenButton.setEnabled(false);

        startListenButton.addActionListener((event)-> {
            this.startListening();
            Integer selectedBaudRate = (Integer)baudRateSelection.getSelectedItem();
            if (selectedBaudRate != null) {
                settings.saveDefaultBaudRate(selectedBaudRate);
            }
        });

        stopListenButton.addActionListener((event)-> this.stopListening());

        toolBar.add(startListenButton);
        toolBar.add(stopListenButton);
    }

    private void startListening() {
        try {
            String selectedComPort = (String)comPortSelection.getSelectedItem();
            Integer baudRate = (Integer)baudRateSelection.getSelectedItem();
            serialReader.startListening(selectedComPort, baudRate);
            startListenButton.setEnabled(false);
            stopListenButton.setEnabled(true);
        } catch (SerialReaderException e) {
            JOptionPane.showMessageDialog(windowFrame, e.getMessage());
        }
    }

    public void stopListening() {
        try {
            serialReader.stopListening();
            startListenButton.setEnabled(true);
            stopListenButton.setEnabled(false);
        } catch (SerialReaderException e) {
            JOptionPane.showMessageDialog(windowFrame, e.getMessage());
        }
    }

    public boolean isListening() {
        return serialReader.isListening();
    }


    private void drawImage(ImageFrame imageFrame, Integer lineIndex) {
        JLabel imageContainer = this.imageContainer;

        // Make sure that table update is executed in AWT thread
        SwingUtilities.invokeLater(()-> {
            synchronized (imageContainer) {
                Graphics2D g = imageBuffer.createGraphics();
                int fromLine = lineIndex != null ? lineIndex : 0;
                int toLine = lineIndex != null ? lineIndex : imageFrame.getLineCount() - 1;

                for (int y = fromLine; y <= toLine; y++) {
                    for (int x = 0; x < imageFrame.getLineLength(); x++) {
                        if (x < MAX_IMAGE_W && y < MAX_IMAGE_H) {
                            g.setColor(imageFrame.getPixelColor(x, y));
                            g.drawLine(x, y, x, y);
                        }
                    }
                }
                imageContainer.repaint();
                // wait for last line to be drawn
                if (selectedFolder != null && toLine == imageFrame.getLineCount() - 1) {
                    saveImageToFile(imageBuffer.getSubimage(0, 0, imageFrame.getLineLength(), imageFrame.getLineCount()), selectedFolder);
                }
            }
        });
    }


    private void debugTextReceived(String debugText) {
        System.out.println(debugText);
        debugWindow.append(debugText + "\n");
    }


    private void saveImageToFile(BufferedImage image, File toFolder) {
        try {
            // save image to png file
            File newFile = new File(toFolder.getAbsolutePath(), getNextFileName());
            ImageIO.write(image, "png", newFile);
            saveCountLabel.setText(" (" + (++saveCounter) + ")");
        } catch (Exception e) {
            System.out.println("Saving file failed: " + e.getMessage());
        }
    }

    private String getNextFileName() {
        return (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS")).format(new Date()) + ".png";
    }




}
