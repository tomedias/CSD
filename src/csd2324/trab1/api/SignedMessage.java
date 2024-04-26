package csd2324.trab1.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignedMessage implements Serializable{
    private List<byte[]> messageContent;
    private byte[] question;

    public SignedMessage(byte[] question){
        this.messageContent = new ArrayList<>(10);
        this.question = question;
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent.add(messageContent);
    }

    public byte[] getMessageContent(int id) {
        return this.messageContent.get(id);
    }
    public byte[] getQuestion() {
        return question;
    }


    
}
