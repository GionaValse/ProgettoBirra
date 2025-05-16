
# Beer Production Line Monitoring

## Project Overview
This project is part of the SUPSI DTI course **"Industry 4.0 e la fabbrica del futuro"**.  
It aims to monitor the final stage of a beer production line using a **Raspberry Pi** and **Grove sensors**.  
The system detects the direction of beer flow and logs production data to **InfluxDB** for analysis and visualization.

## Hardware Setup
- **Raspberry Pi** (any model with GPIO support)
- **Grove Sensors**:
  - **Rotary Sensor**: Detects rotation direction (used to distinguish between Italy and Switzerland).
  - **Light Sensors**: Detect changes in light intensity to confirm beer passage.
  - **Button**: Allows manual control of the acquisition process.
  - **RGB LCD**: Displays system status and error messages.
- **InfluxDB**: Time-series database for storing production and error data.

## Software Architecture
The application is written in **Java** and structured into the following components:

- `Main`: Entry point of the application.
- `MainProgram`: Core logic for sensor initialization, data acquisition, and error handling.
- `SensorMonitor<T>`: Generic class for polling sensor values at regular intervals.
- `InfluxDB`: Handles connection and data writing to InfluxDB.
- `Where`: Enum representing the destination of the beer (SWISS or BADILAND).

## Data Flow
1. The system initializes all sensors and starts monitoring.
2. The **rotary sensor** detects the direction of rotation:
   - Right → Switzerland
   - Left → Italy
3. The **light sensors** detect significant changes in light intensity to confirm beer flow.
4. Events are logged to **InfluxDB** with tags and timestamps.
5. Errors (e.g., inactivity timeout) are also logged and shown on the **LCD display**.

## How to Run
1. Connect all Grove sensors to the Raspberry Pi as per the pin configuration in the code.
2. Ensure InfluxDB is installed and running (default URL: `http://169.254.43.67:8086/`).
3. Clone this repository to your Raspberry Pi.
4. Compile the Java code:
   javac -cp .:path/to/grovepi-lib.jar:path/to/influxdb-client.jar *.java
5. Run the application:
   java -cp .:path/to/grovepi-lib.jar:path/to/influxdb-client.jar Main

## Authors
- Ambra Giuse Graziano
- Daniele Cereghetti
- Giona Valsecchi

## License
This project is developed for educational purposes as part of the SUPSI DTI curriculum.
All rights reserved to the authors unless otherwise specified.
