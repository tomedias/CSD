package csd2324.trab1.api.rest;

import csd2324.trab1.api.Signature;
import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.Transaction;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path(WalletService.Path)
public interface WalletService {


    String Path = "/wallet";
    String FROM = "from";
    String TO = "to";
    String AMOUNT = "amount";
    String ACCOUNT = "account";

    @POST
    @Path("/transfer/{" + FROM + "}/{" + TO + "}/{"  + AMOUNT + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean transfer(@PathParam(FROM) String from, @PathParam(TO) String to, @PathParam(AMOUNT) double amount,Signature signature);

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean atomicTransfer(List<Transaction> transactions);

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
}
