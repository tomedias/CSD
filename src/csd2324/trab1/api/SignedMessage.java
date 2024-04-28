package csd2324.trab1.api;

public class SignedMessage<T>{
    private byte[] ledger_used_hash;
    private T result;
    private int op_number = 0;
    public SignedMessage(byte[] content,T result,int op_number){
        this.ledger_used_hash = content;
        this.result = result;
        this.op_number = op_number;

    }

    public byte[] getLedger_used_hash() {
        return ledger_used_hash;
    }

    public T getResult() {
        return result;
    }

    public int getOp_number() {
        return op_number;
    }
}
