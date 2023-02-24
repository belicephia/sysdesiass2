package org.example.consumer;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;

import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import org.example.rmqpool.RMQChannelFactory;
import org.example.rmqpool.RMQChannelPool;

import javax.servlet.http.HttpServletResponse;


public class LikeAndDislikeCount {
    private static ConcurrentHashMap<Integer, Integer> likeCount = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Integer> dislikeCount = new ConcurrentHashMap<>();


    public static void main (String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setPort(5672);
        Connection connection =  factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("input_queue1",false,false,false,null);

        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String inputFromProducer = new String(body);
                System.out.println(body);
                System.out.println("3");
                Boolean liketheuser = likeornot(inputFromProducer);
                int swiper_ID = findhteswiper(inputFromProducer);
                if (liketheuser){
                    checkifexist(likeCount,swiper_ID);
                }else{
                    checkifexist(dislikeCount,swiper_ID);
                }
            }
        };

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume("input_queue1", true, deliverCallback, consumerTag -> { });
//        System.out.println("1");
//        channel.basicConsume("input_queue1",true,consumer);
//        System.out.println("2");
    }

    public static Boolean likeornot(String message){

        String likeornot = message.substring(message.indexOf(", like="),message.length());
        if (likeornot.contentEquals("left")|| likeornot.contentEquals("LEFT")){
            return false;
        }else{
            return true;
        }
    }

    public static int findhteswiper(String message){
        String substring = message.substring(message.indexOf("swiper="), message.indexOf(", swipee="));
        int swiperId = Integer.parseInt(substring);

        return swiperId;
    }

    public static void checkifexist(ConcurrentHashMap table, Integer swiper){
        if (table.containsKey(swiper)){
            int cur_count = (int) table.get(swiper);
            table.put(swiper, cur_count+1);
        }else{
            table.put(swiper,1);
        }
    }

    public int likeCountRes(Integer swiper){
        return this.likeCount.get(swiper);
    }

    public int dislikeCountRes(Integer swiper){
        return this.dislikeCount.get(swiper);
    }




}
