
import javax.servlet.*;
import javax.servlet.http.*;

public class PitListServlet extends HttpServlet
{
     
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException 
     {
     	String action=request.getParameter("action");
     	if (action==null)action="";
     }
    
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException 
     {
        doGet(request, response);
     }
  
}