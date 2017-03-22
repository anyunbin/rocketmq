package com.alibaba.rocketmq.client;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;


public class SimpleConsumerProducerTest {
    private static final String TOPIC_TEST = "TopicTest";

    @Test
    public void producerConsumerTest() throws MQClientException, InterruptedException {
        //System.setProperty("rocketmq.namesrv.domain", "jmenv.tbsite.alipay.net");
        System.setProperty("rocketmq.namesrv.addr", "localhost:9876");
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("S_fundmng_demo_producer");
        DefaultMQProducer producer = new DefaultMQProducer("P_fundmng_demo_producer");

        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.subscribe(TOPIC_TEST, null);

        final AtomicLong lastReceivedMills = new AtomicLong(System.currentTimeMillis());


        consumer.registerMessageListener(new MessageListenerConcurrently() /*MessageListenerOrderly顺序消费、MessageListenerConcurrently并发消费*/{
            public ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
                                                            final ConsumeConcurrentlyContext context) {
                System.out.println("Received " + msgs.get(0) + " messages !");

                lastReceivedMills.set(System.currentTimeMillis());

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        producer.start();

        for (int i = 0; i < 100000; i++) {
            try {
                Message msg = new Message(TOPIC_TEST, ("Hello RocketMQ " + i).getBytes());
                SendResult sendResult = producer.send(msg);
                System.out.println(sendResult);
                Thread.sleep(1000);
            } catch (Exception e) {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        // wait no messages
        while ((System.currentTimeMillis() - lastReceivedMills.get()) < 5000) {
            TimeUnit.MILLISECONDS.sleep(200);
        }

        consumer.shutdown();
        producer.shutdown();
    }
}
