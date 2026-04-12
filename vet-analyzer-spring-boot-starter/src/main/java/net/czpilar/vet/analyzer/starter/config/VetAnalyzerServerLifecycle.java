package net.czpilar.vet.analyzer.starter.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.starter.tcp.codec.ProtocolDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.List;

/**
 * Manages the Netty TCP server lifecycle within Spring application context.
 * Implements SmartLifecycle for proper startup/shutdown ordering.
 */
public class VetAnalyzerServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(VetAnalyzerServerLifecycle.class);

    private final VetAnalyzerProperties properties;
    private final MessageParserRegistry parserRegistry;
    private final List<AnalyzerMessageListener> listeners;

    private MultiThreadIoEventLoopGroup bossGroup;
    private MultiThreadIoEventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running = false;

    public VetAnalyzerServerLifecycle(VetAnalyzerProperties properties,
                                      MessageParserRegistry parserRegistry,
                                      List<AnalyzerMessageListener> listeners) {
        this.properties = properties;
        this.parserRegistry = parserRegistry;
        this.listeners = listeners;
    }

    @Override
    public void start() {
        try {
            bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

            var bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtocolDetector(parserRegistry, listeners));
                        }
                    });

            serverChannel = bootstrap.bind(properties.getPort()).sync().channel();
            running = true;
            log.info("Vet Analyzer TCP server started on port {}", properties.getPort());
        } catch (Exception e) {
            log.error("Failed to start Vet Analyzer TCP server on port {}", properties.getPort(), e);
            stop();
            throw new RuntimeException("Failed to start Vet Analyzer TCP server", e);
        }
    }

    @Override
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        running = false;
        log.info("Vet Analyzer TCP server stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1; // Start late, stop early
    }
}
