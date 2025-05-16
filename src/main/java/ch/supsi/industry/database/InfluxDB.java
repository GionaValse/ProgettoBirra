package ch.supsi.industry.database;

import ch.supsi.industry.logic.Where;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import com.influxdb.client.domain.WritePrecision;

import java.io.IOException;
import java.time.Instant;

/**
 * Handles communication with an InfluxDB instance for logging beer production data and errors.
 * <p>
 * This class manages the connection to InfluxDB, and provides methods to write
 * beer production events and error messages as time-series data.
 * </p>
 */
public class InfluxDB {

    private final InfluxDBClient client;
    private final WriteApiBlocking writeApi;

    /**
     * Initializes the InfluxDB client and verifies the connection.
     *
     * @throws IOException if the connection to InfluxDB fails.
     */
    public InfluxDB() throws IOException {
        String token = "jWB2qn8caxpXu6EL79UHqcSRmuZTcMiFBdter0JH5Kf-ks5YfGt_ZZ44P7dWqHfXjUtQRVEh_MRChCuNxmmo2A==";
        client = InfluxDBClientFactory.create("http://169.254.43.67:8086/", token.toCharArray());

        if (!client.ping())
            throw new IOException("InfluxDB connection failed");

        writeApi = client.getWriteApiBlocking();
    }

    /**
     * Writes a generic point to the InfluxDB database.
     *
     * @param point the point to write.
     */
    public void putPointOnDB(Point point) {
        String bucket = "project";
        String org = "Supisi";

        System.out.println("Sending point: " + point.toLineProtocol());
        writeApi.writePoint(bucket, org, point);
    }

    /**
     * Logs a beer production event to InfluxDB.
     *
     * @param where the destination of the beer (e.g., Switzerland or Italy).
     */
    public void putBeerOnDB(Where where) {
        Point point = Point.measurement("beer");
        point.addTag("where", where.toString());
        point.addField("count", 1L);
        point.time(Instant.now(), WritePrecision.NS);

        putPointOnDB(point);
    }

    /**
     * Logs an error message to InfluxDB.
     *
     * @param errorMessage the error message to log.
     */
    public void putErrorOnDB(String errorMessage) {
        Point point = Point.measurement("error");
        point.addField("error", errorMessage);
        point.time(Instant.now(), WritePrecision.NS);

        putPointOnDB(point);
    }

    /**
     * Closes the InfluxDB client connection.
     */
    public void close() {
        client.close();
    }
}
