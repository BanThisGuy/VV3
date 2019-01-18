import com.google.gson.Gson;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;


public class TelematikEinheit implements Runnable {

    // jede Einheit erhält eindeutige ID
    private UUID id;

    // Gps Daten der Einheit
    private Gps locationOfVehicle;

    // zufällige Alarm-Nachrichten
    int alarmInSeconds;

    boolean alarmToSend = true;

    // Gefahrene Strecke
    private Long drivenDistanceMeters = 0L;

    // Wartezeit für neue message
    private int waitingPeriodInSeconds;

    // Zufällige Strecke
    Gps.Direction preferredDirection = Gps.Direction.randomDirection();
    ;


    public TelematikEinheit(Gps locationOfVehicle, int waitingPeriodInSeconds) {
        this.id = UUID.randomUUID(); // generiert Einzigartige ID
        //  this.alarmInSeconds = new Random().nextInt(86400); // Ein Tag in sekunden
        this.locationOfVehicle = locationOfVehicle;
        this.waitingPeriodInSeconds = waitingPeriodInSeconds;
    }


    @Override
    public void run() {
        // zufällig generierter Zielort
        preferredDirection = Gps.Direction.randomDirection();
        System.out.println("Zielrichtung: " + preferredDirection);

        System.out.println("Telematikeinheit: " + id);
        System.out.println("Aktueller Standort: " + locationOfVehicle.toString());

        while (true) {
            try {
                int waitingPeriodInMilliseconds = waitingPeriodInSeconds * 1000;
                Thread.sleep(waitingPeriodInMilliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Woke up");
            // zufällige Zahl für die Distanz
            System.out.println("Kurzstrecke o. Langstrecke: " + drivenDistanceMeters);

            drivenDistanceMeters += getRandomDrivenDistance(waitingPeriodInSeconds);
            System.out.println("Gefahrene Strecke: " + drivenDistanceMeters);

            // Simuliere Fahrt und Zielort
            Gps newLocationOfVehicle = drive(locationOfVehicle);
            locationOfVehicle = newLocationOfVehicle;

            System.out.println("Neuer Standort: " + newLocationOfVehicle);


            // generiere message
            Nachricht nachricht = new Nachricht(id, drivenDistanceMeters, locationOfVehicle);
            AlarmNachricht alarmNachricht = new AlarmNachricht(id, drivenDistanceMeters, locationOfVehicle, "alarm!");

            if (alarmToSend) {
                // wenn alarm true sende Alarm-Nachricht
                try {
                    sendAlarm(alarmNachricht);
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
                alarmToSend = false;
                // sende message
            } else try {
                sendMessage(nachricht);
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (NamingException e) {
                e.printStackTrace();
            }

            this.locationOfVehicle = newLocationOfVehicle;
        }
    }

    private Gps drive(Gps locationOfVehicle) {

        Gps newLocation = locationOfVehicle;

        // Langstrecken
        while (drivenDistanceMeters > 550) {
            Gps.Direction randomDirection = Gps.Direction.weightedRandomDirection(preferredDirection);
            switch (randomDirection) {
                case NORDEN:
                    newLocation.driveToTheNorthLong();
                case OSTEN:
                    newLocation.driveToTheEastLong();
                case SUEDEN:
                    newLocation.driveToTheSouthLong();
                case WESTEN:
                    newLocation.driveToTheWestLong();
            }
            drivenDistanceMeters -= 550;
        }

        // Kurzstrecken
        while (drivenDistanceMeters > 35) {
            Gps.Direction randomDirection = Gps.Direction.weightedRandomDirection(preferredDirection);
            switch (randomDirection) {
                case NORDEN:
                    newLocation.driveToTheNorthShort();
                case OSTEN:
                    newLocation.driveToTheEastShort();
                case SUEDEN:
                    newLocation.driveToTheSouthShort();
                case WESTEN:
                    newLocation.driveToTheWestShort();
            }
            drivenDistanceMeters -= 35;
        }

        return newLocation;
    }


    private Long getRandomDrivenDistance(int waitingPeriod) {
        return ThreadLocalRandom.current().nextLong(0, 33) * waitingPeriodInSeconds;
    }


    private void sendMessage(Nachricht nachricht) throws JMSException, NamingException {
        // JMS + ActiveMQ initalisieren
        Session session = JMSManagement.getSession();

        // queue mit nachrichten der Einheiten
        Queue fahrdaten = session.createQueue("fahrdaten");

        // producer verarbeitet senden der Nachricht
        MessageProducer producer = session.createProducer(fahrdaten);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        Gson gson = new Gson();
        String json = gson.toJson(nachricht);
        TextMessage message = session.createTextMessage(json);
        producer.send(message);

        producer.close();
        session.close();
    }

    private void sendAlarm(AlarmNachricht alarmNachricht) throws JMSException, NamingException {
        // JMS + ActiveMQ initalisieren
        Session session = JMSManagement.getSession();

        // queue mit Alarm-Nachrichten
        Queue alarmNachrichten = session.createQueue("alarmNachrichten");

        // producer verarbeitet senden der Alarm-Nachricht
        MessageProducer producer = session.createProducer(alarmNachrichten);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        Gson gson = new Gson();
        String json = gson.toJson(alarmNachricht);
        TextMessage message = session.createTextMessage(json);
        producer.send(message);

        producer.close();
        session.close();
    }


    public static void main(String[] args) throws NamingException, JMSException, IOException {
        // EingangsFilter sendet Nachricht von fahrdaten zum topic "verteiler"
        EingangsFilter eingangsFilter = new EingangsFilter();
        eingangsFilter.initialize();

        // Fahrtenbuch erhält Nachricht aus Topic verteiler und schreibt diese auf die Festplatte
        Fahrtenbuch fahrtenbuch = new Fahrtenbuch();
        fahrtenbuch.initialize();

        // message producer in eigenem Thread
        Thread thread = new Thread(new TelematikEinheit(new Gps(40.703830518, -74.005666644), 5));
        thread.start();
    }
}

