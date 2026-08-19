package com.baicai.demo.repository;

import com.baicai.demo.entity.QueryLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {

    long countByQueryTimeGreaterThanEqualAndQueryTimeLessThan(LocalDateTime start, LocalDateTime end);

    @Query("select q.iccid, count(q.id) "
            + "from QueryLog q "
            + "where q.queryTime >= :start and q.queryTime < :end "
            + "group by q.iccid "
            + "order by count(q.id) desc, q.iccid asc")
    List<Object[]> findTopByQueryTimeBetween(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             Pageable pageable);
}
