package edu.vt.ece4564.example;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

//MainServlet class, display "Mainservlet" on any unknown request
public class MainServlet extends HttpServlet {
	public static void main(String[] args) throws Exception {
		// Using PORT 8888
		Server server = new Server(8888);

		// Set up server
		WebAppContext context = new WebAppContext();
		context.setWar("war");
		context.setContextPath("/");
		server.setHandler(context);

		server.start();
		server.join();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.getWriter().write("Jam with me Server! See API for usage");
	}
}
