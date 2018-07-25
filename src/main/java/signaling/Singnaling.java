package signaling;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.*;

public class Singnaling extends WebSocketServer {

    private static Map<Integer, Set<WebSocket>> rooms = new HashMap<>();
    private int myroom;

    public Singnaling() {
        super(new InetSocketAddress(30001));
    }


    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(clientHandshake.getResourceDescriptor());
        System.out.println("New client Connected: " + webSocket.getRemoteSocketAddress() + " hash " + webSocket.getRemoteSocketAddress().hashCode());
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        JSONObject obj = new JSONObject(message);
        Set<WebSocket> sockets = null;
        try {
            String msgType = obj.getString("type");
            switch (msgType) {
                case "GETROOM": {
                    myroom = generateRoomNumber();
                    sockets.add(webSocket);
                    rooms.put(myroom, sockets);
                    System.out.println("Generated new room: " + myroom);
                    webSocket.send("{\"type\":\"GETROOM\",\"value\":" + myroom + "}");
                    break;
                }
                case "ENTERROOM": {
                    myroom = obj.getInt("value");
                    System.out.println("New Client entered room " + myroom);
                    sockets = rooms.get(myroom);
                    sockets.add(webSocket);
                    rooms.put(myroom, sockets);
                    break;
                }
                default: {
                    sendToAll(webSocket, message);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println();
    }


    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println("Client disconnected: " + s);
    }


    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println("Error happened: " + e);
    }

    @Override
    public void onStart() {
        System.out.println("Server is started....");
    }

    private void sendToAll(WebSocket webSocket, String message) {
        Iterator it = rooms.get(myroom).iterator();
        while (it.hasNext()) {
            WebSocket socket = (WebSocket) it.next();
            if (socket != webSocket) socket.send(message);
        }
    }

    public int generateRoomNumber() {
        return new Random(System.currentTimeMillis()).nextInt();
    }

    public static void main(String[] args){
        Singnaling server = new Singnaling();
        server.start();
    }
}
