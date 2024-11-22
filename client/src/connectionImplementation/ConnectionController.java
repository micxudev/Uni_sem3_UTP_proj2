package connectionImplementation;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

import static connectionImplementation.ConnectionStatus.*;
import static managers.ColorManager.*;

public class ConnectionController {
    private final JTextField ipTextField;
    private final JTextField portTextField;
    private final JTextField usernameTextField;
    private final JLabel statusLabel;
    private final JButton connectButton;
    private final JLabel northServerIp;
    private final JLabel northConnectionStatus;

    public ConnectionController(JTextField ipTextField, JTextField portTextField, JTextField usernameTextField, JLabel statusLabel, JButton connectButton, JLabel northServerIp, JLabel northConnectionStatus) {
        this.ipTextField = ipTextField;
        this.portTextField = portTextField;
        this.usernameTextField = usernameTextField;
        this.statusLabel = statusLabel;
        this.connectButton = connectButton;
        this.northServerIp = northServerIp;
        this.northConnectionStatus = northConnectionStatus;
    }

    // Main logic function to handle connection status
    public void buttonClick() {
        boolean isAlive = Connection.isAlive();

        if (isAlive && !Connection.passedUsernameValidation()) {
            resendUsername();
        } else if (isAlive) {
            disconnect();
        }
        else {
            connect();
        }
    }

    private void resendUsername() {
        beforeButtonClickProcess("resending...");
        setUsernameEditable(false);

        new SwingWorker<ConnectionStatus, Void>() {
            @Override
            protected ConnectionStatus doInBackground() {
                return Connector.resendUsername(usernameTextField.getText());
            }

            @Override
            protected void done() {
                try {
                    ConnectionStatus status = get();
                    handleDoneForConnectAndResend(status);
                } catch (InterruptedException | ExecutionException _) {
                    setUnknownError();
                    setInputEditable(true);
                } finally {
                    connectButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void connect() {
        beforeButtonClickProcess("connecting...");
        setInputEditable(false);
        northServerIp.setText(ipTextField.getText() + " : " + portTextField.getText());

        new SwingWorker<ConnectionStatus, Void>() {
            @Override
            protected ConnectionStatus doInBackground() {
                return Connector.tryToConnect(ipTextField.getText(), portTextField.getText(), usernameTextField.getText());
            }

            @Override
            protected void done() {
                try {
                    ConnectionStatus status = get();
                    handleDoneForConnectAndResend(status);
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

    private void handleDoneForConnectAndResend(ConnectionStatus status) {
        if (status == USERNAME_INVALID || status == USERNAME_TAKEN) {
            setStatusColor(CONTENT__STATUS_LABEL__FAILURE);
            setUsernameEditable(true);
            connectButton.setText("Resend");
        }
        else if (status.getCode() < 0) {
            setStatusColor(CONTENT__STATUS_LABEL__FAILURE);
            setInputEditable(true);
        }
        else if (status == CONNECTED) {
            setStatusColor(CONTENT__STATUS_LABEL__SUCCESS);
            connectButton.setText("Disconnect");
        }
        setStatusText(status.getMessage());
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
        setUsernameEditable(editable);
    }

    private void setUsernameEditable(boolean editable) {
        usernameTextField.setEditable(editable);
        usernameTextField.setFocusable(editable);
    }

    private void setStatusColor(Color color) {
        statusLabel.setForeground(color);
        northConnectionStatus.setForeground(color);
    }

    private void setStatusText(String text) {
        statusLabel.setText(text);
        northConnectionStatus.setText(text + " / " + usernameTextField.getText());
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