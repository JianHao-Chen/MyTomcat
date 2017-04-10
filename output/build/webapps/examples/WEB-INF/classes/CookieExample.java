
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class CookieExample extends HttpServlet{

	public void doGet(HttpServletRequest request,
        HttpServletResponse response)throws IOException, ServletException
     {
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body bgcolor=\"white\">");
        out.println("<head>");
        
        out.println("<title>" + "Cookies Example" + "</title>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("<h3>" + "Cookies Example" + "</h3>");
        
        Cookie[] cookies = request.getCookies();
        if ((cookies != null) && (cookies.length > 0)) {
            out.println("Your browser is sending the following cookies:" + "<br>");
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                out.print("Cookie Name: " +cookie.getName()+ "<br>");
                out.println("  Cookie Value: " + cookie.getValue()
                            + "<br><br>");
            }
        } else {
            out.println("Your browser isn't sending any cookies");
        }
        
        
        String cookieName = request.getParameter("cookiename");
        String cookieValue = request.getParameter("cookievalue");
        if (cookieName != null && cookieValue != null) {
            Cookie cookie = new Cookie(cookieName, cookieValue);
            response.addCookie(cookie);
            out.println("<P>");
            out.println("You just sent the following cookie to your browser:" + "<br>");
            out.print("Name" + "  " +cookieName+ "<br>");
            out.print("Value" + "  " + cookieValue);
        }
        
        out.println("<P>");
        out.println("Create a cookie to send to your browser" + "<br>");
        out.print("<form action=\"");
        out.println("CookieExample\" method=POST>");
        out.print("Name" + "  ");
        out.println("<input type=text length=20 name=cookiename><br>");
        out.print("Value" + "  ");
        out.println("<input type=text length=20 name=cookievalue><br>");
        out.println("<input type=submit></form>");
            
            
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
