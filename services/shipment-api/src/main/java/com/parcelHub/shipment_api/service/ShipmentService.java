package com.parcelHub.shipment_api.service;

import com.parcelHub.shipment_api.dto.*;
import com.parcelHub.shipment_api.exception.ShipmentNotFoundException;
import com.parcelHub.shipment_api.mapper.ShipmentMapper;
import com.parcelHub.shipment_api.model.Shipment;
import com.parcelHub.shipment_api.model.ShipmentStatus;
import com.parcelHub.shipment_api.repository.ShipmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShipmentService {

    private final String baseUrl = "https://labels.local";

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @PersistenceContext
    private EntityManager entityManager;

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

   @Transactional
   public CreateShipmentResponseDto createShipment(CreateShipmentRequestDto requestDto) {
        Shipment shipment = shipmentMapper.mapCreateShipmentRequestDtoToShipment(requestDto);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setId(UUID.randomUUID());

        Long next = nextLabelSeq();
        String labelNumber = format(next);

        try {
            shipment.setLabelNumber(labelNumber);
            shipment.setLabelUrl(baseUrl + "/" + labelNumber + ".pdf");

            shipment = shipmentRepository.save(shipment);

        } catch (DataIntegrityViolationException e) {
            if (requestDto.getClientRequestId() != null) {
                shipment = shipmentRepository.findByClientRequestId(requestDto.getClientRequestId()).orElseThrow();
            } else {
                throw e;
            }
        }

        // TODO: ZAPIS DO OUTBOXEVENT

        return shipmentMapper.mapShipmentToCreateShipmentResponseDto(shipment);
   }

   public InitiateReturnResponseDto initiateReturn(UUID shipmentId, InitiateReturnRequestDto initiateReturnRequestDto) {
       Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow(
               () -> new ShipmentNotFoundException("Shipment with id: '" + shipmentId + "' not found")
       );

       shipment.setStatus(ShipmentStatus.RETURN_INITIATED);

       shipmentRepository.save(shipment);

       // TODO: zapis na baze eventoutbox

       return shipmentMapper.mapShipmentToInitiateReturnResponseDto(shipment);

   }

    private Long nextLabelSeq() {
        Object single = entityManager
                .createNativeQuery("SELECT nextval('shipment.label_number_seq')")
                .getSingleResult();
        return ((Number) single).longValue();
    }

    private String format(Long n) {
        int year = java.time.Year.now().getValue();
        return "LBL-%d-%06d".formatted(year, n);
    }

}
