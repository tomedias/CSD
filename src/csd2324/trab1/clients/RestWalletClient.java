package csd2324.trab1.clients;

import csd2324.trab1.api.Signature;
import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Wallet;
import csd2324.trab1.api.rest.WalletService;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import javax.net.ssl.HttpsURLConnection;
import java.util.List;




public class RestWalletClient extends RestClient implements Wallet {

	final WebTarget target;

	public RestWalletClient(String serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path(WalletService.Path );
	}


	@Override
	public Result<Boolean> transfer(String from, String to, double amount, Signature signature) {
		return null;
	}

	@Override
	public Result<Boolean> atomicTransfer(List<Transaction> transactions) {
		return null;
	}

	@Override
	public Result<Double> balance(String account) {
		return null;
	}

	@Override
	public Result<List<Account>> ledger() {
		return null;
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
}
