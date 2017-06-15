package base.web.servlet;

import hessian.startup.ServerStartup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import base.task.executor.SpForkJoinPool;
import base.task.executor.ThreadPoolExecutorHelper;
import base.util.MiscUtil;

public class ServerControlServlet extends HttpServlet {

	private final Logger log = Logger.getLogger(ServerControlServlet.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		deal(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		deal(req, resp);
	}

	private void deal(HttpServletRequest req, HttpServletResponse resp) {
		String uri = req.getRequestURI();
		try {
			PrintWriter pw = resp.getWriter();
			BufferedWriter bw = new BufferedWriter(pw);

			if (uri.equals("/mainpool/status"))
				bw.write(ThreadPoolExecutorHelper
						.reportWorkingThreadPoolStatus());
			else if (uri.equals("/hispool/status"))
				bw.write(SpForkJoinPool.reportMyForkJoinPoolStatus());
			else if (uri.equals("/stop/hispwd")) {
				bw.write("going to stop server after 3 seconds...");
				stopServer();
			}

			bw.flush();
			bw.close();
			pw.close();
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
		}
	}

	private void stopServer() {
		ExecutorService excutor = Executors.newFixedThreadPool(1);
		excutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					log.error(MiscUtil.traceInfo(e));
				} finally {
					ServerStartup.close();
				}
			}
		});
	}
}
