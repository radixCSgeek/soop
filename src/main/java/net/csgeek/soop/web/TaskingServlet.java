package net.csgeek.soop.web;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.csgeek.soop.Driver;
import net.csgeek.soop.Tasking;

import org.codehaus.jackson.map.ObjectMapper;

@SuppressWarnings("serial")
public class TaskingServlet extends HttpServlet {

    private static final ObjectMapper JSON = new ObjectMapper();
    
    class TaskingListEntry {
	public String schedule;
	public String dotFileName;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    , IOException {
	response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);
        List<TaskingListEntry> taskingList = new LinkedList<TaskingListEntry>();
        for(Tasking t: Driver.taskList) {
            TaskingListEntry tle = new TaskingListEntry();
            tle.schedule = t.getSchedule();
            tle.dotFileName = t.getDotFileName();
            taskingList.add(tle);
        }
        response.getWriter().write(JSON.writeValueAsString(taskingList));
    }
}
