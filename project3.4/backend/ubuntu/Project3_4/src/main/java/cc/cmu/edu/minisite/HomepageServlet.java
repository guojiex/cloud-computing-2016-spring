package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONArray;

import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;

public class HomepageServlet extends HttpServlet {
    private static final String MongoDBDNS = "ec2-52-71-255-182.compute-1.amazonaws.com";
    private MongoClient mongoClient;
    private MongoDatabase db;

    public HomepageServlet() {

    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        mongoClient = new MongoClient(MongoDBDNS, 27017);
        db = mongoClient.getDatabase("test");

        String id = request.getParameter("id");
        JSONObject result = new JSONObject();

        // FindIterable<Document> iterable =
        // db.getCollection("posts").find(eq("uid", id))
        // .sort(new Document("timestamp", 1).append("pid", 1)).limit(30);
        FindIterable<Document> iterable = db.getCollection("posts").find(eq("uid", Integer.parseInt(id)))
                .sort(new Document("timestamp", 1));
        // FindIterable<Document> iterable =
        // db.getCollection("posts").find().limit(10);

        final JSONArray posts = new JSONArray();
        iterable.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                // System.out.println(document);
                //JSONObject temp = new JSONObject();
                posts.put(new JSONObject(document.toJson()));
            }
        });
        result.put("posts", posts);
        PrintWriter writer = response.getWriter();
        //System.out.println(String.format("returnRes(%s)", result.toString()));
        writer.write(String.format("returnRes(%s)", result.toString()));
        writer.close();
        mongoClient.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
