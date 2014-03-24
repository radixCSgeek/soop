package net.csgeek.soop.web;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.lf5.util.StreamUtils;

import static net.csgeek.soop.Constants.*;

@SuppressWarnings("serial")
public class DocketFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    , IOException {
    	FileInputStream in = new FileInputStream(CONFIG_FILE);
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        StreamUtils.copyThenClose(in, response.getOutputStream());
    }
}
