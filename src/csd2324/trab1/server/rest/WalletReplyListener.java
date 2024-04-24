package csd2324.trab1.server.rest;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;

import java.util.Arrays;

public class WalletReplyListener implements ReplyListener {

    AsynchServiceProxy serviceProxy;
    private int replies = 0;
    public WalletReplyListener(AsynchServiceProxy serviceProxy) {
        this.serviceProxy = serviceProxy;
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

        replies++;

        double q = Math.ceil((double) (serviceProxy.getViewManager().getCurrentViewN() + serviceProxy.getViewManager().getCurrentViewF() + 1) / 2.0);

        if (replies >= q) {
            System.out.println("[RequestContext] clean request context id: " + requestContext.getReqId());
            serviceProxy.cleanAsynchRequest(requestContext.getOperationId());
        }
    }
}
