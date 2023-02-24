import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.AMQImpl;
import org.example.Body;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.rabbitmq.client.ConnectionFactory;
import org.example.rmqpool.RMQChannelFactory;
import org.example.rmqpool.RMQChannelPool;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;


@WebServlet(name = "SwipeServlet", value = "/SwipeServlet")
public class SwipeServlet extends HttpServlet {

    private static final String dataQuene = "swipeinfo";
    private RMQChannelPool new_pool;

//    Gson new_g = new Gson();

    @Override
    public void init() throws ServletException{
        super.init();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setPort(5672);
        Connection connection;
        try{
            connection = connectionFactory.newConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        RMQChannelFactory channelFactory = new RMQChannelFactory(connection);
        this.new_pool = new RMQChannelPool(100, channelFactory);


    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts,res)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("something wrong");
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
//            System.out.println("it is a valid url");
            res.getWriter().write("It works!");
        }
    }



    private boolean isUrlValid(String[] urlPath,HttpServletResponse res) throws IOException {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        int size = urlPath.length;
        if (size != 2) {
            res.getWriter().write("length not right"+ size);
            return false;
        } else {
            if (!urlPath[0].equals("")){
                res.getWriter().write(urlPath[0]);
                return false;
            }else{
                if (!urlPath[1].equals("left") && !urlPath[1].equals("right")){
                    res.getWriter().write("user did not pick");
                    return false;
                }
            }

            return true;
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Invalid path");
            return;
        }

        String[] urlParts = urlPath.split("/");
        String likeornot = urlParts[1];

        if (!isUrlValid(urlParts,resp)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("something wrong");
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            ObjectMapper mapper = new ObjectMapper();


            try {
                Body newBody =  mapper.readValue(sb.toString(), Body.class);
                if (!newBody.validInputBody(newBody.getSwipee(), newBody.getSwiper(), newBody.getComment())){
                    resp.getWriter().write("invalid body input");
                    resp.getWriter().write(mapper.writeValueAsString(newBody));
                }else{
                    if(sendToQuene(newBody, likeornot)){

                        resp.getWriter().write("send to queue successfully");
                    }
                    else{
                        resp.getWriter().write("can not send to queue");
                    }


                }

            }catch (Exception e){
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }


            // Now you can access the data in the JSON request body using json.get("key")
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");


            resp.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
//            System.out.println("it is a valid url;");
//            resp.getWriter().write("Created new ride!");


        }


    }

    public boolean sendToQuene(Body this_body,String likeornot){

        String infoToQuene = this_body.toString();
        infoToQuene = infoToQuene + ", like=" + likeornot;

        Channel new_channel = null;

        try{

            new_channel = this.new_pool.borrowObject();
            new_channel.exchangeDeclare(dataQuene, BuiltinExchangeType.FANOUT,false);
            new_channel.queueDeclare("input_queue1", false,false,false, null);
            new_channel.queueDeclare("input_queue2",false,false,false, null);
            new_channel.queueBind("input_queue1",dataQuene,"");
            new_channel.queueBind("input_queue2",dataQuene,"");
            new_channel.basicPublish("",dataQuene,null,infoToQuene.getBytes(StandardCharsets.UTF_8));
            this.new_pool.returnObject(new_channel);
            return true;

        } catch (IOException e) {
            throw new RuntimeException(e);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }


    }
    public static boolean isNumeric(String strNum) {
        System.out.println(strNum);
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}

