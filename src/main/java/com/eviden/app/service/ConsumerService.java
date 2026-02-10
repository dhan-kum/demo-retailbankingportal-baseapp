
package com.eviden.app.service;

import com.rabbitmq.client.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
public class ConsumerService {

    public void connect(final String QUEUE_NAME) throws Exception{
        try (Connection connection = getConnection()) {
            try (Channel channel = connection.createChannel()) {
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                               byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println("Received message: " + message);
                    }
                };
                System.out.println("consumer " + consumer);
            }
        }
    }

    Connection getConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        return factory.newConnection();
    }
}
