package src.chat.server;

import src.chat.auth.AuthenticationService;
import src.chat.auth.AuthenticationServiceDB;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatServer implements Server {
    private Set<ClientHandler> clients;
    private AuthenticationService authenticationService;
    private DataInputStream in;
    private DataOutputStream out;


    public ChatServer() {
        try {
            System.out.println("Server is starting up...");
            ServerSocket serverSocket = new ServerSocket(8888);
            clients = new HashSet<>();
            authenticationService = new AuthenticationServiceDB();
            System.out.println("Server is started up...");


            while (true) {
                System.out.println("Server is listening for clients...");
                Socket socket = serverSocket.accept();
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                System.out.println("Client accepted: " + socket);
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            throw new RuntimeException("public ChatServer", e);
        }
    }

    @Override
    public synchronized void broadcastMessage(String message) {

       if(message.startsWith("/w")) {
            String msg = "";
            String[] array = message.split("\\s");
            for(ClientHandler client : clients){
                if(client.getName().equals(array[1])){
                    for(int i=2;i<array.length;i++){
                        msg = msg + array[i];
                    }
                    client.sendMessage(msg);
                }
            }
        }else{
        clients.forEach(client -> client.sendMessage(message));}
    }

    @Override
    public synchronized boolean isLoggedIn(String nickname) {
        return clients.stream()
                .filter(clientHandler -> clientHandler.getName().equals(nickname))
                .findFirst()
                .isPresent();
    }

    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    @Override
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

}
