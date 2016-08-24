package undertow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import model.Decrypt;

public class Q1Handler implements HttpHandler {
    private static final long serialVersionUID = 1L;
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";
    private static final TimeZone timeZoneNY = TimeZone.getTimeZone("America/New_York");
    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    @Override
    public void handleRequest(HttpServerExchange input) throws Exception {
        if(input.isInIoThread()){
            input.dispatch(this);
            return;
        }
        input.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        if(input == null || input.getQueryParameters().get("key")==null
                ||input.getQueryParameters().get("message")==null){
            input.getResponseSender().send("q1 parameter missing");
            return;
        }
        
        Date date = new Date(System.currentTimeMillis());  
        input.getResponseSender().send(String.format("%s,%s\n%s\n%s\n",TEAM_NAME,AWS_ACCOUNT_ID,df.format(date)
                ,Decrypt.getDecrypt(input.getQueryParameters().get("key").peek(),input.getQueryParameters().get("message").peek())));
    }

}
