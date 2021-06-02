package server;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ServerThread extends Thread {
    Socket socket;
    HashMap<String,PrintWriter> pwMap;
    BufferedReader br;
    String id;
    public ServerThread(Socket socket, HashMap<String,PrintWriter> pwMap){
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
                message(msg);
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

    public String message(String msg){
        char first = msg.charAt(0);
        String m;
        if (first == '#'){
            m = command(msg.split(" "));
            pwSender(m, false);
            System.out.println("[server] " + id + " issued command: " + msg);
            System.out.println("[server] " + id + " command returned: " + m);
        }else {
            m = ("["+id+"] "+ msg);
            pwSender(m);
            System.out.println(m);
        }
        return m;
    }

    public String command(String[] cmd){
        switch (cmd[0]){
            case "#player":
                switch (cmd[1]){
                    case "list":
                        return pwMap.keySet().toString();
                    default:
                        return "wrong command: player [list]";
                }
            default:
                return "wrong command";
        }
    }

    public void pwSender(String msg, boolean all){
        if (all){
            for (String key : pwMap.keySet()) {
                //if (!key.equals(id)) //don't send message to sender
                pwMap.get(key).println(msg);
            }
        }else {
            pwMap.get(id).println(msg);
        }
    }
    public void pwSender(String msg){
        for (String key : pwMap.keySet()) {
            //if (!key.equals(id)) //don't send message to sender
            pwMap.get(key).println(msg);
        }
    }
}
