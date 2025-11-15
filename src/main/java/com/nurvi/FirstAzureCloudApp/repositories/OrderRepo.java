package com.nurvi.FirstAzureCloudApp.repositories;


import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<OrderEntity, Integer> {
}
