package server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ServerThread extends Thread {
    Socket socket;
    HashMap<String,Object> pwMap;
    BufferedReader br;
    String id;
    public ServerThread(Socket socket, HashMap<String,Object> pwMap){
        this.socket = socket;
        this.pwMap = pwMap;
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

            System.out.println("[server] connected to client");
            System.out.println("[server] connect with " + socketAddress.getHostString() + " " + socket.getPort());

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            PrintWriter pw = new PrintWriter(osw, true);

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);

            id = br.readLine();
            pw.println("connected " + id + " to server");
            synchronized (pwMap){
                pwMap.put(id,pw);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void run(){
        String msg;
        try {
            while (true) {
                msg = br.readLine();
                if (msg != null && msg.equals("exit")){
                    System.out.println("[server] " + id + " exit");
                    break;
                }else if (msg == null){
                    System.out.println("[server] " + id + " disconnected");
                    break;

                }
                System.out.println("["+id+"] " + msg);
                //send message to clients
                for (String key : pwMap.keySet()) {
                    if (!key.equals(id))

                        ((PrintWriter) pwMap.get(key)).println(id + "/" + msg);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("[server] error/"+ id +" disconnected");
        }finally {
            synchronized (pwMap){
                pwMap.remove(id);
            }
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[server] socket not closed correctly");
            }
        }
    }
}
