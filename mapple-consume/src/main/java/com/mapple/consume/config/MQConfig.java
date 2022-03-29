package com.mapple.consume.config;

import com.mapple.consume.entity.MkOrder;
import com.mapple.consume.listener.MkOrderTransactionListener;
import com.mapple.consume.service.MkOrderService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author : Gelcon
 * @date : 2022/3/21 21:28
 */
@Configuration
@Slf4j
@Data
public class MQConfig {

    // 注意都需要私有化
    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;

    @Value("${mq.order.topic}")
    private String messageTopic;

    @Value("${mq.order.tag}")
    private String messageTag;

    @Value("${mq.order.producer.group}")
    private String producerTransactionGroup;

    @Value("${mq.order.consumer.group}")
    private String consumerTransactionGroup;

    @Value("${mq.order.producer.topic}")
    private String messageTransactionTopic;


    @Resource
    private MkOrderService orderService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 实现批量处理，消费对应主题和Tag的消息，然后调用批量处理方法
     * @return  返回DefaultMQPushConsumer，交给Spring去管理
     */

//    @Bean(name = "CustomPushConsumer")
//    public DefaultMQPushConsumer customPushConsumer() throws MQClientException {
//        log.info(consumerGroup + "*******" + nameServer + "*******" + messageTopic);
//        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
//        consumer.setNamesrvAddr(nameServer);
//        consumer.subscribe(messageTopic, "*");
//        // 设置每次消息拉取的时间间隔，单位毫秒
//        consumer.setPullInterval(1000);
//        // 设置每个队列每次拉取的最大消息数
//        consumer.setPullBatchSize(24);
//        // 设置消费者单次批量消费的消息数目上限
//        consumer.setConsumeMessageBatchMaxSize(12);
//        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context)
//                -> {
//            List<MkOrder> orderList = new ArrayList<>(msgs.size());
//            Map<Integer, Integer> queueMsgMap = new HashMap<>(8);
//            msgs.forEach(msg -> {
//                orderList.add(JSONObject.parseObject(msg.getBody(), MkOrder.class));
//                queueMsgMap.compute(msg.getQueueId(), (key, val) -> val == null ? 1 : ++val);
//            });
//            log.info("MkOrderList size: {}, content: {}", orderList.size(), orderList);
//            // 处理批量消息
//            orderService.saveBatch(orderList);
//            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//        });
//        consumer.start();
//        return consumer;
//    }
}
