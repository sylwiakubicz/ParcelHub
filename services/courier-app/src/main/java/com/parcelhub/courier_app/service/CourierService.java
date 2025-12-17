package com.parcelhub.courier_app.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CourierService {

    public void collect(String courierId, UUID shipmentId) {}

    public void arrivedAtHub(String courierId, String hubId, UUID shipmentId) {}
}
