package com.tecgurus.filemanager.config.minio.service;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.messages.Bucket;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Optional;

public class MinioTemplate {

    private String endpoint, accessKey, secretKey;
    private MinioClient minioClient;

    public MinioTemplate(String endpoint, String accessKey, String secretKey) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public MinioClient getMinioClient() throws Exception {

        if (minioClient == null)
            minioClient = new MinioClient(endpoint, accessKey, secretKey);

        return minioClient;
    }

    public boolean bucketExists(String bucketName) throws Exception {
        return getMinioClient().bucketExists(bucketName);
    }

    public void createBucket(String bucketName) throws Exception {

        if (!bucketExists(bucketName)) {
            getMinioClient().makeBucket(bucketName);
        }

    }

    public Optional<Bucket> getBucket(String bucketName) throws Exception {
        return getMinioClient().listBuckets().stream()
                .filter(bucket -> bucket.name().equals(bucketName))
                .findFirst();
    }

    public void saveObject(String bucketName, String objectName,
                           InputStream stream, long size, String contentType) throws Exception {
        getMinioClient().putObject(bucketName, objectName, stream, size, contentType);
    }

    public ObjectStat getObjectInfo(String bucketName, String objectName) throws Exception {
        return getMinioClient().statObject(bucketName, objectName);
    }

    public byte[] getObject(String bucketName, String objectName) throws Exception {
        return IOUtils.toByteArray(getMinioClient().getObject(bucketName, objectName));
    }

}
