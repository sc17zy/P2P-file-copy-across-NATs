import service.MyPackageParse;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.*;
import javax.swing.*;
import static java.lang.Thread.sleep;

public class Send {
    final static String SERVER_ADDR = "10.69.84.195";
    private static InetAddress desAddress;
    private static int desPort;
    private static int size = 0;
    private int recAck = 0;
    private static int remainder = 0;
    private static int size_f = 0;
    private static int finish = 0;
    private boolean recAckFlag;
    private static boolean Breakpoint = false;
    private static boolean loop = true;
    private static String fileA = "";
    private static String code = "";
    private static int code1 = 0;
    private static int code2 = 0;
    private static int code3 = 0;
    private static int code4 = 0;
    private static int bk1 = -1;
    private static int bk2 = -1;
    private static int bk3 = -1;

    private static DatagramSocket ds = null;

    static class Sender implements Runnable {
        private int bk;
        private int seqNo = 0;
        private int recAckS = 0;
        private int sizeS = 0;
        private int remainderS = 0;
        private boolean recAckFlagS;
        private static DatagramSocket dsS = null;
        private static boolean flagA = false;
        private static boolean flagB = false;

        public Sender(int bk, int size, int remainder) {
            this.bk = bk;
            this.sizeS = size;
            this.seqNo = bk+1;
            this.remainderS = remainder;
        }

        @Override
        public void run() {
            try {
                dsS = new DatagramSocket();
                this.sendFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void ackCheckS(MyPackageParse packet) throws IOException {
            while (true) {
                try {
                    byte[] buffer = new byte[2048];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    dsS.setSoTimeout(5000);
                    dsS.receive(dp);
                    byte[] data = dp.getData();
                    MyPackageParse parse = null;
                    try {
                        ByteArrayInputStream in = new ByteArrayInputStream(data);
                        ObjectInputStream msg = new ObjectInputStream(in);
                        parse = (MyPackageParse) msg.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    desAddress = dp.getAddress();
                    desPort = dp.getPort();
                    recAckS = parse.getAck_num();
                    recAckFlagS = true;
                } catch (SocketTimeoutException e) {
                    System.out.println(Thread.currentThread().getName() + "||Timed out.");
                    recAckFlagS = false;
                }
                if (recAckFlagS == true && recAckS == seqNo && sizeS > 0) {
                    System.out.println(Thread.currentThread().getName() +
                            "||Receive Ack: " + recAckS + "  |From : " + desAddress + desPort);
                    seqNo++;
                    sizeS--;
                    finish++;
                    break;
                } else if (recAckFlagS == true && recAckS == seqNo && sizeS == 0) {
                    System.out.println(Thread.currentThread().getName() +
                            "||Receive Ack: " + recAckS + "  |From : " + desAddress + desPort);
                    finish++;
                    System.out.println(Thread.currentThread().getName() + "||Finished.");
                    break;
                } else {
                    sendS(packet);
                    System.out.println(Thread.currentThread().getName() +
                            "||Resend packet -- sequence number : " + seqNo);
                }
            }
        }

        public boolean sendS(MyPackageParse packet){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.writeObject(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] dataR = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(dataR,dataR.length,desAddress,desPort);
            try {
                dsS.send(sendPacket);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        public void sendFile() throws IOException {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream(fileA);
            byte[] testRead = new byte[1024];
            while (bk > 0) {
                fileInputStream.read(testRead);
                bk--;
                sizeS--;
                System.out.println(Thread.currentThread().getName() + "|| Locate the position, remain count  :" + bk);
            }
            System.out.println(Thread.currentThread().getName() + "||Send file: " +
                    fileA + " |Packets statistic: " + sizeS + " |Remainder: " + remainderS);
            while (sizeS > 0) {
                fileInputStream.read(testRead);
                MyPackageParse myPackageParse = new MyPackageParse();
                myPackageParse.setDataSize(1024);
                myPackageParse.setSeq_num(seqNo);
                myPackageParse.setData(testRead);
                sendS(myPackageParse);
                ackCheckS(myPackageParse);
            }
            if(remainderS == -1){
                System.out.println(Thread.currentThread().getName() + "||Finished.");
            }
            if(Thread.currentThread().getName()=="C"){
                while (!(flagA && flagB)){
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(remainderS == 0 && flagA && flagB){
                MyPackageParse myPackageParse = new MyPackageParse();
                myPackageParse.setDataSize(0);
                myPackageParse.setSeq_num(seqNo);
                myPackageParse.setEnd_flag(true);
                sendS(myPackageParse);
                ackCheckS(myPackageParse);
            }else if(remainderS!=0 && flagA && flagB) {
                byte[] by = new byte[remainderS];
                fileInputStream.read(by);
                MyPackageParse myPackageParse = new MyPackageParse();
                myPackageParse.setDataSize(remainderS);
                myPackageParse.setSeq_num(seqNo);
                myPackageParse.setEnd_flag(true);
                myPackageParse.setData(by);
                sendS(myPackageParse);
                ackCheckS(myPackageParse);
            }
            if(Thread.currentThread().getName() == "A"){
                flagA = true;
            }else if(Thread.currentThread().getName() == "B"){
                flagB = true;
            }
            fileInputStream.close();
        }
    }

    public static boolean send(MyPackageParse packet){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] dataR = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(dataR,dataR.length,desAddress,desPort);
        try {
            ds.send(sendPacket);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void init() throws SocketException, UnknownHostException {
        ds = new DatagramSocket();
        byte[] test = new byte[]{1, (byte) code1, (byte) code2, (byte) code3, (byte) code4,0,2};
        InetAddress addr = InetAddress.getByName(SERVER_ADDR);
        int port = 10000;
        DatagramPacket testPacket
                = new DatagramPacket(test, test.length, addr, port);
        try {
            ds.send(testPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if(this.ds != null){
            this.ds.close();
        }
    }

    public void receive(DatagramPacket dp) throws IOException {
        ds.receive(dp);
    }

    public void find(DatagramPacket res){
        String recvStr = new String(res.getData(), 0, res.getLength());
        Matcher i = Pattern.compile("((\\d{1,3}).(\\d{1,3}).(\\d{1,3}).(\\d{1,3}))")
                .matcher(recvStr);
        i.find();
        System.out.println("Find receiver ip address: " + i.group(1));
        try {
            desAddress = InetAddress.getByName(i.group(1));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        recvStr = recvStr.replace(i.group(1),"");
        Matcher p = Pattern.compile("(\\d{1,6})").matcher(recvStr);
        p.find();
        System.out.println("Find receiver port: " + p.group(1));
        desPort = Integer.parseInt(p.group(1));
    }

    public void ackCheck(MyPackageParse packet) throws IOException {
        while (true) {
            try {
                byte[] buffer = new byte[2048];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                ds.setSoTimeout(5000);
                ds.receive(dp);
                byte[] data = dp.getData();
                MyPackageParse parse = null;
                try {
                    ByteArrayInputStream in = new ByteArrayInputStream(data);
                    ObjectInputStream msg = new ObjectInputStream(in);
                    parse = (MyPackageParse) msg.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                desAddress = dp.getAddress();
                desPort = dp.getPort();
                recAck = parse.getAck_num();
                Breakpoint = parse.isBreakpoint();
                bk1 = parse.getBk1();
                bk2 = parse.getBk2();
                bk3 = parse.getBk3();
                recAckFlag = true;
            } catch (SocketTimeoutException e) {
                System.out.println("Timed out.");
                recAckFlag = false;
            }
            if(recAckFlag == true && Breakpoint == true){
                System.out.println("Find breakpoint: " + bk1+"|"+bk2+"|"+bk3 + "  |From : " + desAddress + desPort);
                if (bk1!=-1 && bk2!=-1 && bk3!=-1) {
                    finish = bk1+bk2-(1+(size_f-1)/3)+bk3-(1+(size_f-1)*2/3);
                }
                break;
            }else if (recAckFlag == true && recAck == 0 && size > 0) {
                System.out.println("Receive Ack: " + recAck + "  |From : " + desAddress + desPort);
                size--;
                finish++;
                break;
            } else if (recAckFlag == true && recAck == 0 && size == 0) {
                System.out.println("Receive Ack: " + recAck + "  |From : " + desAddress + desPort);
                finish++;
                System.out.println("Finished.");
                break;
            } else{
                send(packet);
                System.out.println("Resend packet -- sequence number : " + 0);
            }
        }
    }

    public void sendData(String filename) throws IOException {

        FileInputStream fileInputStream = null;
        try{
            fileInputStream = new FileInputStream(filename);
            int fileSize = fileInputStream.available();
            size = fileSize / (1024);
            remainder = fileSize%(1024);
            System.out.println("Send file: " + filename + " |Packets statistic: " + size +" |Remainder: " + remainder);
            if (remainder == 0){
                size_f = size;
            }
            if (remainder != 0){
                size_f = size;
            }
            byte[] testRead = new byte[1024];
            fileInputStream.read(testRead);
            MyPackageParse myPackageParse = new MyPackageParse();
            myPackageParse.setSize(size_f);
            myPackageParse.setDataSize(1024);
            myPackageParse.setSeq_num(0);
            myPackageParse.setData(testRead);
            send(myPackageParse);
            ackCheck(myPackageParse);
        }catch (Exception e){
            System.out.println(e.toString());
        }finally {
            fileInputStream.close();
        }
    }

    public void InitUI() {
        JFrame frame = new JFrame();
        frame.setBounds(500, 500, 545, 277);
        frame.setTitle("Sender");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel label = new JLabel("File:");
        label.setFont(new Font("黑体", Font.BOLD, 18));
        label.setBounds(26, 68, 57, 25);
        frame.getContentPane().add(label);

        JLabel lblNewLabel = new JLabel("Code:");
        lblNewLabel.setFont(new Font("黑体", Font.BOLD, 18));
        lblNewLabel.setBounds(26, 119, 57, 25);
        frame.getContentPane().add(lblNewLabel);

        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBounds(105, 68, 300, 25);
        frame.getContentPane().add(textField);
        textField.setColumns(10);

        JTextField textField2 = new JTextField();
        textField2.setBounds(105, 121, 300, 25);
        frame.getContentPane().add(textField2);
        textField2.setColumns(10);

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        JButton button = new JButton("select");
        button.setBounds(425,68,80,25);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = chooser.showOpenDialog(null);
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                if (index == JFileChooser.APPROVE_OPTION) {
                    textField.setText(chooser.getSelectedFile()
                            .getAbsolutePath());
                }
            }
        });

        textField2.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar()<KeyEvent.VK_0||e.getKeyChar()>KeyEvent.VK_9) {
                    e.consume();
                }else if(textField2.getText().length() >= 4) {
                    e.consume();
                }
            }
        });

        JButton button2 = new JButton("start");
        button2.setBounds(205,174,80,25);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textField2.getText().length()==4 && textField.getText().length()>0){
                    fileA = textField.getText();
                    code = textField2.getText();
                    code1 = Integer.parseInt(String.valueOf(code.charAt(0)));
                    code2 = Integer.parseInt(String.valueOf(code.charAt(1)));
                    code3 = Integer.parseInt(String.valueOf(code.charAt(2)));
                    code4 = Integer.parseInt(String.valueOf(code.charAt(3)));
                    frame.dispose();
                    loop = false;
                    System.out.println("File :" + fileA +" | Connect code: " + code1 + code2 + code3 + code4);
                }
            }
        });

        frame.add(button);
        frame.add(button2);
        frame.setVisible(true);
    }

    public static class ProcessBar extends JFrame implements Runnable {
        private JProgressBar progress; // 进度条

        public ProcessBar(String str) {
            super(str);
            progress = new JProgressBar(0, 100); // 实例化进度条

            progress.setStringPainted(true);      // 描绘文字

            progress.setBackground(Color.black); // 设置背景色

            this.add(progress);
            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            this.setBounds(500, 500, 500, 100);
            this.setResizable(false);
            this.setVisible(true);
        }

        public void run() {
            while(progress.getValue()<100){
                try {
                    progress.setValue((finish*100)/size_f);
                    progress.setString(progress.getValue() + "%");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            progress.setString("Finished.");
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.dispose();
        }
    }

    public static void main(String[] args) {
        Send client = new Send();
        try {
            client.InitUI();
            while (loop){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            client.init();
            byte[] recvBuf = new byte[100];
            DatagramPacket recvPacket
                    = new DatagramPacket(recvBuf, recvBuf.length);
            client.receive(recvPacket);
            client.find(recvPacket);
            client.sendData(fileA);
            ProcessBar bar = new ProcessBar("Send Progress");
            Thread tb = new Thread(bar);
            tb.start();
            System.out.println(finish);
            int i = (size - 1)/3;
            int r = (size - 1)%3;
            if(Breakpoint){
                Sender s1 = new Sender(bk1,1+i,-1);
                Sender s2 = new Sender(bk2,1+2*i,-1);
                Sender s3 = null;
                if(remainder==0){s3 = new Sender(bk3,1+3*i+r,remainder);}
                if(remainder!=0){s3 = new Sender(bk3,2+3*i+r,remainder);}
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Thread t1 = new Thread(s1,"A");
                Thread t2 = new Thread(s2,"B");
                Thread t3 = new Thread(s3,"C");

                t1.start();
                t2.start();
                t3.start();
            }else if(!Breakpoint){
                int f = finish;
                Sender s1 = new Sender(f,1+i,-1);
                Sender s2 = new Sender(f+i,1+2*i,-1);
                Sender s3 = null;
                if(remainder==0){s3 = new Sender(f+i+i,1+3*i+r,remainder);}
                if(remainder!=0){s3 = new Sender(f+i+i,2+3*i+r,remainder);}
                System.out.println(f);
                System.out.println(i);
                System.out.println(r);
                System.out.println(size);
                System.out.println(size_f);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Thread t1 = new Thread(s1,"A");
                Thread t2 = new Thread(s2,"B");
                Thread t3 = new Thread(s3,"C");

                t1.start();
                t2.start();
                t3.start();
            }

        } catch (SocketException | UnknownHostException e) {
            System.out.println("Start failed.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
