package com.mulberry.WebChat.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Component
public class FileLoadUtil {
    @Value("${custom.oss.bucket-name}")
    private String bucketName;
    @Value("${custom.oss.url-expire}")
    private Long urlExpire;
    @Value("${custom.oss.file-expire}")
    private Long fileExpire;

    private final OSS ossClient;

    public FileLoadUtil(OSS ossClient) {
        this.ossClient = ossClient;
    }

    private static final byte[][] IMAGE_MAGIC_BYTES = {
            // JPEG, PNG, GIF, BMP, WebP
            new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
            new byte[]{0x47, 0x49, 0x46, 0x38},
            new byte[]{0x42, 0x4D},
            new byte[]{0x52, 0x49, 0x46, 0x46, -1, -1, -1, -1, 0x57, 0x45, 0x42, 0x50}
    };

    private static boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != -1 && data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = inputStream.read(header);
            if (read < 2) return false;

            for (byte[] magic : IMAGE_MAGIC_BYTES) {
                if (startsWith(header, magic)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static String getRandomName(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Illegal filename");
        }

        String extension = "";
        int lastDot = filename.lastIndexOf(".");
        if (lastDot > 0) {
            extension = filename.substring(lastDot).toLowerCase();
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss_"));
        String randomId = UUID.randomUUID().toString().substring(0, 6);

        return  timestamp + randomId + extension;
    }

    public String ossSave(MultipartFile file, String folder) throws IOException {
        String savedName = folder + getRandomName(file);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setCacheControl("public, max-age=" + fileExpire);
        //metadata.setContentType("");
        PutObjectRequest objectRequest = new PutObjectRequest(
                this.bucketName,
                savedName,
                file.getInputStream(),
                metadata
        );
        ossClient.putObject(objectRequest);

        return savedName;
    }

    public String generateSignedUrl(String objectKey) {
        Date expirationTime = new Date(System.currentTimeMillis() + this.urlExpire * 1000);
        URL url = ossClient.generatePresignedUrl(bucketName, objectKey, expirationTime);
        return url.toString();
    }

    public boolean isFileExists(String objectKey) {
        return ossClient.doesObjectExist(bucketName, objectKey);
    }

    public void deleteFile(String objectKey) {
        ossClient.deleteObject(bucketName, objectKey);
    }
}
