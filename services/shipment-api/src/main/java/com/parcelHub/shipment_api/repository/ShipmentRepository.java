package com.parcelHub.shipment_api.repository;

import com.parcelHub.shipment_api.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByClientRequestId(UUID clientRequestId);
}
