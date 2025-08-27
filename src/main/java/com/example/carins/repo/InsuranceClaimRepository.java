package com.example.carins.repo;

import com.example.carins.model.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    @Query("select c from InsuranceClaim c " +
            "where c.car.id = :carId " +
            "order by c.claimDate asc")
    List<InsuranceClaim> findAllByCarIdOrderByDate(@Param("carId") Long carId);
}
