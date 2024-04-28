package csd2324.trab1.clients;


import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class RestWalletClient extends RestClient {

	final WebTarget target;

	public RestWalletClient(String serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path(WalletService.Path );
	}


	
	public Result<byte[]> transfer(SignedTransaction signedTransaction,int op_number) {
		return super.reTry(() -> clt_transfer(signedTransaction,op_number));
	}

	
	public Result<byte[]> atomicTransfer(List<SignedTransaction> transactions,int op_number) {
		return super.reTry(() -> clt_atomicTransfer(transactions,op_number));
	}

	
	public Result<byte[]> balance(String account,int op_number) {
		return super.reTry(() -> clt_balance(account,op_number));
	}

	
	public Result<byte[]> ledger(int op_number) {
		return super.reTry(() -> clt_ledger(op_number));
	}

	
	public Result<byte[]> test() {
		return super.reTry(this::clt_test);
	}

	
	public Result<byte[]> giveme(Transaction transaction,int op_number) {
		return super.reTry(() -> clt_admin(transaction,op_number));
	}

	private Result<byte[]> clt_test(){
		Response r = target.path("/").
				request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();
		return super.toJavaResult(r, byte[].class);
	}

	private Result<byte[]> clt_transfer(SignedTransaction signedTransaction,int op_number){
		Response r = target.path("transfer")
				.queryParam(WalletService.OP_NUMBER,op_number)
				.request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.post(Entity.entity(signedTransaction, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, byte[].class);
	}

	private Result<byte[]> clt_atomicTransfer(List<SignedTransaction> transactions,int op_number){
		Response r = target.path("transfer").path("atomic")
				.queryParam(WalletService.OP_NUMBER,op_number)
				.request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.post(Entity.entity(transactions, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, byte[].class);
	}

	private Result<byte[]> clt_balance(String account,int op_number){
		Response r = target.path("balance").queryParam(WalletService.ACCOUNT,account)
				.queryParam(WalletService.OP_NUMBER,op_number)
				.request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();
		return super.toJavaResult(r, byte[].class);
	}

	private Result<byte[]> clt_ledger(int op_number){
		Response r = target.path("ledger")
				.queryParam(WalletService.OP_NUMBER,op_number)
				.request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();
		return super.toJavaResult(r, byte[].class);
	}

	private Result<byte[]> clt_admin(Transaction transaction,int op_number){
		Response r = target.path("giveme")
				.queryParam(WalletService.OP_NUMBER,op_number)
				.request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.post(Entity.entity(transaction, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, byte[].class);
	}
}
