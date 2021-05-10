import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class client {

    public static void main(String[] args) throws IOException, InterruptedException {
        DatagramSocket clienttoclient = new DatagramSocket(null);
        clienttoclient.bind(new InetSocketAddress(Integer.parseInt("10004")));
        byte[] sendBuf = new byte[]{1,2,2,2,2,0,2};
        InetAddress addr = InetAddress.getByName("10.69.164.50");
        int port = 10000;
        DatagramPacket sendPacket
                = new DatagramPacket(sendBuf, sendBuf.length, addr, port);
        clienttoclient.send(sendPacket);
        //本地端口号;wireshark;tcpdump
        System.out.println("Listen......");
        byte[] recvBuf = new byte[100]; //本地端口
        DatagramPacket recvPacket
                = new DatagramPacket(recvBuf, recvBuf.length);
        int status = 1;
        InetAddress dclientip = null;
        int dclientport;
        while (true) {
            try {
                clienttoclient.receive(recvPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InetAddress userIP = recvPacket.getAddress();
            int userPort = recvPacket.getPort();
            String result = userIP.toString() + " " + userPort;
            String recvStr = new String(recvPacket.getData(), 0, recvPacket.getLength());
            System.out.println("Receive: " + recvStr + " From: " + result);
            System.out.println("Local:" + clienttoclient.getLocalPort());
            System.out.println(status);
            if(status==0) {
                Matcher i = Pattern.compile("((\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3}))")
                        .matcher(recvStr);
                i.find();
                System.out.println("Find client ip address: " + i.group(1));
                try {
                    dclientip = InetAddress.getByName(i.group(1));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                recvStr = recvStr.replace(i.group(1),"");
                Matcher p = Pattern.compile("(\\d{1,5})").matcher(recvStr);
                p.find();
                System.out.println("Find client port: " + p.group(1));
                dclientport = Integer.parseInt(p.group(1));
                byte[] holeinfo = ".".getBytes();
                DatagramPacket holePacket
                        = new DatagramPacket(holeinfo, holeinfo.length, dclientip, dclientport);
                try {
                    clienttoclient.send(holePacket);
                    System.out.println("Send udphole info.");
                    status++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(status==1){
                Thread.sleep(1000);
                dclientip = userIP;
                dclientport = userPort;
                byte[] holeinfo = ".".getBytes();
                DatagramPacket holePacket
                        = new DatagramPacket(holeinfo, holeinfo.length,dclientip,dclientport);
                try {
                    clienttoclient.send(holePacket);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}

