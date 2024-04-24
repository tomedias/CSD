package csd2324.trab1.clients;


import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.SignedTransaction;
import csd2324.trab1.server.java.Transaction;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class RestWalletClient extends RestClient implements Wallet {

	final WebTarget target;

	public RestWalletClient(String serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path(WalletService.Path );
	}


	@Override
	public Result<Void> transfer(SignedTransaction signedTransaction) {
		return super.reTry(() -> clt_transfer(signedTransaction));
	}

	@Override
	public Result<Void> atomicTransfer(List<SignedTransaction> transactions) {
		return super.reTry(() -> clt_atomicTransfer(transactions));
	}

	@Override
	public Result<Double> balance(String account) {
		return super.reTry(() -> clt_balance(account));
	}

	@Override
	public Result<List<Account>> ledger() {
		return super.reTry(this::clt_ledger);
	}

	@Override
	public Result<String> test() {
		return super.reTry(this::clt_test);
	}

	@Override
	public Result<Void> giveme(Transaction transaction) {
		return super.reTry(() -> clt_admin(transaction));
	}

	private Result<String> clt_test(){
		Response r = target.path("/").
				request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, String.class);
	}

	private Result<Void> clt_transfer(SignedTransaction signedTransaction){
		Response r = target.path("transfer")
				.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(signedTransaction, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);
	}

	private Result<Void> clt_atomicTransfer(List<SignedTransaction> transactions){
		Response r = target.path("transfer").path("atomic")
				.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(transactions, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);
	}

	private Result<Double> clt_balance(String account){
		Response r = target.path("balance").queryParam(WalletService.ACCOUNT,account)
				.request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, Double.class);
	}

	private Result<List<Account>> clt_ledger(){
		Response r = target.path("ledger")
				.request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, new GenericType<>() {});
	}

	private Result<Void> clt_admin(Transaction transaction){
		Response r = target.path("giveme")
				.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(transaction, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Void.class);
	}
}
