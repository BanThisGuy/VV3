import javax.jms.*;
import javax.naming.NamingException;


public class EingangsFilter implements MessageListener {
    private Session session;
    private Queue fahrdaten = JMSManagement.getFahrdaten();
    private Queue alarmNachrichten = JMSManagement.getAlarmNachrichten();
    private Topic verteiler = JMSManagement.getVerteiler();
    MessageConsumer consumer;
    MessageProducer producerAlarm;
    MessageProducer producerVerteiler;


    public void initialize() throws JMSException, NamingException {
        session = JMSManagement.getSession();
        try {

            // ließt Nachricht aus queue fahrdaten
            consumer = session.createConsumer(fahrdaten);

            // Message listener für queue fahrdaten
            consumer.setMessageListener(this);

            // schreibt alarmNachrichten in queue alarmNachrichten
            producerAlarm = session.createProducer(alarmNachrichten);
            producerAlarm.setDeliveryMode(DeliveryMode.NON_PERSISTENT);


            // schreibt gelesene Nachricht in topic verteiler
            producerVerteiler = session.createProducer(verteiler);
            producerVerteiler.setDeliveryMode(DeliveryMode.NON_PERSISTENT);


        } catch (JMSException ex) {

            ex.printStackTrace();

        }

    }

    public static void main(String[] args) throws NamingException, JMSException {
        EingangsFilter filter = new EingangsFilter();
        filter.initialize();
    }

    @Override
    public void onMessage(Message message) {

        TextMessage nachrichtOrAlarmNachricht = (TextMessage) message;

        Nachricht nachricht = null;
        AlarmNachricht alarmNachricht = null;


        // try deserialize
        try {
            alarmNachricht = AlarmNachricht.deserialize(nachrichtOrAlarmNachricht.getText());
        } catch (JMSException ex) {
            ex.printStackTrace();
        }

        if (alarmNachricht.getAlarmMessage() == null) {

            // falscher Alarm :D
            nachricht = alarmNachricht;
            alarmNachricht = null;
        }


        // Alarm!!

        if (alarmNachricht != null) {

            System.out.println("!!!Alarm in Queue fahrdaten!!!");

            // schreibe in alarmNachrichten
            String json = AlarmNachricht.serialize(alarmNachricht);

            try {
                TextMessage textMessage = session.createTextMessage(json);
                producerAlarm.send(textMessage);


            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }

        // normale Nachricht

        else if (nachricht != null) {

            System.out.println("Neue Nachricht im Topic Verteiler");

            // serialize json
            String json = Nachricht.serialize(nachricht);

            try {

                // schreibe Nachricht in Topic
                TextMessage textMessage = session.createTextMessage(json);
                producerVerteiler.send(textMessage);

            } catch (JMSException e) {
                e.printStackTrace();
            }

        } else { // konnte nicht deserializiert werden -> Alarm oder Fehler

            System.err.println("Alarm oder Fehler: " + nachrichtOrAlarmNachricht.toString());
        }
    }
}
