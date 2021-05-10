package service;

import java.io.Serializable;

public class MyPackageParse implements Serializable {
    int dataSize;
    int size = 0;
    int seq_num;
    int ack_num;
    int check_data;
    int bk1 = -1;
    int bk2 = -1;
    int bk3 = -1;
    boolean End_flag;
    byte[] data;
    boolean Breakpoint = false;

    public void setSeq_num(int seq_num) {
        this.seq_num = seq_num;
    }

    public void setAck_num(int ack_num) {
        this.ack_num = ack_num;
    }

    public void setCheck_data(int check_data) {
        this.check_data = check_data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setEnd_flag(boolean end_flag) {
        End_flag = end_flag;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public void setBk1(int bk1) {
        this.bk1 = bk1;
    }

    public void setBk2(int bk2) {
        this.bk2 = bk2;
    }

    public void setBk3(int bk3) {
        this.bk3 = bk3;
    }

    public int getAck_num() {
        return ack_num;
    }

    public int getCheck_data() {
        return check_data;
    }

    public int getSeq_num() {
        return seq_num;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getSize() {
        return size;
    }

    public int getBk1() {
        return bk1;
    }

    public int getBk2() {
        return bk2;
    }

    public int getBk3() {
        return bk3;
    }

    public boolean isEnd_flag() {
        return End_flag;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isBreakpoint(){ return Breakpoint; }

    public void setbreakpoint(){ this.Breakpoint = true; }
}
