package es.reprogramador.networknotifier;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.graphics.BitmapFactory;
import java.io.ByteArrayInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import es.reprogramador.util.*;

public class NetworkNotifierService extends Service {

    private static final boolean DEBUG = false;
    private static final int MAX_UDP_DATAGRAM_LEN = 1500;
    private static final int UDP_SERVER_PORT = 7415;
    private static final String TAG = "Network Notifier";
    private networkHelper socket = null;
    private notificationHelper notifier = null;
    private int id = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (notifier == null) {
            notifier = new notificationHelper(this, R.drawable.icon, BitmapFactory.decodeResource(getResources(), R.drawable.icon));
            //notifier.showNotification(id, TAG, "Service started.", "http://reprogramador.es");
        }
        if (socket == null) {
            socket = new networkHelper(this, networkHelper.protocolType.UDP, UDP_SERVER_PORT, UDP_SERVER_PORT, true, MAX_UDP_DATAGRAM_LEN, TAG);
            socket.start();
            socket.addMessageListener(new networkHelper.MessageListener()
            {
                public void onMessageReceived(String message)
                {
                    messageHelper m = parseXML(message);
                    if (m.message != "") {
                        if (notifier != null) {
                            notifier.showNotification(++id, m.title, m.message, TAG);
                        }
                    }
                }
            });
        }
    }

    private class messageHelper {
        public String title = "";
        public String message = "";
        public String subMessage = "";
    }

    private messageHelper parseXML(String xml){

        String hash = "";

        messageHelper m = new messageHelper();
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        digestHelper digest = new digestHelper();
        try {
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
            NodeList rootNodes = document.getElementsByTagName("Notification");
            if (rootNodes.getLength() > 0) {
                Node rootElement = rootNodes.item(0);
                NodeList nodes = rootElement.getChildNodes();

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    switch (node.getNodeName().toLowerCase()) {
                        case "source":
                            m.title = node.getFirstChild().getNodeValue();
                            break;
                        case "message":
                            m.message = node.getFirstChild().getNodeValue();
                            break;
                        case "hash":
                            hash = digest.getSHA256Hash(m.title + '-' + m.message);
                            if (!hash.equalsIgnoreCase(node.getFirstChild().getNodeValue())) {
                                // hash mismatch
                                return new messageHelper();
                            } else {
                                m.subMessage = hash;
                            }
                            break;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return m;
    }

    @Override
    public void onDestroy() {
        if (notifier != null) {
            notifier = null;
        }
        if (socket != null) {
            socket.kill();
        }
    }

}
