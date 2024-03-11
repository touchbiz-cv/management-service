package org.jeecg.modules.alarmrecord.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Description: t_alarm_record
 * @Author: jeecg-boot
 * @Date: 2024-03-09
 * @Version: V1.0
 */
@Api(tags = "图片路径访问")
@RestController
@RequestMapping("/api/images")
@Slf4j
public class ImagesController {
    private final Path imageStoragePath = Paths.get(""); // 修改为你的图片存储路径

    @ApiOperation(value = "图片获取", notes = "图片获取")
    @GetMapping(value = "/getImage", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getImage(@RequestParam String filename) {
        try {
            Path imagePath = imageStoragePath.resolve(filename);
            Resource resource = new UrlResource(imagePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            } else {
                throw new RuntimeException("Could not find or read the image file");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

}
