package org.example.consumer;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;

import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import org.example.Body;
import org.example.rmqpool.RMQChannelFactory;
import org.example.rmqpool.RMQChannelPool;

import javax.servlet.http.HttpServletResponse;


public class Matches {
    private static ConcurrentHashMap<Integer, ArrayList<Integer>> potMatches = new ConcurrentHashMap<>();




    public void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setPort(5672);
        Connection connection =  factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("input_queue2",false,false,false,null);

        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String inputFromProducer = new String(body);
                Boolean likeornot = likeornot(inputFromProducer);
                if (likeornot){
                    int swiper_Id = findhteswiper(inputFromProducer);
                    int swipee_Id = findhteswipee(inputFromProducer);
                    add_to_lsit(swiper_Id,swipee_Id);
                }
            }
        };

        channel.basicConsume("input_queue2",true,consumer);
    }


    public void add_to_lsit(Integer swiper_Id, Integer swipee_Id){
        if (this.potMatches.containsKey(swiper_Id)){
            ArrayList<Integer> cur_list= this.potMatches.get(swiper_Id);
            if (cur_list.size() < 100){
                cur_list.add(swipee_Id);
                this.potMatches.put(swiper_Id,cur_list);
            }
        }else{
            ArrayList<Integer> new_list = new ArrayList<Integer>();
            new_list.add(swipee_Id);
            this.potMatches.put(swiper_Id,new_list);
        }
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

    public static int findhteswipee(String message){
        String substring = message.substring(message.indexOf("swipee="), message.indexOf(", comment="));
        int swipeeId = Integer.parseInt(substring);

        return swipeeId;
    }

    public ArrayList<Integer> returnPotMatches(Integer swiper_Id){
        if (this.potMatches.containsKey(swiper_Id)){
            return this.potMatches.get(swiper_Id);
        }
        else{
            return new ArrayList<Integer>();
        }
    }


}
