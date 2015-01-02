package es.reprogramador.util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Enumeration;
import java.util.EventListener;

public class networkHelper extends Thread {

    public enum protocolType {
        TCP,
        UDP
    }

    private static String MULTICAST_GROUP = "224.0.0.1";
    private Context _context = null;
    private protocolType _protocol = protocolType.TCP;
    private int _serverPort = 0;
    private int _remotePort = 0;
    private int _packetMaxLength = 1000;
    private boolean _useMulticast = false;
    private boolean _keepRunning = true;
    private String _lastMessage = "";
    private String _lockID = "";
    private EventListenerList _messageListenerList = new EventListenerList();

    /**
     *
     * @param c                 Application context, usually "this".
     * @param protocol          Which protocol to use.
     * @param listenPort        Local server port.
     * @param remotePort        Remote destination port.
     * @param useMulticast      Listen to multicast packets? (drains more battery)
     * @param packetMaxLength   Maximum packet length.
     * @param lockID            Broadcast lock identifier, can be any unique string.
     */
    public networkHelper(Context c, protocolType protocol, int listenPort, int remotePort, boolean useMulticast, int packetMaxLength, String lockID){
        _context=c;
        _protocol = protocol;
        _serverPort = listenPort;
        _remotePort = remotePort;
        _useMulticast = useMulticast;
        _packetMaxLength = packetMaxLength;
        _lockID = lockID;
    }

    public interface MessageListener extends EventListener {
        void onMessageReceived(String message);
    }

    public void addMessageListener(MessageListener l) {
        _messageListenerList.add(MessageListener.class, l);
    }

    public void removeMessageListener(MessageListener l) {
        _messageListenerList.remove(MessageListener.class, l);
    }

    private void fireListeners(){
        MessageListener[] ls = _messageListenerList.getListeners(MessageListener.class);
        for (MessageListener l : ls) {
            l.onMessageReceived(_lastMessage);
        }
    }

    public void run() {
        byte[] lMessage = new byte[_packetMaxLength];
        InetAddress group = null;
        DatagramPacket packet = new DatagramPacket(lMessage, lMessage.length);
        DatagramSocket socket = null;
        MulticastSocket multi = null;
        WifiManager wifi = null;
        WifiManager.MulticastLock multicastLock = null;
        String message = "";

        try {
            if (_useMulticast) {
                group = InetAddress.getByName(MULTICAST_GROUP);
                wifi = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifi.createMulticastLock(_lockID);
                multicastLock.setReferenceCounted(true);
                multicastLock.acquire();
                multi = new MulticastSocket(_serverPort);
                multi.joinGroup(group);
                socket = multi;
            } else {
                socket = new DatagramSocket(_serverPort);
            }
            while (_keepRunning) {
                socket.receive(packet);
                message = new String(lMessage, 0, packet.getLength());
                if (message != "") {
                    _lastMessage = message;
                    fireListeners();
                }
            }
            if (_useMulticast) {
                multi.leaveGroup(group);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (socket != null) {
            socket.close();
        }

        if (_useMulticast && (multicastLock != null)) {
            multicastLock.release();
            multicastLock = null;
        }

    }

    public InetAddress getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void kill() {
        _keepRunning = false;
    }

    public String getLastMessage() {
        return _lastMessage;
    }

}
