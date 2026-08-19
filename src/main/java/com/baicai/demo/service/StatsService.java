package com.baicai.demo.service;

import com.baicai.demo.repository.QueryLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final QueryLogRepository queryLogRepository;

    public StatsService(QueryLogRepository queryLogRepository) {
        this.queryLogRepository = queryLogRepository;
    }

    public Map<String, Object> getStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalQueries", queryLogRepository.count());
        stats.put("todayQueries",
                queryLogRepository.countByQueryTimeGreaterThanEqualAndQueryTimeLessThan(start, end));

        List<Map<String, Object>> top5 = new ArrayList<>();
        for (Object[] row : queryLogRepository.findTopByQueryTimeBetween(start, end, PageRequest.of(0, 5))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("iccid", row[0]);
            item.put("count", ((Number) row[1]).longValue());
            top5.add(item);
        }
        stats.put("top5", top5);
        return stats;
    }
}
