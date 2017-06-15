package hessian.server.hetty.core.ssl;

import hessian.conf.HettyConfig;
import hessian.server.hetty.core.HettySimpleHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLException;

import base.servicecenter.hessian.service.ISpTimeoutService;

public class HettySslHandler extends HettySimpleHandler {

	public HettySslHandler(ISpTimeoutService spTimeoutService) {
		super(spTimeoutService);
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		// Get the SslHandler in the current pipeline.
		final SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
		// Get notified when SSL handshake is done.
		sslHandler.handshakeFuture().addListener(new SslListener());
	}

	private class SslListener implements GenericFutureListener<Future<Channel>> {
		@Override
		public void operationComplete(Future<Channel> future) throws Exception {
			if (!future.isSuccess()) {
				log.error(future.cause().getMessage(), future.cause());
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// We have to redirect to https://, as it was targeting http://
		// Redirect to the root as we don't know the url at that point
		if (cause.getCause() instanceof SSLException) {
			log.error(cause.getCause().getMessage(), cause.getCause());
			InetSocketAddress remoteAddr = (InetSocketAddress) ctx.channel()
					.remoteAddress();
			ctx.pipeline().remove("ssl");
			HttpResponse response = new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT);
			response.headers().add(
					HttpHeaders.Names.LOCATION,
					"https://" + remoteAddr.getHostName() + ":"
							+ HettyConfig.getInstance().getHttpsPort() + "/");
			ChannelFuture writeFuture = ctx.write(response);
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		} else {
			super.exceptionCaught(ctx, cause);
		}
	}
}
