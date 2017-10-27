/*

Network Notifier

Copyright (C) 2015 Jesús Diéguez Fernández

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA or see http://www.gnu.org/licenses/.

*/

package es.reprogramador.util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.EventListener;

public class networkHelper extends Thread {

    public enum protocolType {
        TCP,
        UDP
    }

    private static String MULTICAST_GROUP = "224.0.0.1";
    private static String MULTICAST_GROUP_V6 = "FF01:0:0:0:0:0:0:1";
    private Context _context = null;
    private protocolType _protocol = protocolType.TCP;
    private int _serverPort = 0;
    private int _remotePort = 0;
    private int _packetMaxLength = 1000;
    private boolean _useMulticast = true;
    private boolean _keepRunning = true;
    private String _lastMessage = "";
    private String _lockID = "";
    private static WifiManager.WifiLock wifiLock = null;
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
        InetAddress groupv6 = null;
        DatagramPacket packet = new DatagramPacket(lMessage, lMessage.length);
        DatagramSocket socket = null;
        MulticastSocket multi = null;
        WifiManager wifi;
        WifiManager.MulticastLock multicastLock = null;
        String message;

        try {
            if (_useMulticast) {
                group = InetAddress.getByName(MULTICAST_GROUP);
                groupv6 = InetAddress.getByName(MULTICAST_GROUP_V6);
                wifi = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                try {
                    multicastLock = wifi.createMulticastLock(_lockID);
                } catch(Exception ex) { }
                if (multicastLock != null) {
                    multicastLock.setReferenceCounted(true);
                    multicastLock.acquire();
                }
                multi = new MulticastSocket(_serverPort);
                multi.joinGroup(group);
                multi.joinGroup(groupv6);
                socket = multi;
            } else {
                socket = new DatagramSocket(_serverPort);
            }
            while (_keepRunning) {
                socket.receive(packet);
                message = new String(lMessage, 0, packet.getLength());
                if (!message.equals("")) {
                    _lastMessage = message;
                    fireListeners();
                }
            }
            if (_useMulticast) {
                try {
                    multi.leaveGroup(group);
                    multi.leaveGroup(groupv6);
                } catch(Exception ex) { }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (socket != null) {
            socket.close();
        }

        if (_useMulticast && (multicastLock != null)) {
            multicastLock.release();
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

    public void keepWiFiOn(Context context, boolean on) {
        if (wifiLock == null) {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, es.reprogramador.networknotifier.NetworkNotifierService.TAG);
                wifiLock.setReferenceCounted(true);
            }
        }
        if (wifiLock != null) {
            if (on) {
                wifiLock.acquire();
            } else if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
    }

}
