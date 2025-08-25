package com.fanda.banner.controller;

import com.fanda.banner.dto.BannerItemDto;
import com.fanda.banner.service.BannerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BannerQueryController {

    private final BannerQueryService bannerQueryService;

    @GetMapping("/images/urls")
    public ResponseEntity<List<BannerItemDto>> listAllUrls(){
        return ResponseEntity.ok(bannerQueryService.listAllBannerUrls());
    }
}
