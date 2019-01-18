import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


public class JMSManagement {
    // Queues
    private static Queue fahrdaten;
    private static Queue alarmNachrichten;

    // Topic
    private static Topic verteiler;


    static {
        try {
            fahrdaten = getSession().createQueue("fahrdaten");
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    static {

        try {
            alarmNachrichten = getSession().createQueue("alarmNachrichten");
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    static {
        try {
            verteiler = getSession().createTopic("verteiler");
        } catch (JMSException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public JMSManagement() throws NamingException, JMSException {
    }

    public static Session getSession() throws NamingException, JMSException {
        // JMS + ActiveMQ initialisierung
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        Context naming = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) naming.lookup("ConnectionFactory");

        Connection connection = connectionFactory.createConnection();

        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        return session;

    }

    public static Connection getConnection() throws JMSException, NamingException {
        // JMS + ActiveMQ initialisierung
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

        Context naming = new InitialContext(props);
        ConnectionFactory connectionFactory = (ConnectionFactory) naming.lookup("ConnectionFactory");

        Connection connection =  connectionFactory.createConnection();

        return connection;
    }


    public static Queue getFahrdaten() {
        return fahrdaten;
    }

    public static Queue getAlarmNachrichten() {
        return alarmNachrichten;
    }

    public static Topic getVerteiler() {
        return verteiler;
    }
}
