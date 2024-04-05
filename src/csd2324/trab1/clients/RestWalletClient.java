package csd2324.trab1.clients;

import csd2324.trab1.api.Signature;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.Account;
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
	public Result<Boolean> transfer(String from, String to, double amount, Signature signature) {
		return super.reTry(() -> clt_transfer(from, to, amount, signature));
	}

	@Override
	public Result<Boolean> atomicTransfer(List<Transaction> transactions) {
		return super.reTry(() -> clt_atomicTransfer(transactions));
	}

	@Override
	public Result<Double> balance(String account) {
		return super.reTry(() -> clt_balance(account));
	}

	@Override
	public Result<List<Account>> ledger() {
		return super.reTry(() -> clt_ledger());
	}

	@Override
	public Result<String> test() {
		return super.reTry(() -> clt_test());
	}

	private Result<String> clt_test(){
		Response r = target.path("/").
				request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, String.class);
	}

	private Result<Boolean> clt_transfer(String from, String to, double amount, Signature signature){
		Response r = target.path("transfer").path(from).path(to).path(String.valueOf(amount))
				.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(signature, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Boolean.class);
	}

	private Result<Boolean> clt_atomicTransfer(List<Transaction> transactions){
		Response r = target.path("transfer")
				.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(transactions, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r, Boolean.class);
	}

	private Result<Double> clt_balance(String account){
		Response r = target.path("balance").path(account)
				.request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, Double.class);
	}

	private Result<List<Account>> clt_ledger(){
		Response r = target.path("ledger")
				.request().accept(MediaType.APPLICATION_JSON)
				.get();
		return super.toJavaResult(r, new GenericType<List<Account>>() {});
	}
}
