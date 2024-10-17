package vn.edu.hcmus.mentor.config;

import com.corundumstudio.socketio.SocketIOServer;

public class SocketIOServerShutdownHook extends Thread {

    private final SocketIOServer server;

    public SocketIOServerShutdownHook(SocketIOServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            server.stop();
        } catch (Exception e) {
        }
    }
}