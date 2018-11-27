package ds.gae.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;
import ds.gae.view.ViewTools;
import ds.gae.view.JSPSite;

@SuppressWarnings("serial")
public class ConfirmQuotesServlet extends HttpServlet {
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		HttpSession session = req.getSession();
		HashMap<String, ArrayList<Quote>> allQuotes = (HashMap<String, ArrayList<Quote>>) session.getAttribute("quotes");

		try {
			ArrayList<Quote> qs = new ArrayList<Quote>();
			
			for (String crcName : allQuotes.keySet()) {
				qs.addAll(allQuotes.get(crcName));
			}
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(bos);
		    oos.writeObject(qs);
		    byte[] bytes = bos.toByteArray();
			
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(TaskOptions.Builder.withUrl("/worker")
					.param("quotes", bytes));
			
			// CarRentalModel.get().confirmQuotes(qs);
			
			session.setAttribute("quotes", new HashMap<String, ArrayList<Quote>>());
			
			// TODO
			// If you wish confirmQuotesReply.jsp to be shown to the client as
			// a response of calling this servlet, please replace the following line 
			// with resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
			resp.sendRedirect(JSPSite.CREATE_QUOTES.url());
		} catch (Exception e) {
			session.setAttribute("errorMsg", ViewTools.encodeHTML(e.getMessage()));
			resp.sendRedirect(JSPSite.RESERVATION_ERROR.url());				
		}
	}
}
