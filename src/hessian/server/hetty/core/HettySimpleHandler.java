package hessian.server.hetty.core;

import hessian.server.TimeOutJobHandler;
import hessian.server.hetty.processor.SecurityProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import base.servicecenter.hessian.service.ISpTimeoutService;
import base.task.executor.ThreadPoolExecutorHelper;
import base.util.StringUtil;

public class HettySimpleHandler extends ChannelInboundHandlerAdapter {

	protected final Logger log = Logger.getLogger(getClass());

	protected ISpTimeoutService spTimeoutService;

	private ByteBuf byteBuf = null;

	private FullHttpRequest request = null;

	public HettySimpleHandler(ISpTimeoutService spTimeoutService) {
		this.spTimeoutService = spTimeoutService;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.error("Channel Error: ");
		log.error(cause.getCause().getMessage(), cause.getCause());
		ctx.close();
	}

	@Override
	public final void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof FullHttpRequest) {
			request = (FullHttpRequest) msg;

			ByteBuf content = request.content();
			byteBuf = Unpooled.copiedBuffer(content);
			ReferenceCountUtil.release(request);

			String uri = request.getUri();

			if (!uri.startsWith("/apis/")) {
				writeResponse(ctx, HttpResponseStatus.NOT_FOUND.reasonPhrase(),
						HttpResponseStatus.NOT_FOUND, true);
				return;
			}

			getClientIP(ctx);
			parseAuthInfo();

			String clientIp = request.headers().get("ClientIP");
			String username = request.headers().get("UserName");
			String password = request.headers().get("PassWord");
			String spId = request.headers().get("SpId");

			boolean isRight = SecurityProcessor.checkPermission(username,
					password);
			if (!isRight) {
				writeResponse(ctx, "鉴权失败", HttpResponseStatus.UNAUTHORIZED,
						true);
				return;
			}

			if (!TimeOutJobHandler.isJobPass(spId)) {
				writeResponse(ctx, "spId=" + spId + ": 资源当前被锁定，请求被拒绝处理",
						HttpResponseStatus.LOCKED, true);
				return;
			}

			String serviceName = uri.substring(uri.lastIndexOf("/") + 1);
			handleService(ctx, byteBuf, username, password, spId, clientIp,
					serviceName);
		}
	}

	private void getClientIP(ChannelHandlerContext ctx) {
		String clientIP = request.headers().get("X-Real-IP");
		if (StringUtil.isEmptyStr(clientIP)) {
			InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel()
					.remoteAddress();
			clientIP = remoteAddress.getAddress().getHostAddress();
		}

		request.headers().add("ClientIP", clientIP);
	}

	protected void handleService(final ChannelHandlerContext ctx,
			final ByteBuf content, final String username,
			final String password, final String spId, final String clientIp,
			final String serviceName) {
		Future<Boolean> future = null;
		try {
			MyJob job = new MyJob(ctx, content, username, password, spId,
					clientIp, serviceName);
			future = ThreadPoolExecutorHelper.workingThreadPool.submit(job);

			if (future != null && spTimeoutService != null) {
				boolean obj = future.get(
						spTimeoutService.getWorkingTimeout(spId) + 3000,
						TimeUnit.MILLISECONDS);
				if (!obj)
					log.info("Future get result failed! spId=" + spId);
			}
		} catch (RejectedExecutionException exception) {
			writeResponse(ctx, "并发请求过多，服务暂停",
					HttpResponseStatus.TOO_MANY_REQUESTS, true);
		} catch (TimeoutException e) {
			log.error("spId="
					+ spId
					+ ": Service ["
					+ serviceName
					+ "] Callable job executed time out! Now will let thread die.");
			TimeOutJobHandler.incrJobTimeoutCount(spId);
			future.cancel(true);

			writeResponse(ctx, "spId=" + spId + ": 资源查询超时",
					HttpResponseStatus.REQUEST_TIMEOUT, true);
		} catch (Throwable t) {
			log.error(
					"spId=" + spId + ": Service [" + serviceName
							+ "] Callable job executing found some exection: "
							+ t.getMessage(), t);
			TimeOutJobHandler.incrJobTimeoutCount(spId);
			future.cancel(true);

			writeResponse(ctx, "服务器内部错误",
					HttpResponseStatus.INTERNAL_SERVER_ERROR, true);
		}
	}

	private void parseAuthInfo() {
		String auths = request.headers().get("Authorization");

		if (auths != null) {
			String auth[] = auths.split(" ");
			String bauth = auth[1];
			String dauth = new String(Base64.decodeBase64(bauth));
			String authInfo[] = dauth.split(":");

			if (authInfo != null) {
				if (authInfo.length > 1) {
					request.headers().add("UserName", authInfo[0]);
					request.headers().add("PassWord", authInfo[1]);
				}

				if (authInfo.length > 2) {
					request.headers().add("SpId", authInfo[2]);
				}
			}
		}
	}

	protected final void writeResponse(ChannelHandlerContext ctx,
			String content, HttpResponseStatus status, boolean forceClose) {
		boolean ka = forceClose ? false : HttpHeaders.isKeepAlive(request);

		DefaultFullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(content,
						CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
				"text/plain;charset=".concat(CharsetUtil.UTF_8.displayName()));
		response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
				response.content().readableBytes());

		if (ka) {
			response.headers().set(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
			// Write and flush the response.
			ctx.writeAndFlush(response);
		} else {
			// Write and flush the response and close the connection.
			ctx.writeAndFlush(response)
					.addListener(ChannelFutureListener.CLOSE);
		}
	}
}