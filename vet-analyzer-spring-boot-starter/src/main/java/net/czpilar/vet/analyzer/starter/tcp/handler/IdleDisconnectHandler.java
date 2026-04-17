package net.czpilar.vet.analyzer.starter.tcp.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Closes the channel when it becomes idle (no data received within timeout).
 * This triggers channelInactive on downstream handlers, which properly closes the session.
 */
public class IdleDisconnectHandler extends ChannelDuplexHandler {

    private static final Logger log = LoggerFactory.getLogger(IdleDisconnectHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            log.warn("Connection idle, closing: {}", ctx.channel().remoteAddress());
            ctx.close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
