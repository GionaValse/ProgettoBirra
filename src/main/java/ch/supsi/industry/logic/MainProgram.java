package ch.supsi.industry.logic;

import ch.supsi.industry.database.InfluxDB;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveLightSensor;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.data.GroveRotaryValue;
import org.iot.raspberry.grovepi.sensors.digital.GroveButton;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;
import org.iot.raspberry.grovepi.sensors.listener.GroveButtonListener;
import org.iot.raspberry.grovepi.sensors.synch.SensorMonitor;

import java.io.IOException;

/**
 * The Main program for monitoring the final stage of a beer production line using Grove sensors on a Raspberry Pi.
 * <p>
 * This class handles sensor initialization, data acquisition, and communication with InfluxDB.
 * It monitors rotary and light sensors to detect a beer flow direction (Switzerland or Italy) and logs events accordingly.
 * </p>
 */
public class MainProgram implements GroveButtonListener {


    /** Interval in milliseconds between sensor reads. */
    public static final int READ_INTERVAL = 500;

    /** Timeout interval in milliseconds to detect inactivity. */
    public static final int WAIT_INTERVAL = 10_000;

    /** Threshold for detecting significant light changes. */
    public static final int LIGHT_DIFFERENCE = 20;


    private final SensorMonitor<GroveRotaryValue> rotarySensorMonitor;
    private final SensorMonitor<Double> lightSensorRMonitor;
    private final SensorMonitor<Double> lightSensorLMonitor;
    private final GroveButton button;
    private final GroveRgbLcd rgbLcd;

    private final InfluxDB influxDB;

    private boolean inAcquisition = true;

    private double lastLightRValue = -1.0;
    private double lastLightLValue = -1.0;


    /**
     * Private constructor that initializes all sensors and the InfluxDB connection.
     *
     * @throws Exception if sensor or database initialization fails.
     */
    private MainProgram() throws Exception {
        GrovePi grovePi = new GrovePi4J();
        GroveRotarySensor rotarySensor = new GroveRotarySensor(grovePi, 1);
        GroveLightSensor lightSensorR = new GroveLightSensor(grovePi, 2);
        GroveLightSensor lightSensorL = new GroveLightSensor(grovePi, 0);
        button = new GroveButton(grovePi, 5);
        rgbLcd = grovePi.getLCD();

        rotarySensorMonitor = new SensorMonitor<>(rotarySensor, READ_INTERVAL);
        lightSensorRMonitor = new SensorMonitor<>(lightSensorR, READ_INTERVAL);
        lightSensorLMonitor = new SensorMonitor<>(lightSensorL, READ_INTERVAL);

        try {
            influxDB = new InfluxDB();
            bindSensor();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            inAcquisition = false;
            setErrorLCD(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * Factory method to create a new instance of the main program.
     *
     * @return a new instance of {@code MainProgram}.
     * @throws Exception if initialization fails.
     */
    public static MainProgram newInstance() throws Exception {
        return new MainProgram();
    }


    /** Called when the button is released (not used). */
    @Override
    public void onRelease() {}

    /** Called when the button is pressed (not used). */
    @Override
    public void onPress() {}

    /**
     * Called when the button is clicked.
     * Resumes acquisition if it was previously stopped due to an error.
     */
    @Override
    public void onClick() {
        if (inAcquisition)
            return;

        try {
            setMessageLCD("Acquisition...");
            inAcquisition = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Binds the button listener and starts all sensor monitors.
     */
    private void bindSensor() {
        button.setButtonListener(this);

        // start monitor
        rotarySensorMonitor.start();
        lightSensorRMonitor.start();
        lightSensorLMonitor.start();
    }


    /**
     * Main loop for acquiring sensor data and sending it to InfluxDB.
     * Detects a beer flow direction based on rotary sensor and light changes.
     *
     * @throws InterruptedException if the thread is interrupted.
     * @throws IOException if an error occurs while writing to the LCD.
     */
    public void runAcquisition() throws InterruptedException, IOException {
        setMessageLCD("Acquisition...");

        boolean acquiredRotary = false;
        boolean acquiredLightR = false;
        boolean acquiredLightL = false;

        long startTime = System.currentTimeMillis();

        while (true) {
            Thread.sleep(500);

            if (inAcquisition && (System.currentTimeMillis() - startTime) >= WAIT_INTERVAL) {
                influxDB.putErrorOnDB("Timeout: no beer production");
                setErrorLCD("Error");
                inAcquisition = false;
            }

            if (inAcquisition) {
                if (rotarySensorMonitor.isValid()) {
                    GroveRotaryValue rotaryValue = rotarySensorMonitor.getValue();
                    double degree = rotaryValue.getDegrees();
                    System.out.println(degree);

                    if (degree < 100) { // Right rotation
                        if (acquiredRotary)
                            continue;

                        startTime = System.currentTimeMillis();
                        acquiredRotary = true;
                        System.out.println("Swiss rotation");
                    } else if (degree > 280) { // Left rotation
                        if (acquiredRotary)
                            continue;

                        startTime = System.currentTimeMillis();
                        acquiredRotary = true;
                        System.out.println("Italy rotation");
                    } else {
                        acquiredRotary = false;
                    }
                }

                if (lightSensorRMonitor.isValid()) {
                    double lightValue = lightSensorRMonitor.getValue();

                    if (lastLightRValue == -1) {
                        lastLightRValue = lightValue;
                        continue;
                    }

                    if (Math.abs(lightValue - lastLightRValue) > LIGHT_DIFFERENCE) {
                        if (acquiredLightR)
                            continue;

                        influxDB.putBeerOnDB(Where.SWISS);
                        acquiredLightR = true;
                    } else {
                        acquiredLightR = false;
                    }

                    lastLightRValue = lightValue;
                }

                if (lightSensorLMonitor.isValid()) {
                    double lightValue = lightSensorLMonitor.getValue();

                    if (lastLightLValue == -1) {
                        lastLightLValue = lightValue;
                        continue;
                    }

                    if (Math.abs(lightValue - lastLightLValue) > LIGHT_DIFFERENCE) {
                        if (acquiredLightL)
                            continue;

                        influxDB.putBeerOnDB(Where.BADILAND);
                        acquiredLightL = true;
                    } else {
                        acquiredLightL = false;
                    }

                    lastLightLValue = lightValue;
                }
            }
        }
    }

    /**
     * Displays a message on the RGB LCD screen.
     *
     * @param message the message to display.
     * @throws IOException if the LCD cannot be updated.
     */
    private void setMessageLCD(String message) throws IOException {
        rgbLcd.setRGB(0, 0, 0);
        rgbLcd.setText(message);
    }

    /**
     * Displays an error message on the RGB LCD screen.
     *
     * @param errorMessage the message to display.
     * @throws IOException if the LCD cannot be updated.
     */
    private void setErrorLCD(String errorMessage) throws IOException {
        rgbLcd.setRGB(255, 0, 0);
        rgbLcd.setText(errorMessage);
    }
}
