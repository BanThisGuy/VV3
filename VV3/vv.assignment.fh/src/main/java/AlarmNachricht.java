import com.google.gson.Gson;

import java.util.UUID;

public class AlarmNachricht extends Nachricht{

    String alarmMessage;



    public AlarmNachricht(UUID telematicsId, Long drivenDistanceMeters, Gps coordinates, String alarmMessage) {

        super(telematicsId, drivenDistanceMeters, coordinates);

        this.alarmMessage = alarmMessage;

    }

    public static AlarmNachricht deserialize(String json){

        // deserialize json

        Gson gson = new Gson();

        return gson.fromJson(json, AlarmNachricht.class);

    }


    public static String serialize(AlarmNachricht message){

        // serialize to json

        Gson gson = new Gson();

        return gson.toJson(message, AlarmNachricht.class);

    }

    // getter & setter

    public String getAlarmMessage() {

        return alarmMessage;

    }

    public void setAlarmMessage(String alarmMessage) {

        this.alarmMessage = alarmMessage;

    }
}


