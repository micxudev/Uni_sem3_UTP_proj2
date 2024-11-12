package connectionImplementation;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

import static managers.ColorManager.*;

public class ConnectionController {
    private final JTextField ipTextField;
    private final JTextField portTextField;
    private final JLabel statusLabel;
    private final JButton connectButton;
    private final JLabel northServerIp;
    private final JLabel northConnectionStatus;

    public ConnectionController(JTextField ipTextField, JTextField portTextField, JLabel statusLabel, JButton connectButton, JLabel northServerIp, JLabel northConnectionStatus) {
        this.ipTextField = ipTextField;
        this.portTextField = portTextField;
        this.statusLabel = statusLabel;
        this.connectButton = connectButton;
        this.northServerIp = northServerIp;
        this.northConnectionStatus = northConnectionStatus;
    }

    public void buttonClick() {
        if (!Connector.isConnected()) {
            connect();
        } else {
            disconnect();
        }
    }

    private void connect() {
        beforeButtonClickProcess("connecting...");
        setInputEditable(false);
        northServerIp.setText(ipTextField.getText() + " : " + portTextField.getText());

        new SwingWorker<ConnectionStatus, Void>() {
            @Override
            protected ConnectionStatus doInBackground() {
                return Connector.tryToConnect(ipTextField.getText(), portTextField.getText());
            }

            @Override
            protected void done() {
                try {
                    ConnectionStatus status = get();
                    String message = status.getMessage();

                    if (status.getCode() < 0) {
                        setStatusColor(CONTENT__STATUS_LABEL__FAILURE);
                        setInputEditable(true);
                    } else if (status == ConnectionStatus.CONNECTED) {
                        setStatusColor(CONTENT__STATUS_LABEL__SUCCESS);
                        connectButton.setText("Disconnect");
                    }
                    setStatusText(message);
                } catch (InterruptedException | ExecutionException _) {
                    setUnknownError();
                    setInputEditable(true);
                } finally {
                    connectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void disconnect() {
        beforeButtonClickProcess("disconnecting...");

        new SwingWorker<ConnectionStatus, Void>() {
            @Override
            protected ConnectionStatus doInBackground() {
                return Connector.tryToDisconnect();
            }

            @Override
            protected void done() {
                try {
                    ConnectionStatus status = get();
                    String message = status.getMessage();
                    if (status.getCode() < 0) {
                        setStatusColor(CONTENT__STATUS_LABEL__FAILURE);
                    } else if (status == ConnectionStatus.DISCONNECTED) {
                        connectButton.setText("Connect");
                    }
                    setStatusText(message);
                } catch (InterruptedException | ExecutionException _) {
                    setUnknownError();
                } finally {
                    connectButton.setEnabled(true);
                    setInputEditable(true);
                }
            }
        }.execute();
    }

    // Helper functions
    private void beforeButtonClickProcess(String text) {
        connectButton.setEnabled(false);
        setStatusColor(CONTENT__STATUS_LABEL__DEFAULT);
        setStatusText(text);
    }

    private void setInputEditable(boolean editable) {
        ipTextField.setEditable(editable);
        ipTextField.setFocusable(editable);
        portTextField.setEditable(editable);
        portTextField.setFocusable(editable);
    }

    private void setStatusColor(Color color) {
        statusLabel.setForeground(color);
        northConnectionStatus.setForeground(color);
    }

    private void setStatusText(String text) {
        statusLabel.setText(text);
        northConnectionStatus.setText(text);
    }

    private void setUnknownError() {
        setStatusColor(CONTENT__STATUS_LABEL__FAILURE);
        setStatusText(ConnectionStatus.UNKNOWN_ERROR.getMessage());
    }

    public void serverClosedConnection(String message) {
        setInputEditable(true);
        connectButton.setEnabled(true);
        connectButton.setText("Connect");
        setStatusColor(CONTENT__STATUS_LABEL__FAILURE);
        setStatusText(message);
    }
}