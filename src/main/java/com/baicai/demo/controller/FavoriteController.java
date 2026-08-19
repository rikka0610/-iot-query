package com.baicai.demo.controller;

import com.baicai.demo.common.Result;
import com.baicai.demo.entity.Favorite;
import com.baicai.demo.service.FavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iot")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/favorite")
    public Result<Favorite> favorite(@RequestBody(required = false) Map<String, String> request) {
        if (request == null) {
            return Result.error(400, "请求体不能为空");
        }
        String iccid = request.get("iccid");
        if (iccid == null || iccid.isBlank()) {
            return Result.error(400, "iccid 不能为空");
        }
        String remark = request.get("remark");
        return Result.success(favoriteService.favorite(iccid.trim(), remark));
    }

    @DeleteMapping("/favorite/{iccid}")
    public Result<Void> unfavorite(@PathVariable String iccid) {
        if (iccid == null || iccid.isBlank()) {
            return Result.error(400, "iccid 不能为空");
        }
        favoriteService.unfavorite(iccid.trim());
        return Result.success();
    }

    @GetMapping("/favorites")
    public Result<List<Map<String, Object>>> listFavorites() {
        return Result.success(favoriteService.listFavorites());
    }
}
