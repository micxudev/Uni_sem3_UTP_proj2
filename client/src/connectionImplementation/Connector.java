package connectionImplementation;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static connectionImplementation.ConnectionStatus.*;

public class Connector {
    private Connector() {}

    public static ConnectionStatus tryToConnect(String ip, String port) {
        int port_int;

        try {
            port_int = Integer.parseInt(port);
        } catch (NumberFormatException _) {
            return PORT_NOT_INT;
        }

       if (port_int < 0 || port_int > 0xFFFF) {
           return PORT_OUT_OF_RANGE;
       }

        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port_int), 10_000);
            Connection.setNewConnection(socket);
            return CONNECTED;
        } catch (SocketTimeoutException _) {
            return CONNECTION_TIMEOUT;
        } catch (UnknownHostException _) {
            return UNKNOWN_IP;
        } catch (IOException _) {
            return CONNECTION_FAILED;
        }
    }

    public static ConnectionStatus tryToDisconnect() {
        if (Connection.isConnectionAlive()) {
            return Connection.closeConnection();
        }
        return ALREADY_DISCONNECTED;
    }

    public static boolean isConnected() {
        return Connection.isConnectionAlive();
    }
}