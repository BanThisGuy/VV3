import com.sun.xml.internal.ws.encoding.soap.DeserializationException;

import javax.jms.*;
import javax.naming.NamingException;
import java.io.*;
import java.util.*;

public class Fahrtenbuch implements MessageListener {
    private static final String listsDirectory = "C://telematikEinheiten/";

    // Jede Einheit hat seine eigene Liste
    static HashMap<UUID, List<Nachricht>> listsForTelematics = new HashMap<>();

    private Connection connection;
    private Session session;
    Topic verteiler;
    MessageConsumer topicConsumer;


    public void initialize() throws JMSException, NamingException {
        connection = JMSManagement.getConnection();

        // Client identifizierbar machen
        connection.setClientID(this.getClass().getName());
        connection.start();

        session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        // Fahrtenbuch ließt Nachricht aus topic "verteiler"
        verteiler = JMSManagement.getVerteiler();
        topicConsumer = session.createDurableSubscriber(verteiler, this.getClass().getName());

        topicConsumer.setMessageListener(this);

        // erstellt .dir
        new File(listsDirectory).mkdirs();
    }


    @Override
    public void onMessage(Message message) {
        // get text message aus topic
        TextMessage filteredTelematicsMessagesJson = (TextMessage) message;

        // deserialize
        Nachricht deserializedMessage = null;
        try {
            deserializedMessage =
                    Constants.gson.fromJson(filteredTelematicsMessagesJson.getText(), Nachricht.class);

        } catch (JMSException e) {
            e.printStackTrace();
        }
        if (deserializedMessage == null) {
            throw new DeserializationException("deserializierung nicht möglich.");
        }

        // if no file -> create
        if (!checkExistence(deserializedMessage.getTelematicsId())) {
            // new file
            writeNewList(deserializedMessage);
        } else {
            // Nachricht an bestehende Liste anhängen
            addMessageToList(deserializedMessage);
        }
    }


    public long drivenDistance(UUID telematicsId) {
        List<Nachricht> list = listsForTelematics.get(telematicsId);

        // Laufe über Liste und summiere auf
        Iterator<Nachricht> iterator = list.iterator();
        Long drivenDistanceInMeters = 0L;

        while (iterator.hasNext()) {
            drivenDistanceInMeters += iterator.next().getDrivenDistanceMeters();
        }

        return drivenDistanceInMeters;
    }


    public static void writeNewList(Nachricht message) {
        // serialize list with first message in it
        List<Nachricht> messages = new LinkedList<Nachricht>();
        messages.add(message);
        String jsonList = Constants.gson.toJson(messages);

        try {
            PrintWriter out = new PrintWriter(listsDirectory + message.getTelematicsId() + ".txt");

            // schreibt serialized json-list
            out.println(jsonList);

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addMessageToList(Nachricht message) {
        try {
            String pathToFile = new String(listsDirectory + message.getTelematicsId() + ".txt");
            BufferedReader reader = new BufferedReader(new FileReader(pathToFile));

            // file ohne Zeilenumbruch
            String jsonList = reader.readLine();

            reader.close();

            // deserialize
            List<Nachricht> list = Constants.gson.fromJson(jsonList, List.class);

            // schreibt message in liste
            list.add(message);

            // serialize to json
            String listAsJson = Constants.gson.toJson(list);

            // schreibe updated liste
            File file = new File(pathToFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(listAsJson);

            // close
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkExistence(UUID telematicsId) {
        File exist = new File(listsDirectory + telematicsId.toString() + ".txt");
        return exist.exists();
    }
}
