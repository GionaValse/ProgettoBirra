package ch.supsi.industry;

import ch.supsi.industry.logic.MainProgram;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Entry point for the beer production line monitoring application.
 * <p>
 * This class initializes the main program and starts the data acquisition process.
 * It is designed to run on a Raspberry Pi using Grove sensors to monitor
 * the final stage of a beer production line. The collected data is sent to InfluxDB
 * for further analysis and monitoring.
 * </p>
 *
 * <p>
 * Logging for GrovePi and RaspberryPi libraries is disabled to reduce console output.
 * </p>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.OFF);
        Logger.getLogger("RaspberryPi").setLevel(Level.OFF);

        MainProgram program = MainProgram.newInstance();
        program.runAcquisition();
    }
}