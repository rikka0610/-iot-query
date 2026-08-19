package com.baicai.demo.repository;

import com.baicai.demo.entity.SimCardInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SimCardInfoRepository extends JpaRepository<SimCardInfo, Long> {

    Optional<SimCardInfo> findByIccid(String iccid);
}
