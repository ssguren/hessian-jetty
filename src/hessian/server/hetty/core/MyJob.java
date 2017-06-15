package hessian.server.hetty.core;

import hessian.model.RequestWrapper;
import hessian.server.hetty.processor.ServiceMetaData;
import hessian.server.hetty.processor.ServiceMetaDataProcessor;
import hessian.server.hetty.processor.ServiceProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import base.util.MiscUtil;
import base.util.SemaphoreUtil;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianFactory;
import com.caucho.hessian.io.HessianInputFactory;
import com.caucho.hessian.io.HessianInputFactory.HeaderType;

public class MyJob implements Callable<Boolean> {

	private final Logger log = Logger.getLogger(MyJob.class);

	private final static HessianInputFactory inputFactory = new HessianInputFactory();

	private final static HessianFactory hessianFactory = new HessianFactory();

	private final ChannelHandlerContext ctx;

	private final ByteBuf content;

	private final String username;

	private final String password;

	private final String spId;

	private final String clientIp;

	private final String serviceName;

	private String errMsg = null;

	public MyJob(final ChannelHandlerContext ctx, final ByteBuf content,
			final String username, final String password, final String spId,
			final String clientIp, final String serviceName) {
		this.ctx = ctx;
		this.content = content;
		this.username = username;
		this.password = password;
		this.spId = spId;
		this.clientIp = clientIp;
		this.serviceName = serviceName;
	}

	@Override
	public Boolean call() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		boolean res = false;
		boolean acquired = false;
		AbstractHessianInput in = null;
		AbstractHessianOutput out = null;

		try {
			acquired = SemaphoreUtil.acquire(spId);
			if (acquired) {
				InputStream is = new ByteArrayInputStream(content.array());

				RequestWrapper rw = new RequestWrapper(username, password,
						spId, clientIp, serviceName);

				HeaderType header = inputFactory.readHeader(is);
				if (HeaderType.HESSIAN_2.equals(header)) {
					in = hessianFactory.createHessian2Input(is);
					in.readCall();
					out = hessianFactory.createHessian2Output(os);

					res = handleRequest(rw, in, out);
				} else {
					errMsg = "����֧�ֵ�HessianЭ�� -- " + header;
				}
			} else {
				errMsg = "spId=" + spId + ": ��Դ���󲢷����";
			}
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
			errMsg = "Hessian�����ȡʧ��1";
		} finally {
			if (acquired)
				SemaphoreUtil.release(spId);
		}

		writeResponse(ctx, os);

		return res;
	}

	private boolean handleRequest(RequestWrapper rw, AbstractHessianInput in,
			AbstractHessianOutput out) {
		Object result = null;
		try {
			// backward compatibility for some frameworks that don't read the
			// call
			// type first
			in.skipOptionalCall();

			String serviceName = rw.getServiceName();
			ServiceMetaData metaData = ServiceMetaDataProcessor
					.getServiceMetaData(serviceName);
			if (metaData == null) {
				log.error("service " + serviceName + " can't found.");
				errMsg = "spId=" + spId + ": �Ҳ������� -- " + serviceName;

				return false;
			}

			String methodName = in.readMethod();
			int argLength = in.readMethodArgLength();

			Method method = metaData.getMethod(methodName);
			if (method == null) {
				log.error("method " + methodName + " can't found.");
				errMsg = "spId=" + spId + ": �Ҳ������� -- " + methodName;

				return false;
			}

			Class<?>[] argTypes = method.getParameterTypes();
			if (argLength != argTypes.length && argLength >= 0) {
				log.error("method " + methodName + " parameters not match.");
				errMsg = "spId=" + spId + ": ��������ƥ�� -- " + methodName;

				return false;
			}

			Object[] argObjs = new Object[argTypes.length];
			for (int i = 0; i < argTypes.length; i++) {
				argObjs[i] = in.readObject(argTypes[i]);
			}

			rw.setMethodName(method.getName());
			rw.setArgs(argObjs);
			rw.setArgsTypes(argTypes);

			// invoke
			result = ServiceProcessor.invokeMethod(rw);

			return result != null;
		} catch (Throwable t) {
			log.error(MiscUtil.traceInfo(t));

			if (t instanceof IOException) {
				errMsg = "Hessian�����ȡʧ��2";
			} else {
				errMsg = "spId=" + spId + ": �ӿڵ��÷����쳣";
			}

			return false;
		} finally {
			try {
				in.close();

				out.writeReply(result);
				out.close();
			} catch (IOException e) {// never happen
				log.error(MiscUtil.traceInfo(e));
				errMsg = "Hessian���д��ʧ��";
			}
		}
	}

	private void writeResponse(ChannelHandlerContext ctx,
			ByteArrayOutputStream os) {
		if (errMsg == null) {
			DefaultFullHttpResponse response = new DefaultFullHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					Unpooled.copiedBuffer(os.toByteArray()));
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
					"x-application/hessian");
			response.headers().set(HttpHeaders.Names.CONTENT_ENCODING,
					CharsetUtil.UTF_8.displayName());
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
					response.content().readableBytes());

			// Write the response.
			ctx.writeAndFlush(response)
					.addListener(ChannelFutureListener.CLOSE);

			os = null;
		} else {
			DefaultFullHttpResponse response = new DefaultFullHttpResponse(
					HttpVersion.HTTP_1_1,
					HttpResponseStatus.SERVICE_UNAVAILABLE,
					Unpooled.copiedBuffer(errMsg, CharsetUtil.UTF_8));
			response.headers()
					.set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
			response.headers().set(HttpHeaders.Names.CONTENT_ENCODING,
					CharsetUtil.UTF_8.displayName());
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
					response.content().readableBytes());

			// Write the response.
			ctx.writeAndFlush(response)
					.addListener(ChannelFutureListener.CLOSE);
		}
	}
}