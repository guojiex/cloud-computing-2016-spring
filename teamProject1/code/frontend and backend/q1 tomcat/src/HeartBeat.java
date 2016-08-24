

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.Decrypt;

/**
 * Servlet implementation class HeartBeat
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/q1" })
public final class HeartBeat extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HeartBeat() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		HeartBeat.df.setTimeZone(timeZoneNY);
	}
	private static final String AWS_ACCOUNT_ID="505243408493";
	private static final String TEAM_NAME="MyLittlePony";
	private static final TimeZone timeZoneNY = TimeZone.getTimeZone("America/New_York");
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		if(request.getParameter("key")==null){
			out.print("server alive,key missing");
			return;
		}
		
		Date date = new Date(System.currentTimeMillis());  
		out.print(String.format("%s,%s\n%s\n%s\n",TEAM_NAME,AWS_ACCOUNT_ID,df.format(date)
				,Decrypt.getDecrypt(request.getParameter("key"),request.getParameter("message"))));
	}

}
