package org.jeecg.modules.image.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/images")
@Slf4j
public class ImagesController {
    private final Path imageStoragePath = Paths.get("/Users/heqiongqiong/Downloads"); // 修改为你的图片存储路径

//    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
//    public ResponseEntity<Resource> getImage() {
//        try {
//            Path imagePath = imageStoragePath.resolve("image.jpg"); // 修改为你的图片文件名
//            Resource resource = new UrlResource(imagePath.toUri());
//            if (resource.exists() || resource.isReadable()) {
//                return ResponseEntity.ok().body(resource);
//            } else {
//                throw new RuntimeException("Could not find or read the image file");
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("Error: " + e.getMessage());
//        }
//    }

    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path file = imageStoragePath.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/getImage")
    public ResponseEntity<Resource> getImage1(@RequestParam String imagePath) {
        try {
            Path path = Paths.get(imagePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
