package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

    public static final int PORT = 5000;
    static HashMap<String,Object> pwMap;

    public static void main(String[] args) {
        // write your code here
        ServerSocket serverSocket = null;

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        OutputStream os = null;
        OutputStreamWriter osw = null;
        PrintWriter pw = null;
        Scanner sc = new Scanner(System.in);

        try{
            serverSocket = new ServerSocket(PORT);

            /*InetAddress inetAddress = InetAddress.getLocalHost();
            String localhost = inetAddress.getHostAddress();

            serverSocket.bind(new InetSocketAddress("localhost", PORT));*/

            System.out.println("[server] binding:" + serverSocket.getInetAddress().getHostAddress());

            pwMap = new HashMap<String,Object>();
            while(true) {
                Socket socket = serverSocket.accept();
                InetSocketAddress socketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

                //System.out.println("[server] connected to client");
                //System.out.println("[server] connect with " + socketAddress.getHostString() + " " + socket.getPort());
                Thread t = new Thread(new ServerThread(socket,pwMap));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if (serverSocket != null && !serverSocket.isClosed()){
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            sc.close();
        }
    }
}

