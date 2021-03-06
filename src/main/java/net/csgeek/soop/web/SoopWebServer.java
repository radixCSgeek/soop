package net.csgeek.soop.web;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

public class SoopWebServer {
    private static final Logger LOG = Logger.getLogger(SoopWebServer.class);
    
    private Server server = null;
    
    public void launch() throws Exception {
	if(server != null) throw new IllegalStateException("Attempted to launch web server when one already exists!");
	
	LOG.info("Starting web UI");
	server = new Server(8080);
	ServletHandler servlet_handler = new ServletHandler();
	servlet_handler.addServletWithMapping(DocketFileServlet.class, "/docketService");
	servlet_handler.addServletWithMapping(DOTServlet.class, "/dotService");
	servlet_handler.addServletWithMapping(TaskingServlet.class, "/tasking");

	// Create the ResourceHandler. It is the object that will actually
	// handle the request for a given file. It is
	// a Jetty Handler object so it is suitable for chaining with other
	// handlers as you will see in other examples.
	ResourceHandler resource_handler = new ResourceHandler();
	// Configure the ResourceHandler. Setting the resource base indicates
	// where the files should be served out of.
	// In this example it is the current directory but it can be configured
	// to anything that the jvm has access to.
	resource_handler.setWelcomeFiles(new String[] { "index.html" });
	resource_handler.setResourceBase("web");

	// Add the ResourceHandler to the server.
	HandlerList handlers = new HandlerList();
	handlers.setHandlers(new Handler[] { resource_handler, servlet_handler });
	server.setHandler(handlers);
	server.start();
    }
    
    public void waitFor() throws InterruptedException {
	server.join();
	server = null;
    }

}
