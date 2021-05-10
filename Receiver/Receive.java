import service.MyPackageParse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.sleep;

public class Receive {
    private static List<MyPackageParse> fileR = new LinkedList<>();
    private static DatagramSocket ds = null;
    final static String SERVER_ADDR = "10.69.84.195";
    private static boolean loop = true;
    private static boolean loop2 = true;
    private static boolean loop3 = true;
    private static InetAddress dest;
    private static int destPort;
    private static String fileA = "";
    private static String code = "";
    private static int code1 = 0;
    private static int code2 = 0;
    private static int code3 = 0;
    private static int code4 = 0;
    private static int finish = 0;
    private static int size_f = 0;
    private static int bk1 = -1;
    private static int bk2 = -1;
    private static int bk3 = -1;
    private static boolean breakpoint = false;

    public boolean send(DatagramPacket dp) {
        try {
            ds.send(dp);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void init() throws SocketException, UnknownHostException {
        ds = new DatagramSocket();
        byte[] test = new byte[]{2, (byte) code1, (byte) code2, (byte) code3, (byte) code4, 0, 2};
        InetAddress addr = InetAddress.getByName(SERVER_ADDR);
        int port = 10000;
        DatagramPacket sendPacket
                = new DatagramPacket(test, test.length, addr, port);
        try {
            ds.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (this.ds != null) {
            this.ds.close();
        }
    }


    public void receive(DatagramPacket dp) throws IOException {
        this.ds.receive(dp);
    }

    public static boolean new_file(List<MyPackageParse> list, int current_seq) {
        for (MyPackageParse i : list) {
            if (i.getSeq_num() == current_seq) {
                return false;
            }
        }
        return true;
    }

    public static boolean is_ordered(List<MyPackageParse> list, int flag) {
        if (list != null && flag > -1) {
            for (MyPackageParse i : fileR) {
                if (flag > i.getSeq_num()) {
                    return false;
                } else if (flag < i.getSeq_num()){
                    flag = i.getAck_num();
                }else if(flag == i.getSeq_num()){
                    flag ++;
                }
            }
        }
        return true;
    }

    public static void send_ack(MyPackageParse parse) {
        if (ds != null) {
            MyPackageParse resPacket = new MyPackageParse();
            if (breakpoint == false) {
                resPacket.setAck_num(parse.getSeq_num());
            } else {
                resPacket.setAck_num(0);
                resPacket.setbreakpoint();
                resPacket.setBk1(bk1);
                resPacket.setBk2(bk2);
                resPacket.setBk3(bk3);
                breakpoint = false;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = null;
            try {
                os = new ObjectOutputStream(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.writeObject(resPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] dataR = outputStream.toByteArray();
            DatagramPacket res = new DatagramPacket(dataR, dataR.length, dest, destPort);
            try {
                ds.send(res);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Send ACK to sender: " + resPacket.getAck_num());
        } else {
            System.out.println("Can't find socket.");
        }
    }

    public static void set_breakpoint(List<MyPackageParse> list) throws IOException {
        if (list.size() != 0) {
            int seq = list.get(list.size() - 1).getSeq_num();
            System.out.println(seq);
            FileOutputStream fouta = new FileOutputStream(fileA + ".a",
                    true);
            FileOutputStream foutb = new FileOutputStream(fileA + ".b",
                    true);
            FileOutputStream foutc = new FileOutputStream(fileA + ".c",
                    true);
            bk1 = 1;
            bk2 = 1 + (size_f-1)/3;
            System.out.println(bk2);
            bk3 = 1 + ((size_f-1)/3)*2;
            System.out.println(bk3);
            for (MyPackageParse i : fileR) {
                if(i.getSeq_num()>=0 && i.getSeq_num()<=(1+(size_f-1)/3)) {
                    bk1 = i.getSeq_num();
                    fouta.write(i.getData());
                    fouta.flush();
                }else if(i.getSeq_num()>(1+(size_f-1)/3) && i.getSeq_num()<=(1+((size_f-1)/3)*2)){
                    bk2 = i.getSeq_num();
                    foutb.write(i.getData());
                    foutb.flush();
                }else if(i.getSeq_num()>(1+((size_f-1)/3))*2){
                    bk3 = i.getSeq_num();
                    foutc.write(i.getData());
                    foutc.flush();
                }
            }
            fouta.close();
            foutb.close();
            foutc.close();
            DataOutputStream fota = new DataOutputStream(
                    new FileOutputStream(fileA + ".ab"));
            DataOutputStream fotb = new DataOutputStream(
                    new FileOutputStream(fileA + ".bb"));
            DataOutputStream fotc = new DataOutputStream(
                    new FileOutputStream(fileA + ".cb"));
            fota.writeInt(bk1);
            fotb.writeInt(bk2);
            fotc.writeInt(bk3);
            System.out.println(1+((size_f-1)/3)*2);
            System.out.println(bk1);
            System.out.println(bk2);
            System.out.println(bk3);
            fota.close();
            fotb.close();
            fotc.close();
            System.out.println("Time out and current progress will be saved.");
        } else {
            System.out.println("Time out.");
        }
    }

    public void InitUI() {
        JFrame frame = new JFrame();
        frame.setBounds(500, 500, 550, 300);
        frame.setTitle("Receiver");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel label = new JLabel("Directory:");
        label.setFont(new Font("黑体", Font.BOLD, 18));
        label.setBounds(10, 68, 120, 25);
        frame.getContentPane().add(label);

        JLabel label1 = new JLabel("Code:");
        label1.setFont(new Font("黑体", Font.BOLD, 18));
        label1.setBounds(10, 119, 57, 25);
        frame.getContentPane().add(label1);

        JLabel label2 = new JLabel("Name:");
        label2.setFont(new Font("黑体", Font.BOLD, 18));
        label2.setBounds(10, 170, 57, 25);
        frame.getContentPane().add(label2);

        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBounds(135, 68, 300, 25);
        frame.getContentPane().add(textField);
        textField.setColumns(10);

        JTextField textField2 = new JTextField();
        textField2.setBounds(135, 121, 300, 25);
        frame.getContentPane().add(textField2);
        textField2.setColumns(10);

        JTextField textField3 = new JTextField();
        textField3.setBounds(135, 174, 300, 25);
        frame.getContentPane().add(textField3);
        textField3.setColumns(10);

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设置选择模式，既可以选择文件又可以选择文件夹

        JButton button = new JButton("select");
        button.setBounds(450, 68, 80, 25);
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
                if (e.getKeyChar() < KeyEvent.VK_0 || e.getKeyChar() > KeyEvent.VK_9) {
                    e.consume();
                } else if (textField2.getText().length() >= 4) {
                    e.consume();
                }
            }
        });

        JButton button2 = new JButton("start");
        button2.setBounds(240, 217, 80, 25);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textField2.getText().length() == 4 &&
                        textField.getText().length() > 0 && textField3.getText().length() > 0) {
                    fileA = textField.getText() + "\\" + textField3.getText();
                    code = textField2.getText();
                    code1 = Integer.parseInt(String.valueOf(code.charAt(0)));
                    code2 = Integer.parseInt(String.valueOf(code.charAt(1)));
                    code3 = Integer.parseInt(String.valueOf(code.charAt(2)));
                    code4 = Integer.parseInt(String.valueOf(code.charAt(3)));
                    frame.dispose();
                    loop2 = false;
                    System.out.println("File :" + fileA + " | Connect code: " + code1 + code2 + code3 + code4);
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
            while (progress.getValue() < 100 && loop3) {
                try {
                    if (size_f != 0) {
                        progress.setValue((finish * 100) / size_f);
                        progress.setString(progress.getValue() + "%");
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (loop3) {
                progress.setString("Finished.");
            } else {
                progress.setString("Breakpoint set.");
            }
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.dispose();
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Receive server = new Receive();
        Thread tb = null;
        try {
            server.InitUI();
            while (loop2) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long startTime = System.currentTimeMillis();
            System.out.println("start");
            server.init();
            File a = new File(fileA + ".ab");
            File b = new File(fileA + ".bb");
            File c = new File(fileA + ".cb");
            if (a.exists() || b.exists() || c.exists()) {
                if(a.exists()){
                    DataInputStream ina = new DataInputStream(new FileInputStream(a));
                    bk1 = ina.readInt();
                }
                if(b.exists()){
                    DataInputStream inb = new DataInputStream(new FileInputStream(b));
                    bk2 = inb.readInt();
                }
                if(c.exists()){
                    DataInputStream inc = new DataInputStream(new FileInputStream(c));
                    bk3 = inc.readInt();
                }
                breakpoint = true;
                System.out.println("Find breakpoint, sequence number is :" + bk1 +"|" +bk2+ "|" + bk3);
            } else {
                System.out.println("No breakpoint.");
            }
            ProcessBar bar = new ProcessBar("Receive Progress");
            tb = new Thread(bar);
            tb.start();
            while (loop) {
                byte[] buffer = new byte[2048];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                server.receive(dp);
                ds.setSoTimeout(10000);
                byte[] data = dp.getData();
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream msg = new ObjectInputStream(in);
                MyPackageParse parse = (MyPackageParse) msg.readObject();
                dest = dp.getAddress();
                destPort = dp.getPort();
                if (size_f==0){
                    size_f = parse.getSize();
                }
                System.out.println("Receive : " + parse.getDataSize() + "byte  |From : " + dest + destPort
                        + " |Sequence number :" + parse.getSeq_num());
                Collections.sort(fileR, Comparator.comparingInt(MyPackageParse::getSeq_num));

                if (new_file(fileR, parse.getSeq_num()) && breakpoint == false) {
                    fileR.add(parse);
                    send_ack(parse);
                    finish++;
                    int flag = fileR.get(0).getSeq_num();

                    if (fileR.size() != 0) {
                        if (fileR.get(fileR.size() - 1).isEnd_flag()) {
                            finish++;
                            if (is_ordered(fileR, flag)) {
                                FileOutputStream fouta = new FileOutputStream(
                                        fileA + ".a", true);
                                FileOutputStream foutb = new FileOutputStream(
                                        fileA + ".b", true);
                                FileOutputStream foutc = new FileOutputStream(
                                        fileA + ".c", true);
                                for (MyPackageParse i : fileR) {
                                    if(i.getSeq_num()>=0 && i.getSeq_num()<=(1+(size_f-1)/3)) {
                                        fouta.write(i.getData());
                                        fouta.flush();
                                    }else if(i.getSeq_num()>(1+(size_f-1)/3) && i.getSeq_num()<=(1+((size_f-1)/3)*2)){
                                        foutb.write(i.getData());
                                        foutb.flush();
                                    }else if(i.getSeq_num()>(1+((size_f-1)/3)*2)){
                                        foutc.write(i.getData());
                                        foutc.flush();
                                    }
                                }
                                fouta.close();
                                foutb.close();
                                foutc.close();
                                File fia = new File(fileA + ".a");
                                File fib = new File(fileA + ".b");
                                File fic = new File(fileA + ".c");
                                if (fia.exists()&&fib.exists()&&fic.exists()) {
                                    InputStream inb = new FileInputStream(fileA + ".b");
                                    InputStream inc = new FileInputStream(fileA + ".c");
                                    OutputStream outa = new FileOutputStream(fileA+ ".a",true);
                                    byte[] buffer1 = new byte[1024];
                                    while (true) {
                                        int byteRead = inb.read(buffer1);
                                        if (byteRead == -1)
                                            break;
                                        outa.write(buffer1, 0, byteRead);
                                    }
                                    while (true) {
                                        int byteRead = inc.read(buffer1);
                                        if (byteRead == -1)
                                            break;
                                        outa.write(buffer1, 0, byteRead);
                                    }

                                    File fio = new File(fileA);
                                    outa.close();
                                    fia.renameTo(fio);
                                    inb.close();
                                    inc.close();
                                    fib.delete();
                                    System.out.println("deleteb");
                                    fic.delete();
                                    System.out.println("deletec");
                                    System.out.println("rename");
                                }
                                File fiab = new File(fileA + ".ab");
                                File fibb = new File(fileA + ".bb");
                                File ficb = new File(fileA + ".cb");

                                if (fiab.exists()) {
                                    fiab.delete();
                                    System.out.println("delete");
                                }
                                if (fibb.exists()) {
                                    fibb.delete();
                                    System.out.println("delete");
                                }
                                if (ficb.exists()) {
                                    ficb.delete();
                                    System.out.println("delete");
                                }
                                System.out.println("Finished.");
                                loop = false;
                            }
                        }
                    }
                } else if (new_file(fileR, parse.getSeq_num()) && breakpoint) {
                    finish = bk1+bk2-(1+(size_f-1)/3)+bk3-(1+((size_f-1)/3)*2);
                    System.out.println(finish);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    send_ack(parse);
                } else if (!new_file(fileR, parse.getSeq_num())) {
                    send_ack(parse);
                } else {
                    System.out.println("Ack sending error.");
                }
            }
            long endTime = System.currentTimeMillis();
            System.out.println("Time: " + (endTime - startTime) + "ms");
        } catch (SocketException | UnknownHostException e) {
            System.out.println("Failed to start.");
        } catch (SocketTimeoutException e) {
            set_breakpoint(fileR);
            loop3 = false;
            System.out.println("Exit.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.close();
        }
    }
}
