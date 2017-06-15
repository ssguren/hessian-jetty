package hessian.server.hetty.core;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.RejectedExecutionException;

import base.task.executor.ThreadPoolExecutorHelper;

public class HettyWithoutTimeoutHandler extends HettySimpleHandler {

	public HettyWithoutTimeoutHandler() {
		super(null);
	}

	protected void handleService(final ChannelHandlerContext ctx,
			final ByteBuf content, final String username,
			final String password, final String spId, final String clientIp,
			final String serviceName) {
		try {
			MyJob job = new MyJob(ctx, content, username, password, spId,
					clientIp, serviceName);
			ThreadPoolExecutorHelper.workingThreadPool.submit(job);
		} catch (RejectedExecutionException exception) {
			writeResponse(ctx, "���������࣬������ͣ",
					HttpResponseStatus.TOO_MANY_REQUESTS, true);
		}
	}
}