package net.csgeek.soop.web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.csgeek.soop.Driver;

@SuppressWarnings("serial")
public class DOTServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    , IOException {
	String dotFileName = request.getParameter("dotFileName");
	BufferedReader in = new BufferedReader(new FileReader(dotFileName));
	StringBuilder dotBuilder = new StringBuilder();
	String line;
	while((line = in.readLine()) != null) {
	    dotBuilder.append(line);
	    dotBuilder.append("\n");
	}
	in.close();
	String dot = dotBuilder.toString();
	for(String key : Driver.statusMap.keySet()) {
	    	String[] keyParts = key.split("@@@", 2);
	    	if(!dotFileName.equals(keyParts[0])){
	    	    continue;
	    	}
        	Pattern p = Pattern.compile("\"("+keyParts[1]+")\"");
        	Matcher m = p.matcher(dot);
        	dot = m.replaceAll('"'+Driver.statusMap.get(key).getReplacement()+'"');
	}
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(dot);
    }
}
