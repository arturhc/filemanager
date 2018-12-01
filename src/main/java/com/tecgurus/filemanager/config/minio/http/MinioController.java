package com.tecgurus.filemanager.config.minio.http;

import com.tecgurus.filemanager.config.minio.dto.MinioObject;
import com.tecgurus.filemanager.config.minio.service.MinioTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api-filemanager/${minio.endpoint.name:minio}")
@ConditionalOnProperty(name = "minio.endpoint.enable", havingValue = "true")
public class MinioController {

    @Autowired
    private MinioTemplate minioTemplate;

    @GetMapping("/bucketExists/{bucketName}")
    public boolean bucketExists(@PathVariable String bucketName) {
        try {
            return minioTemplate.bucketExists(bucketName);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/bucket/{bucketName}")
    public ResponseEntity createBucket(@PathVariable String bucketName) throws Exception {
        minioTemplate.createBucket(bucketName);

        return ResponseEntity.ok(minioTemplate.getBucket(bucketName).get());
        //orElseThrow(() -> new IllegalArgumentException("The bucket with the given name does not exists"))
    }

    @PostMapping("/object/{bucketName}")
    public ResponseEntity createObject(@PathVariable String bucketName,
                                       @RequestParam("object") MultipartFile object) {

        String name = object.getOriginalFilename();

        try {

            minioTemplate.saveObject(bucketName, name, object.getInputStream(), object.getSize(), object.getContentType());

            return ResponseEntity.ok(
                    new MinioObject(minioTemplate.getObjectInfo(bucketName, name), minioTemplate.getEndpoint())
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("The file could not be created, " + e.getMessage());
        }

    }

    @GetMapping("/object/{bucketName}/{objectName}")
    public ResponseEntity getObject(@PathVariable String bucketName,
                                    @PathVariable String objectName) {
        try {

            String fileName = String.format("attachment; filename=\"%s\"", objectName);

            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_DISPOSITION, fileName)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(minioTemplate.getObject(bucketName, objectName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("An error occurred trying to retrieve the object");
        }

    }

}
