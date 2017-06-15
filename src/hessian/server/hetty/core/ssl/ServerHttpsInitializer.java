package hessian.server.hetty.core.ssl;

import hessian.conf.HettyConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;

import base.servicecenter.hessian.service.ISpTimeoutService;

public class ServerHttpsInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger log = Logger
			.getLogger(ServerHttpsInitializer.class);

	private final HettyConfig hettyConfig = HettyConfig.getInstance();

	private ISpTimeoutService spTimeoutService;

	public ServerHttpsInitializer(ISpTimeoutService spTimeoutService) {
		this.spTimeoutService = spTimeoutService;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		String mode = hettyConfig.getClientAuth();
		// Add SSL handler first to encrypt and decrypt everything.
		SSLEngine engine = SslContextFactory.getServerContext()
				.createSSLEngine();
		engine.setUseClientMode(false);
		if ("want".equalsIgnoreCase(mode)) {
			engine.setWantClientAuth(true);
		} else if ("need".equalsIgnoreCase(mode)) {
			engine.setNeedClientAuth(true);
		}
		engine.setEnableSessionCreation(true);

		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast("ssl", new SslHandler(engine));
		// pipeline.addLast(new HttpRequestDecoder());
		// pipeline.addLast(new ChunkedWriteHandler());
		// pipeline.addLast(new HttpResponseEncoder());
		// pipeline.addLast(new HttpContentCompressor());
		pipeline.addLast("http", new HttpServerCodec());
		pipeline.addLast("aggregates", new HttpObjectAggregator(65536));
		pipeline.addLast("hetty", new HettySslHandler(spTimeoutService));
	}
}