package org.geworkbenchweb.authentication;

import org.vaadin.appfoundation.authentication.data.User;
 
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException; 
import java.util.Enumeration; 
 
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet that confirms the email address of the user.
 * 
 * @author Min You
 */
public class ConfirmUserServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8011738912148914245L;

	protected static Log log = LogFactory.getLog(ConfirmUserServlet.class);	 

	public static final String SERVLET_URI = "servlet/ConfirmUser";

	public static final String PARAM_USER_ID = "userID";
	public static final String PARAM_KEY = "key";

	public final void init() throws ServletException {
		super.init();

	}

	 
	public final void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	 
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("-------->" + request.getRequestURI());

		String forwardLocation = execute(request, response);
		forwardLocation = request.getRequestURL().toString().split("/servlet")[0] + forwardLocation;
		response.sendRedirect(forwardLocation);	
		 
		response.encodeURL(forwardLocation);

	}

	/**
	 * Used internally to handle servlet execution.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String execute(HttpServletRequest request,
			HttpServletResponse response) {

		ParameterMap params = new ParameterMap();

		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String name = (String) paramNames.nextElement();
			params.put(name, request.getParameterValues(name));
		}

		String userIDString = params.getParameter(PARAM_USER_ID);
		String keyString = params.getParameter(PARAM_KEY);
		Long userID = new Long(userIDString);

		User user = FacadeFactory.getFacade().find(User.class, userID);
	
		if (user != null) {
			if (user.getReasonForLockedAccount() == null)
				return "/VAADIN/pages/error.html";
			String dbKey = user.getReasonForLockedAccount().split("\\(")[0];
			if (keyString.equals(dbKey)) {
				if (user.isAccountLocked()) {
					user.setAccountLocked(false);
					user.setReasonForLockedAccount(keyString + "(user account has been confirmed.)");
					FacadeFactory.getFacade().store(user);			 
				}
				HttpSession session = request.getSession(false);
				if (session != null) {
				    //session.invalidate();
				    session.removeAttribute("com.vaadin.terminal.gwt.server.WebApplicationContext");
				    
				}			 
				return "/VAADIN/pages/confirmed.html";
			}

		}

		return "/VAADIN/pages/error.html";

	}

}
