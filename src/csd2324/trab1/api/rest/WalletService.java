package csd2324.trab1.api.rest;


import csd2324.trab1.server.java.Account;
import csd2324.trab1.server.java.SignedTransaction;

import csd2324.trab1.server.java.Transaction;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path(WalletService.Path)
public interface WalletService{


    String Path = "/wallet";
    String ACCOUNT = "account";
    String QUANTITY = "quantity";
    String ADMIN = "admin";

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean transfer(SignedTransaction signedTransaction);

    @POST
    @Path("/transfer/atomic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    boolean atomicTransfer(List<SignedTransaction> transactions);

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_JSON)
    double balance(@QueryParam(ACCOUNT) String account);

    @GET
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_JSON)
    List<Account> ledger();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String test();

    @POST
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    boolean admin(Transaction transaction);

}
