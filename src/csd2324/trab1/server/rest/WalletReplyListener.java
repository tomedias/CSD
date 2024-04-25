package csd2324.trab1.server.rest;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.TOMUtil;
import csd2324.trab1.api.SignedMessage;

import java.nio.ByteBuffer;
import java.security.Signature;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class WalletReplyListener implements ReplyListener {

    private static Logger Log = Logger.getLogger(WalletReplyListener.class.getName());

    AsynchServiceProxy serviceProxy;
    private int replies = 0;
    private CountDownLatch latch;
    private SignedMessage message;
    public WalletReplyListener(AsynchServiceProxy serviceProxy, CountDownLatch latch, SignedMessage message) {
        this.serviceProxy = serviceProxy;
        this.latch = latch;
        this.message = message;
    }

    @Override
    public void reset() {
        System.out.println("[RequestContext] The proxy is re-issuing the request to the replicas");
        replies = 0;
    }

    @Override
    public void replyReceived(RequestContext requestContext, TOMMessage tomMessage) {
        StringBuilder builder = new StringBuilder();
        builder.append("[RequestContext] id: " + requestContext.getReqId() + " type: " + requestContext.getRequestType());
        builder.append(" [TOMMessage reply] sender id: " + tomMessage.getSender() + " Hash content: " + Arrays.toString(tomMessage.getContent()));
        System.out.println(builder.toString());
        ByteBuffer buffer = ByteBuffer.wrap(tomMessage.getContent());
        int l = buffer.getInt();
        byte[] reply = new byte[l];
        buffer.get(reply);
        l = buffer.getInt();
        byte[] signature = new byte[l];
        buffer.get(signature);
        this.message.setMessageContent(reply);
        this.message.addSignature(signature);
        replies++;
        latch.countDown();
        double q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 2.0);
        if (replies >= q) {
            System.out.println("[RequestContext] clean request context id: " + requestContext.getReqId());
            serviceProxy.cleanAsynchRequest(requestContext.getOperationId());
        }
    }
}
