import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SessionExample extends HttpServlet{

	public void doGet(HttpServletRequest request,
            HttpServletResponse response)
            		throws IOException, ServletException
    {
		response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body bgcolor=\"white\">");
        out.println("<head>");
        
        
        out.println("<title>" + "Sessions Example" + "</title>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<h3>" + "Sessions Example" + "</h3>");
        
        HttpSession session = request.getSession(true);
        out.println("Session ID:" + " " + session.getId());
        out.println("<br>");
        
        out.println("Created:" + " ");
        out.println(new Date(session.getCreationTime()) + "<br>");
        out.println("Last Accessed:" + " ");
        out.println(new Date(session.getLastAccessedTime()));
        
        String dataName = request.getParameter("dataname");
        String dataValue = request.getParameter("datavalue");
        if (dataName != null && dataValue != null) {
            session.setAttribute(dataName, dataValue);
        }

        out.println("<P>");
        
        out.println("The following data is in your session:" + "<br>");
        Enumeration names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement(); 
            String value = session.getAttribute(name).toString();
            out.println(name + " = " 
                        + value + "<br>");
        }

        out.println("<P>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=POST>");
        out.println("Name of Session Attribute:");
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println("Value of Session Attribute:");
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.println("<P>GET based form:<br>");
        out.print("<form action=\"");
        out.print(response.encodeURL("SessionExample"));
        out.print("\" ");
        out.println("method=GET>");
        out.println("Name of Session Attribute:");
        out.println("<input type=text size=20 name=dataname>");
        out.println("<br>");
        out.println("Value of Session Attribute:");
        out.println("<input type=text size=20 name=datavalue>");
        out.println("<br>");
        out.println("<input type=submit>");
        out.println("</form>");

        out.print("<p><a href=\"");
        out.print(response.encodeURL("SessionExample?dataname=foo&datavalue=bar"));
        out.println("\" >URL encoded </a>");
	
        out.println("</body>");
        out.println("</html>");
        
        out.println("</body>");
        out.println("</html>");
        
    }
	
	
	
	public void doPost(HttpServletRequest request,
            HttpServletResponse response)
            		throws IOException, ServletException
   {
		doGet(request, response);
   }
	
	
	
}
