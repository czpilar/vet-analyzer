package net.czpilar.vet.analyzer.server.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.server.session.SessionManager;
import net.czpilar.vet.analyzer.server.tcp.codec.ProtocolDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ServerProperties.class)
public class TcpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(TcpServerConfig.class);

    @Bean
    public MessageParserRegistry messageParserRegistry() {
        return MessageParserRegistry.createDefault();
    }

    @Bean
    public SessionManager sessionManager(ServerProperties properties) {
        return new SessionManager(properties.getSessionDirectory());
    }

    @Bean
    public CommandLineRunner startTcpServer(ServerProperties properties,
                                            SessionManager sessionManager,
                                            MessageParserRegistry parserRegistry) {
        return args -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            var bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtocolDetector(sessionManager, parserRegistry));
                        }
                    });

            ChannelFuture future = bootstrap.bind(properties.getPort()).sync();
            log.info("Vet Analyzer Server listening on port {}", properties.getPort());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down server...");
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }));
        };
    }
}
