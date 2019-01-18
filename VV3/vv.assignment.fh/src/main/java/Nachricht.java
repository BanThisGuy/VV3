import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.UUID;


public class Nachricht {

    // Eindeutige Id, somit eindeutige Zuordnung der Nachricht
    private UUID telematicsId;


    // Zeitstempel jeder Nachricht
    private String timestamp;

    // Jede Einheit zeichnet die gefahrene Strecke auf
    private Long drivenDistanceMeters;


    // GPS koordinaten
    private Gps coordinates;

    public Nachricht(UUID telematicsId, Long drivenDistanceMeters, Gps coordinates) {
        this.timestamp = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss.SSS").format(System.currentTimeMillis());
        this.telematicsId = telematicsId;
        this.drivenDistanceMeters = drivenDistanceMeters;
        this.coordinates = coordinates;
    }


    public static Nachricht deserialize(String json) {

        // deserialize json

        Gson gson = new Gson();

        return gson.fromJson(json, Nachricht.class);

    }


    public static String serialize(Nachricht message) {

        // serialize to json
        Gson gson = new Gson();

        return gson.toJson(message, Nachricht.class);

    }

    // getter & setter

    public UUID getTelematicsId() {
        return telematicsId;
    }

    public void setTelematicsId(UUID telematicsId) {
        this.telematicsId = telematicsId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDrivenDistanceMeters() {
        return drivenDistanceMeters;
    }

    public void setDrivenDistanceMeters(Long drivenDistanceMeters) {
        this.drivenDistanceMeters = drivenDistanceMeters;
    }

    public Gps getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Gps coordinates) {
        this.coordinates = coordinates;
    }
}

