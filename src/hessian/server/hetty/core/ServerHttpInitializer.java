package hessian.server.hetty.core;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import org.apache.log4j.Logger;

import base.servicecenter.hessian.service.ISpTimeoutService;

public class ServerHttpInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger log = Logger
			.getLogger(ServerHttpInitializer.class);

	private ISpTimeoutService spTimeoutService;

	public ServerHttpInitializer(ISpTimeoutService spTimeoutService) {
		this.spTimeoutService = spTimeoutService;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// pipeline.addLast(new HttpRequestDecoder());
		// pipeline.addLast(new ChunkedWriteHandler());
		// pipeline.addLast(new HttpResponseEncoder());
		// pipeline.addLast(new HttpContentCompressor());
		pipeline.addLast("http", new HttpServerCodec());
		pipeline.addLast("aggregates", new HttpObjectAggregator(65536));
		// pipeline.addLast("hetty", new HettyWithoutTimeoutHandler());
		pipeline.addLast("hetty", new HettySimpleHandler(spTimeoutService));
	}
}
