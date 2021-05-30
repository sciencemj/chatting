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
            /*while (true){
                is = socket.getInputStream();
                isr = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(isr);

                os = socket.getOutputStream();
                osw = new OutputStreamWriter(os, "UTF-8");
                pw = new PrintWriter(osw, true);

                String buffer = null;
                buffer = br.readLine();
                if (buffer == null){
                    System.out.println("[server] closed by client");
                    break;
                }

                System.out.println("[server] recieved : " + buffer);
                pw.println(buffer);
            }*/

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

