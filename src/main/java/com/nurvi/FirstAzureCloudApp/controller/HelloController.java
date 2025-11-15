package com.nurvi.FirstAzureCloudApp.controller;

import com.nurvi.FirstAzureCloudApp.repositories.OrderEntity;
import com.nurvi.FirstAzureCloudApp.repositories.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/hello")
public class HelloController {

    @Autowired
    private OrderRepo orderRepo;

    @GetMapping
    public String sayHello(){
        return "Hello from First Azure Cloud App!\nRe-dployed";
    }

    @GetMapping("/allOrders")
    public List<OrderEntity> getAllOrders(){
        // This method should ideally call a service to fetch orders from the database.
        // For demonstration purposes, we'll return an empty list here.
        return orderRepo.findAll();
    }

}
