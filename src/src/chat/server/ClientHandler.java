package src.chat.server;

import src.chat.logging.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;


public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    boolean flag =false;
    private Logger logger;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            doListen();
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

    public String getName() {
        return name;
    }

    private void doListen() {
        new Thread(() -> {
            try {
                doAuth();
                logger = new Logger(socket, name);
                try {
                    logger.showLog();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                receiveMessage();
            } catch (SocketTimeoutException e) {
                e.getStackTrace();
            } finally {
                server.unsubscribe(this);
            }
        }).start();
    }

    private void doAuth() throws SocketTimeoutException {
        try {
                socket.setSoTimeout(300000);
            while (true) {

                    String credentials = in.readUTF();

                    /**
                     * Input credentials sample
                     * "-auth n1@mail.com 1"
                     */
                    if (credentials.startsWith("-auth")) {
                        /**
                         * After splitting sample
                         * array of ["-auth", "n1@mail.com", "1"]
                         */
                        String[] credentialValues = credentials.split("\\s");
                        server.getAuthenticationService()
                                .doAuth(credentialValues[1], credentialValues[2])
                                .ifPresentOrElse(
                                        user -> {
                                            if (!server.isLoggedIn(user.getNickname())) {
                                                name = user.getNickname();
                                                server.broadcastMessage(name + " is logged in.");
                                                server.subscribe(this);
                                                sendMessage("cmd auth: Status OK "+ name);
                                                flag = true;
                                            } else {
                                                sendMessage("Current user is already logged in.");
                                            }

                                        },
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                sendMessage("No a such user by email and password.");
                                            }
                                        }
                                );

                    }
                    if (flag) {
                        return;
                    }
                }

        } catch (SocketTimeoutException e) {
            e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives input data from {@link ClientHandler#in} and then broadcast via {@link Server#broadcastMessage(String)}
     */
    private void receiveMessage() {
        try {
            while (true) {
               String message = in.readUTF();
                if (message.equals("-exit")) {
                    server.unsubscribe(this);
                    return;
                }
                server.broadcastMessage(message);
                logger.addLog(message);
            }
        } catch (Exception e) {
            throw new RuntimeException("SWW", e);
        }
    }

    public void sendMessage(String message) {
        try {
            if(flag){
            logger.addLog(message);}
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return Objects.equals(server, that.server) &&
                Objects.equals(socket, that.socket) &&
                Objects.equals(in, that.in) &&
                Objects.equals(out, that.out) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, socket, in, out, name);
    }
}
