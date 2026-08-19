package com.baicai.demo.repository;

import com.baicai.demo.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByIccid(String iccid);

    List<Favorite> findAllByOrderByCreatedAtDesc();

    long deleteByIccid(String iccid);
}
