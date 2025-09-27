package com.parcelHub.shipment_api.service;

import com.parcelHub.shipment_api.dto.LabelResponseDto;
import com.parcelHub.shipment_api.exception.ShipmentNotFoundException;
import com.parcelHub.shipment_api.mapper.ShipmentMapper;
import com.parcelHub.shipment_api.model.Shipment;
import com.parcelHub.shipment_api.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentMapper shipmentMapper) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentMapper = shipmentMapper;
    }

   public LabelResponseDto getShipment(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(
               () -> new ShipmentNotFoundException("Shipment with id: '" + shipmentId + "' not found")
       );
        return shipmentMapper.mapShipmentToLabelResponseDto(shipment);
   }

}
