package undertow;

import java.net.MalformedURLException;
import java.sql.SQLException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class Q2Handler implements HttpHandler {

    /**
     * @param requester
     * @throws MalformedURLException
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Q2Handler() throws MalformedURLException, ClassNotFoundException, SQLException {
        super();
        requester = new Q2MysqlRequester("localhost");
    }

    Q2MysqlRequester requester = null;

    

    private static final long serialVersionUID = 1L;
    private static final String AWS_ACCOUNT_ID = "505243408493";
    private static final String TEAM_NAME = "MyLittlePony";
//    private static MyLinkedHashMap<String,String> cache=new MyLinkedHashMap<String,String>(100000, (float)0.75, true);

    @Override
    public void handleRequest(HttpServerExchange input) throws Exception {
        if(input.isInIoThread()){
            input.dispatch(this);
            return;
        }
        if (input == null || input.getQueryParameters().get("userid") == null
                || input.getQueryParameters().get("hashtag") == null) {
            input.getResponseSender().send("q2 parameter missing");
            return;
        }
        String userid = input.getQueryParameters().get("userid").peek();
        String hashtag = input.getQueryParameters().get("hashtag").peek();
        if(userid==null||userid.isEmpty()||hashtag==null||hashtag.isEmpty()){
            input.getResponseSender().send("q2 parameter missing");
            return;
        }
        input.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=UTF-8");
//        String targetId=String.format("%s%s", userid,hashtag);
//        String result=cache.get(targetId);
//        if(result!=null){
//            input.getResponseSender().send(result);
//            return;
//        }
        try {
            String line = String.format("%s,%s\n%s\n", TEAM_NAME, AWS_ACCOUNT_ID,
                    requester.getResponse(userid, hashtag));
//            cache.put(targetId, line);
            input.getResponseSender().send(line);
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
