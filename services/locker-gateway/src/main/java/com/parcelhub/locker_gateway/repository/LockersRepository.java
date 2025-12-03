package com.parcelhub.locker_gateway.repository;

import com.parcelhub.locker_gateway.model.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LockersRepository extends JpaRepository<Locker, UUID> {
}
