package hessian.server.jetty;

import hessian.server.BaseServer;
import hessian.startup.ServerStartup;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import base.util.MiscUtil;
import base.web.servlet.ServerControlServlet;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;

public class JettyServer extends BaseServer {

	private static AtomicBoolean isRunning = new AtomicBoolean(false);

	private static Server server = null;

	private static final String API = "/apis/";

	private static int port = 8448;

	private static Map<String, ServletHolder> hessianRemotes = new HashMap<String, ServletHolder>();

	public void start() {
		startServer();
	}

	private Future<JettyServer> startServer() {
		FutureTask<JettyServer> future = new FutureTask<JettyServer>(
				new Callable<JettyServer>() {
					@Override
					public JettyServer call() throws Exception {
						synchronized (isRunning) {
							if (isRunning.get()) {
								IllegalStateException is = new IllegalStateException(
										"Server already started.");
								log.fatal(is.getMessage(), is);
								throw is;
							}

							server = new Server();
							SelectChannelConnector connector = new SelectChannelConnector();
							connector.setMaxIdleTime(3000);
							connector.setPort(port);
							server.addConnector(connector);

							ServletContextHandler root = new ServletContextHandler(
									server, "/", ServletContextHandler.SESSIONS);
							root.setResourceBase("./");

							// root.addEventListener(new
							// IntrospectorCleanupListener());
							// root.addEventListener(new
							// ContextLoaderListener());
							// root.addEventListener(new
							// RequestContextListener());

							root.addFilter(
									new FilterHolder(new WebStatFilter()), API
											.concat("*"), EnumSet.of(
											DispatcherType.ASYNC,
											DispatcherType.ERROR,
											DispatcherType.FORWARD,
											DispatcherType.INCLUDE,
											DispatcherType.REQUEST));

							root.addServlet(new ServletHolder(
									new StatViewServlet()), "/druid/*");
							root.addServlet(new ServletHolder(
									new ServerControlServlet()),
									"/mainpool/status");
							root.addServlet(new ServletHolder(
									new ServerControlServlet()),
									"/hispool/status");
							root.addServlet(new ServletHolder(
									new ServerControlServlet()), "/stop/hispwd");
							// root.addServlet(new ServletHolder(
							// new MyCXFServlet()), "/ws/*");
							Set<Entry<String, ServletHolder>> set = hessianRemotes
									.entrySet();
							for (Entry<String, ServletHolder> ent : set) {
								root.addServlet(ent.getValue(),
										API.concat(ent.getKey()));
							}

							int processorSize = Runtime.getRuntime()
									.availableProcessors();

							QueuedThreadPool pool = new QueuedThreadPool();
							pool.setMaxThreads(processorSize * 2);
							pool.setMinThreads(processorSize);

							server.setThreadPool(pool);

							synchronized (ServerStartup.startup) {
								ServerStartup.startup.wait();
							}

							server.start();

							isRunning.set(true);

							log.info("Server started, listening for HTTP on port "
									+ port);

							return JettyServer.this;
						}
					}
				});

		final Thread thread = new Thread(future, "JETTY-SERVER-STARTUP-THREAD");
		thread.start();

		return future;
	}

	@Override
	public void stop() {
		try {
			server.stop();
			log.info("Jetty stop operation complete...!!");
		} catch (Exception e) {
			log.error(MiscUtil.traceInfo(e));
		}
	}

	public void setPort(int port) {
		JettyServer.port = port;
	}

	public void setHessianRemotes(Map<String, ServletHolder> hessianRemotes) {
		JettyServer.hessianRemotes = hessianRemotes;
	}
}