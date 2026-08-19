package com.baicai.demo.service;

import com.baicai.demo.entity.Favorite;
import com.baicai.demo.entity.SimCardInfo;
import com.baicai.demo.repository.FavoriteRepository;
import com.baicai.demo.repository.SimCardInfoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final SimCardInfoRepository simCardInfoRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           SimCardInfoRepository simCardInfoRepository) {
        this.favoriteRepository = favoriteRepository;
        this.simCardInfoRepository = simCardInfoRepository;
    }

    public Favorite favorite(String iccid, String remark) {
        Favorite favorite = favoriteRepository.findByIccid(iccid).orElseGet(Favorite::new);
        favorite.setIccid(iccid);
        favorite.setRemark(remark);
        if (favorite.getCreatedAt() == null) {
            favorite.setCreatedAt(LocalDateTime.now());
        }
        return favoriteRepository.save(favorite);
    }

    public void unfavorite(String iccid) {
        favoriteRepository.deleteByIccid(iccid);
    }

    public List<Map<String, Object>> listFavorites() {
        return favoriteRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toFavoriteMap)
                .toList();
    }

    private Map<String, Object> toFavoriteMap(Favorite favorite) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", favorite.getId());
        result.put("iccid", favorite.getIccid());
        result.put("remark", favorite.getRemark());
        result.put("createdAt", favorite.getCreatedAt());

        SimCardInfo card = simCardInfoRepository.findByIccid(favorite.getIccid()).orElse(null);
        Map<String, Object> cardData = card == null ? emptyCardData(favorite.getIccid()) : card.toMap();
        cardData.remove("iccid");
        result.putAll(cardData);
        return result;
    }

    private Map<String, Object> emptyCardData(String iccid) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("iccid", iccid);
        data.put("msisdn", null);
        data.put("carrierType", null);
        data.put("lifeCycle", null);
        data.put("serviceEndTime", null);
        data.put("packageName", null);
        data.put("packageCapacityKb", null);
        data.put("usedKb", null);
        data.put("remainingKb", null);
        data.put("usageRate", null);
        data.put("cycleEndTime", null);
        return data;
    }
}
