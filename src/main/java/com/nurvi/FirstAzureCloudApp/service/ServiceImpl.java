package com.nurvi.FirstAzureCloudApp.service;

import com.nurvi.FirstAzureCloudApp.repositories.OrderEntity;
import com.nurvi.FirstAzureCloudApp.repositories.OrderRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceImpl {

    Logger logger = LoggerFactory.getLogger(ServiceImpl.class);
    @Autowired
    OrderRepo orderRepo;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate2;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public static boolean manualCheck = true;

    //upload bulk data from a csv file to the database
    public boolean uploadBulkOrders(){
        logger.info("Inside uploadBulkOrders");
        try {
//            sendKafkaMessage("Hello Kafka..");
            List<OrderEntity> orderEntities = null;//readCsv();

            if(orderEntities != null) {
                System.out.println("Total records to be inserted: " + orderEntities.size());
                //inserting to database
                int[][] ints = jdbcTemplate.batchUpdate("insert into test (id,name,address) values(?,?,?)"
                        , orderEntities
                        , 50
                        , (ps, orderDetails) -> {
                            ps.setInt(1, orderDetails.getId());
                            ps.setString(2, orderDetails.getName());
                            ps.setString(3, orderDetails.getAddress());
                        });
                System.out.println("Batch update completed." + Arrays.toString(ints));
                int totalInserted = ints.length;
                System.out.println("Total records inserted: " + totalInserted);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return false;
        }
        return true;
    }

    private static List<OrderEntity> readCsv() {
        //read csv file and upload to database
        try {
            return Files.lines(Paths.get("C:/Users/Vozon/Downloads/bulk_dummy_data.csv"))
                    .skip(1)
                    .map(x->x.split(","))
                    .map(record ->  {
                        OrderEntity order = new OrderEntity();
                        order.setId(Integer.parseInt(record[0]));
                        order.setName(record[1]);
                        order.setAddress(record[2]);
                        return order;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
    }

    private void sendBulkOrdersTransactional() {

        List<OrderEntity> orders = orderRepo.findAll();
        kafkaTemplate.executeInTransaction(trans -> {

            logger.info("Starting Kafka Transaction...");

            for (OrderEntity order : orders) {
                logger.info("Sending message for Order {}", order.getId());

                // Send to main topic - MUST USE `trans`
                trans.send("my-topic",
                        "Order Id: " + order.getId()
                                + ", Name: " + order.getName()
                                + ", Address: " + order.getAddress());

                // Second topic inside the same Kafka transaction
//                testKafkaAtomicity(trans, order.getId());

                // Simulate error after some messages
//                if (order.getId() > 20) {
//                    throw new RuntimeException("Simulated failure after sending some messages");
//                }
            }

            logger.info("Kafka Transaction Complete.");
            return true;
        });

        // If exception occurs above → TRANS IS ABORTED automatically
    }

    // -------------------------------
    // All atomic messages must use `trans`
    // -------------------------------

    private void testKafkaAtomicity(KafkaOperations<String, String> trans, int orderId) {
        logger.info("Inside testKafkaAtomicity for order {}", orderId);

        trans.send("my-topic-atomicity",
                "Atomicity test for Order ID: " + orderId);
    }

    // -------------------------------
    // Consumers
    // -------------------------------

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void consumeMyTopic(String message) {
        logger.info("Consumer-1 received: {}", message);

        // THIS PRODUCER IS NON-TRANSACTIONAL — OK FOR SIMPLE CHAIN EVENTS
        kafkaTemplate.send("my-topic-2", "Event triggered from first consumer: " + message);
    }

    @KafkaListener(topics = "my-topic-2", groupId = "my-group")
    public void consumeTopic2(String message) {
        logger.info("Consumer-2 received: {}", message);
    }

    @KafkaListener(topics = "my-topic-atomicity", groupId = "my-group")
    public void consumeAtomicity(String message) {
        logger.info("Atomicity Topic received: {}", message);
    }

    public Page<OrderEntity> getPageData(int pageNo, int pageSize) {
        Pageable pageable= PageRequest.of(pageNo,pageSize);
        return orderRepo.findAll(pageable);
    }
}
