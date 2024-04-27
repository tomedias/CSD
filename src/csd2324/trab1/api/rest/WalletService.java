package csd2324.trab1.api.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import csd2324.trab1.api.Account;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;

@Path(WalletService.Path)
public interface WalletService{


    String Path = "/wallet";
    String ACCOUNT = "account";

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] transfer(SignedTransaction signedTransaction);

    @POST
    @Path("/transfer/atomic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] atomicTransfer(List<SignedTransaction> transactions);

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] balance(@QueryParam(ACCOUNT) String account);

    @GET
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] ledger();

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] test();

    @POST
    @Path("/giveme")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] admin(Transaction transaction);

}
