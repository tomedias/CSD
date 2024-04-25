package csd2324.trab1.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SignedMessage implements Serializable{
    private byte[] messageContent;
    private List<byte[]> signatures;
    private byte[] answer;

    public SignedMessage(){
        this.signatures = new ArrayList<>();
    }
    public SignedMessage(byte[] content){
        this.messageContent = content;
        this.signatures = new ArrayList<>();
    }

    public void setMessageContent(byte[] messageContent) {
        this.messageContent = messageContent;
    }

    public byte[] getAnswer() {
        return answer;
    }

    public void setAnswer(byte[] answer) {
        this.answer = answer;
    }

    public byte[] getMessageContent() {
        return messageContent;
    }

    public List<byte[]> getSignatures() {
        return signatures;
    }

    public void addSignature(byte[] signature){
        this.signatures.add(signature);
    }

    
}
