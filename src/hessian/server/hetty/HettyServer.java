package hessian.server.hetty;

import hessian.conf.HettyConfig;
import hessian.model.Application;
import hessian.model.HettyException;
import hessian.plugin.IPlugin;
import hessian.server.BaseServer;
import hessian.server.hetty.core.ServerHttpInitializer;
import hessian.server.hetty.core.ssl.ServerHttpsInitializer;
import hessian.server.hetty.processor.SecurityProcessor;
import hessian.server.hetty.processor.ServiceMetaDataProcessor;
import hessian.server.hetty.processor.ServiceProcessor;
import hessian.startup.ServerStartup;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;

import base.servicecenter.hessian.service.ISpTimeoutService;
import base.util.FileUtil;
import base.util.MiscUtil;
import base.util.StringUtil;

public class HettyServer extends BaseServer {
	private static AtomicBoolean isRunning = new AtomicBoolean(false);
	private ServerBootstrap httpBootstrap = null;
	private ServerBootstrap httpsBootstrap = null;
	private HettyConfig hettyConfig = HettyConfig.getInstance();
	private int httpListenPort;
	private int httpsListenPort;

	public static EventLoopGroup bossGroup = null;

	public static EventLoopGroup workerGroup = null;

	@Autowired
	private ISpTimeoutService spTimeoutService;

	public HettyServer() {
		try {
			hettyConfig.loadPropertyFile("hj.properties");
			log.info("************************************************************");
			log.info("* loading hj.properties file for config netty server *");
			log.info("************************************************************");
		} catch (Exception e) {
			log.fatal("hj.properties is not exist or config is exist some exception!");
			System.exit(-1);
		}
	}

	public HettyServer(String file) {
		hettyConfig.loadPropertyFile(file);
	}

	/**
	 * start hetty
	 */
	public void start() {
		startServer();
	}

	private Future<HettyServer> startServer() {
		FutureTask<HettyServer> future = new FutureTask<HettyServer>(
				new Callable<HettyServer>() {
					@Override
					public HettyServer call() throws Exception {
						synchronized (isRunning) {
							if (isRunning.get()) {
								IllegalStateException is = new IllegalStateException(
										"Server already started.");
								log.fatal(is.getMessage(), is);
								throw is;
							}
							synchronized (ServerStartup.startup) {
								ServerStartup.startup.wait();
							}
							init();
							serverLog();
							isRunning.set(true);
							return HettyServer.this;
						}
					}
				});

		final Thread thread = new Thread(future, "HETTY-SERVER-STARTUP-THREAD");
		thread.start();
		return future;
	}

	private void init() {
		initServerInfo();
		initHettySecurity();
		initPlugins();
		initServiceMetaData();
		if (httpListenPort == -1 && httpsListenPort == -1) {
			httpListenPort = 8081;// default port is 8081
		}
		if (httpListenPort != -1) {
			initHttpBootstrap();
		}
		if (httpsListenPort != -1) {
			initHttpsBootstrap();
		}
	}

	/**
	 * init hetty server info
	 */
	private void initServerInfo() {
		httpListenPort = hettyConfig.getHttpPort();
		httpsListenPort = hettyConfig.getHttpsPort();

		/** 用于分配处理业务线程的线程组个数 */
		// int processorSize = Runtime.getRuntime().availableProcessors();

		bossGroup = new NioEventLoopGroup(hettyConfig.getServerCorePoolSize());
		workerGroup = new NioEventLoopGroup(
				hettyConfig.getServerMaximumPoolSize());

		log.info("httpListenPort: " + httpListenPort);
		log.info("httpsListenPort: " + httpsListenPort);
		log.info("bossGroup Size: " + hettyConfig.getServerCorePoolSize());
		log.info("workerGroup Size: " + hettyConfig.getServerMaximumPoolSize());
	}

	/**
	 * init service metaData
	 */
	private void initHettySecurity() {
		log.info("init hetty security...........");
		Application app = new Application(hettyConfig.getServerKey(),
				hettyConfig.getServerSecret());
		SecurityProcessor.addToApplicationMap(app);
	}

	/**
	 * init plugins
	 */
	private void initPlugins() {
		log.info("init plugins...........");
		List<Class<?>> pluginList = hettyConfig.getPluginClassList();
		try {
			for (Class<?> cls : pluginList) {
				IPlugin p;
				p = (IPlugin) cls.newInstance();
				p.start();
			}
		} catch (InstantiationException e) {
			log.error("init plugin failed.");
			log.error(MiscUtil.traceInfo(e));
		} catch (IllegalAccessException e) {
			log.error("init plugin failed.");
			log.error(MiscUtil.traceInfo(e));
		}
	}

	/**
	 * init service metaData
	 */
	private void initServiceMetaData() {
		log.info("init service MetaData...........");
		ServiceMetaDataProcessor.initMetaDataMap();
	}

	/**
	 * init http bootstrap
	 */
	private void initHttpBootstrap() {
		log.info("init HTTP Bootstrap...........");

		try {
			if (!checkPortConfig(httpListenPort)) {
				throw new IllegalStateException("port: " + httpListenPort
						+ " already in use!");
			}

			httpBootstrap = new ServerBootstrap();
			httpBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.localAddress(httpListenPort)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.SO_REUSEADDR, true)
					// .childOption(ChannelOption.SO_KEEPALIVE, false)
					// .handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ServerHttpInitializer(spTimeoutService));

			httpBootstrap.bind().sync().channel().closeFuture()
					.addListener(cleaner);
		} catch (InterruptedException e) {
			log.fatal(MiscUtil.traceInfo(e));
		}
	}

	/**
	 * init https bootstrap
	 */
	private void initHttpsBootstrap() {
		log.info("init HTTPS Bootstrap...........");

		try {
			if (!checkHttpsConfig()) {
				return;
			}

			if (!checkPortConfig(httpsListenPort)) {
				throw new IllegalStateException("port: " + httpsListenPort
						+ " already in use!");
			}
			httpsBootstrap = new ServerBootstrap();
			httpsBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.localAddress(httpsListenPort)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.SO_REUSEADDR, true)
					// .childOption(ChannelOption.SO_KEEPALIVE, false)
					// .handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ServerHttpsInitializer(spTimeoutService));

			httpsBootstrap.bind().sync().channel().closeFuture()
					.addListener(cleaner);
		} catch (InterruptedException e) {
			log.fatal(MiscUtil.traceInfo(e));
		}
	}

	private final ChannelFutureListener cleaner = new ChannelFutureListener() {
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			log.info("Hetty channel operation complete...");
		}
	};

	/**
	 * check the netty listen port
	 * 
	 * @param listenPort
	 * @return
	 */
	private boolean checkPortConfig(int listenPort) {
		if (listenPort < 0 || listenPort > 65536) {
			throw new IllegalArgumentException("Invalid start port: "
					+ listenPort);
		}
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(listenPort);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(listenPort);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
		} finally {
			if (ds != null) {
				ds.close();
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					log.error(MiscUtil.traceInfo(e));
				}
			}
		}

		return false;
	}

	/**
	 * check https config
	 * 
	 * @return
	 */
	private boolean checkHttpsConfig() {
		if (!StringUtil.isEmptyStr(hettyConfig.getKeyStorePath())) {
			if (!FileUtil.getFile(hettyConfig.getKeyStorePath()).exists()) {
				throw new HettyException(
						"we can't find the file which you configure:[ssl.keystore.file]");
			}
		} else if (!StringUtil.isEmptyStr(hettyConfig.getCertificateKeyFile())
				&& !StringUtil.isEmptyStr(hettyConfig.getCertificateFile())) {
			if (!FileUtil.getFile(hettyConfig.getCertificateKeyFile()).exists()) {
				throw new HettyException(
						"we can't find the file which you configure:[ssl.certificate.key.file]");
			}
			if (!FileUtil.getFile(hettyConfig.getCertificateFile()).exists()) {
				throw new HettyException(
						"we can't find the file which you configure:[ssl.certificate.file]");
			}
		} else {
			throw new HettyException("please check your ssl's config.");
		}

		return true;
	}

	private void serverLog() {
		log.info("devMod:" + hettyConfig.getDevMod());
		log.info("server key:" + hettyConfig.getServerKey());
		log.info("server secret:" + hettyConfig.getServerSecret());
		ServiceProcessor.setDevMod(hettyConfig.getDevMod());
		if (httpListenPort != -1) {
			log.info("Server started, listening for HTTP on port "
					+ httpListenPort);
		}
		if (httpsListenPort != -1) {
			log.info("Server started, listening for HTTPS on port "
					+ httpsListenPort);
		}
	}

	/**
	 * stop hetty
	 */
	public void stop() {
		Future<?> fw = workerGroup.shutdownGracefully();
		Future<?> fb = bossGroup.shutdownGracefully();
		while (!fw.isDone() || !fb.isDone()) {
			if (!fw.isDone())
				log.warn("Work group is not shutdown complete, wait...");
			if (!fb.isDone())
				log.warn("Boss group is not shutdown complete, wait...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}

		log.info("Hetty stop operation complete...!!");
	}

	public static void main(String[] args) {
		new HettyServer().start();
	}
}
