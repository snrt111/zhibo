package com.zhibo.backend.controller;

import com.zhibo.backend.common.Result;
import com.zhibo.backend.entity.Gift;
import com.zhibo.backend.entity.GiftRecord;
import com.zhibo.backend.service.GiftService;
import com.zhibo.backend.service.MetricsService;
import com.zhibo.backend.utils.JwtUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gift")
public class GiftController {

    @Autowired
    private GiftService giftService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MetricsService metricsService;

    @GetMapping("/list")
    public Result<List<Gift>> getGiftList() {
        List<Gift> giftList = giftService.getGiftList();
        return Result.success("获取礼物列表成功", giftList);
    }

    @GetMapping("/hot")
    public Result<List<Gift>> getHotGiftList() {
        List<Gift> giftList = giftService.getHotGiftList();
        return Result.success("获取热门礼物列表成功", giftList);
    }

    @GetMapping("/admin/list")
    public Result<Page<Gift>> getAdminGiftList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Gift> result = giftService.getAdminGiftList(keyword, minPrice, maxPrice, page, size);
        return Result.success("获取礼物列表成功", result);
    }

    @PostMapping("/send")
    public Result<GiftRecord> sendGift(@RequestHeader("Authorization") String authorization, @RequestBody GiftRecord giftRecord) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        giftRecord.setFromUserId(userId);
        GiftRecord sentGiftRecord = giftService.sendGift(giftRecord);
        // 增加礼物数量
        metricsService.incrementGiftCount();
        return Result.success("赠送礼物成功", sentGiftRecord);
    }

    @GetMapping("/record/{liveId}")
    public Result<List<GiftRecord>> getGiftRecordList(@PathVariable Long liveId, @RequestParam(defaultValue = "50") int limit) {
        List<GiftRecord> giftRecordList = giftService.getGiftRecordList(liveId, limit);
        return Result.success("获取礼物记录成功", giftRecordList);
    }

    @GetMapping("/my-record")
    public Result<List<GiftRecord>> getMyGiftRecord(@RequestHeader("Authorization") String authorization, @RequestParam(defaultValue = "50") int limit) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<GiftRecord> giftRecordList = giftService.getGiftRecordByUserId(userId, limit);
        return Result.success("获取礼物记录成功", giftRecordList);
    }

    @GetMapping("/stats/live/{liveId}")
    public Result<Map<String, Object>> getLiveGiftStats(@PathVariable Long liveId) {
        Map<String, Object> stats = giftService.getLiveGiftStats(liveId);
        return Result.success("获取礼物统计成功", stats);
    }

    @GetMapping("/stats/anchor")
    public Result<Map<String, Object>> getAnchorGiftStats(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        Map<String, Object> stats = giftService.getAnchorGiftStats(userId);
        return Result.success("获取礼物统计成功", stats);
    }

    @PostMapping("/create")
    public Result<Gift> createGift(
            @RequestParam("name") String name,
            @RequestParam("price") Double price,
            @RequestParam(value = "icon", required = false) MultipartFile iconFile,
            @RequestParam(value = "iconUrl", required = false) String iconUrl,
            @RequestParam(value = "isHot", required = false, defaultValue = "false") Boolean isHot) {
        Gift gift = new Gift();
        gift.setName(name);
        gift.setPrice(price);
        gift.setIsHot(isHot);
        Gift createdGift = giftService.createGift(gift, iconFile, iconUrl);
        return Result.success("添加礼物成功", createdGift);
    }

    @PostMapping("/update/{id}")
    public Result<Gift> updateGift(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "icon", required = false) MultipartFile iconFile,
            @RequestParam(value = "iconUrl", required = false) String iconUrl,
            @RequestParam(value = "isHot", required = false) Boolean isHot) {
        Gift gift = new Gift();
        gift.setId(id);
        gift.setName(name);
        gift.setPrice(price);
        gift.setIsHot(isHot);
        Gift updatedGift = giftService.updateGift(gift, iconFile, iconUrl);
        return Result.success("更新礼物成功", updatedGift);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteGift(@PathVariable Long id) {
        giftService.deleteGift(id);
        return Result.success("删除礼物成功", null);
    }
}
