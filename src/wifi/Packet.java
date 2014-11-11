package wifi;

import java.nio.ByteBuffer;

/**
 * Created by Adam on 11/9/2014.
 */
public class Packet {
    byte[] data;
    int crc;
    ByteBuffer buf;

    public Packet(byte[] frame){
        buf = ByteBuffer.allocate(frame.length);
        buf = ByteBuffer.wrap(frame);
    }

    public Packet(int frameType, short seqNum, short destAddr, short senderAddr, byte[] data){

        if(data != null){
            buf = ByteBuffer.allocate(10 + data.length);
        }
        else{
            buf = ByteBuffer.allocate(10);
        }

        setData(data); //set data first to short circuit
        setFrameType(frameType);
        setRetry(false);
        setSeqNum(seqNum);
        setDestAddr(destAddr);
        setSenderAddr(senderAddr);
        setCRC();
    }

    public void setFrameType(int type){
        type = 0;
    }

    public int getFrameType(){
        return 0;
    }

    public void setRetry(boolean input){

    }

    public void setSeqNum(short seq){
        seq = 0;
    }

    public short getSeqNum(){

    }

    public void setDestAddr(short addr){

    }

    public short getDestAddr(){

    }

    public void setSenderAddr(short addr){

    }

    public short getSenderAddr(){
        return buf.getShort(4);
    }

    public void setData(byte[] inData){
        if(inData != null){

            //Check data
            if(inData.length > 2038){
                throw new IllegalArgumentException("Invalid data.");
            }else{
                for(int i=0;i<inData.length;i++){ //put data bytes
                    buf.put(i+6,inData[i]);
                }
            }
        }
    }

    public byte[] getData(){
        byte[] temp = new byte[buf.limit() - 10];
        for(int i=0;i<temp.length;i++){ //put data bytes
            temp[i]= buf.get(i+6);
        }
        return temp;
    }

    public void setCRC(){
        int crc = 1111;

    }

    public int getCRC(){
        return crc;
    }


    public byte[] getFrame(){
        setCRC();
        return buf.array();
    }

    public String toString(){
        String type;
        switch(getFrameType()){
            case 0:
                type = "DATA";
                break;
            case 1:
                type = "ACK";
                break;
            case 2:
                type = "BEACON";
                break;
            case 4:
                type = "CTS";
                break;
            case 5:
                type = "RTS";
                break;
            default:
                type = "UNKNOWN";
        }
        String out = "<" + type + " " + getSeqNum() + " " + getSenderAddr() + "-->" +
                getDestAddr() + " [" + getData().length + " bytes] (" + getCRC() + ")>"  ;

        return out;
    }
}
