package csd2324.trab1.api.rest;

import csd2324.trab1.api.Signature;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.net.UnknownHostException;
import java.util.List;

@Path(WalletService.Path)
public interface WalletService {


    String Path = "/wallet";
    String FROM = "from";
    String TO = "to";
    String AMOUNT = "amount";
    String ACCOUNT = "account";
    String COMMAND = "command";
    String SECRET = "secret";
    String SIGNATURE = "signature";

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean transfer(@QueryParam(SIGNATURE)  String  signature,Transaction transaction);

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean atomicTransfer(List<Transaction> transactions,List<String> signatures);

    @GET
    @Path("/balance/{" + ACCOUNT + "}")
    @Produces(MediaType.APPLICATION_JSON)
    double balance(@PathParam(ACCOUNT) String account);

    @GET
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_JSON)
    List<Account> ledger();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String test();

    @POST
    @Path("/admin/{" + COMMAND + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    boolean admin(@PathParam(COMMAND) String command, List<String> args,@QueryParam(SECRET) String secret);

}
