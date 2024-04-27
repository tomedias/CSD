package csd2324.trab1.api;

public class SignedMessage<T>{
    private byte[] ledger_used_hash;
    private T result;
    public SignedMessage(byte[] content,T result){
        this.ledger_used_hash = content;
        this.result = result;

    }

    public byte[] getLedger_used_hash() {
        return ledger_used_hash;
    }

    public T getResult() {
        return result;
    }
}
