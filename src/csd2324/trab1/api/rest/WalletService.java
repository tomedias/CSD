package csd2324.trab1.api.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import csd2324.trab1.api.SignedTransaction;
import csd2324.trab1.api.Transaction;

@Path(WalletService.Path)
public interface WalletService{


    String Path = "/wallet";
    String ACCOUNT = "account";
    String OP_NUMBER = "op_number";

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] transfer(SignedTransaction signedTransaction, @QueryParam(OP_NUMBER) int op_number);

    @POST
    @Path("/transfer/atomic")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] atomicTransfer(List<SignedTransaction> transactions, @QueryParam(OP_NUMBER) int op_number);

    @GET
    @Path("/balance")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] balance(@QueryParam(ACCOUNT) String account, @QueryParam(OP_NUMBER) int op_number);

    @GET
    @Path("/ledger")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] ledger(@QueryParam(OP_NUMBER) int op_number);

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] test();

    @POST
    @Path("/giveme")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    byte[] admin(Transaction transaction,@QueryParam(OP_NUMBER) int op_number);

}
