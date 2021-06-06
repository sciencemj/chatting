package server;

import javafx.util.Pair;

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
                pwSender("!player " + id);
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
                pwSender(id + " exit");
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
        Pair<String, Boolean> re;
        String m;
        if (first == '#'){
            re = command(msg.split(" "));
            pwSender(re.getKey(), re.getValue());
            System.out.println("[server] " + id + " issued command: " + msg);
            System.out.println("[server] " + id + " command returned: " + re.toString());
            return re.getKey();
        }else {
            m = ("["+id+"] "+ msg);
            pwSender(m);
            System.out.println(m);
            return m;
        }
    }

    public Pair<String, Boolean> command(String[] cmd){
        switch (cmd[0]){
            case "#player":
                switch (cmd[1]){
                    case "list":
                        return new Pair<String,Boolean>(pwMap.keySet().toString(), false);
                    default:
                        return new Pair<String,Boolean>("wrong command: player [list]", false);
                }
            case "#stone_add":
                return new Pair<String,Boolean>(("!stone_add " + cmd[1] + " " + cmd[2]), true);
            case "#stone_win":
                return new Pair<String,Boolean>(("!stone_win " + cmd[1]), true);
            default:
                return new Pair<String,Boolean>("wrong command",false);
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
