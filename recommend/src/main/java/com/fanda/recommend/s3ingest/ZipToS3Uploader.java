package com.fanda.recommend.s3ingest;

import com.fanda.recommend.property.S3IngestionProperties;
import com.fanda.recommend.s3ingest.dto.UploadResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipToS3Uploader {

    private static final Set<String> REQUIRED = Set.of("users.csv", "items.csv", "interactions.csv");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final S3Client s3Client;
    private final S3IngestionProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZipToS3Uploader(S3Client s3Client, S3IngestionProperties properties){
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public UploadResultDto uploadZipToS3(InputStream zipStream, String runIdMaybeNull){
        String runId = runIdMaybeNull != null && !runIdMaybeNull.isBlank() ? runIdMaybeNull : generateRunId();

        String datePrefix = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DATE_FMT);
        String s3Prefix = trim(properties.getBasePrefix()) + "/" + datePrefix + "/" + runId + "/";

        Map<String, File> tempFiles = new HashMap<>();
        Set<String> seen = new HashSet<>();

        try(ZipInputStream zis = new ZipInputStream(zipStream)){
            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null){
                String name = entry.getName();
                if(!REQUIRED.contains(name)){
                    throw new IllegalStateException("Unexpected ZIP entry: "+name);
                }
                if(seen.contains(name)){
                    throw new IllegalStateException("Duplicated ZIP entry: "+name);
                }
                seen.add(name);

                File temp = Files.createTempFile("etl-", "-"+name).toFile();
                Files.copy(zis, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFiles.put(name, temp);
            }
        }
        catch (Exception e){
            throw new RuntimeException("ZIP unpack failed: "+e.getMessage(), e);
        }
        if(!tempFiles.keySet().containsAll(REQUIRED)){
            throw new IllegalStateException("Missing required entries. Found : "+ tempFiles.keySet());
        }

        // 최소 헤더 검증
        validateInteractionsHeader(tempFiles.get("interactions.csv"));
        String bucket = properties.getBucket();
        String usersKey = s3Prefix + "users.csv";
        String itemsKey = s3Prefix + "items.csv";
        String interactionKey = s3Prefix + "interactions.csv";

        putCsv(bucket, usersKey, tempFiles.get("users.csv"));
        putCsv(bucket, itemsKey, tempFiles.get("items.csv"));
        putCsv(bucket, interactionKey, tempFiles.get("interactions.csv"));

        headOk(bucket, usersKey);
        headOk(bucket, itemsKey);
        headOk(bucket, interactionKey);

        String manifestKey = s3Prefix + "manifest.json";
        try{
            Map<String, Object> manifest = new HashMap<>();
            manifest.put("runId", runId);
            manifest.put("prefix", s3Prefix);
            manifest.put("createdAt", ZonedDateTime.now().toString());
            Map<String, Long> sizes = new HashMap<>();
            sizes.put("users.csv", tempFiles.get("users.csv").length());
            sizes.put("items.csv", tempFiles.get("items.csv").length());
            sizes.put("interactions.csv", tempFiles.get("interactions.csv").length());
            manifest.put("sizes", sizes);

            byte[] json = objectMapper.writeValueAsBytes(manifest);
            PutObjectRequest mReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(manifestKey)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .build();
            s3Client.putObject(mReq, RequestBody.fromBytes(json));
        } catch (Exception ignore) {
        } finally {
            for (File f : tempFiles.values()) {
                try { Files.deleteIfExists(f.toPath()); } catch (Exception ignore) { }
            }
        }

        return new UploadResultDto(
                runId,
                s3Prefix,
                "s3://" + bucket + "/" + usersKey,
                "s3://" + bucket + "/" + itemsKey,
                "s3://" + bucket + "/" + interactionKey,
                "s3://" + bucket + "/" + manifestKey
        );
    }

    private void validateInteractionsHeader(File file) {
        String header = readFirstLine(file);
        if (header == null) throw new IllegalStateException("interactions.csv header missing");
        String upper = header.trim().toUpperCase();
        if (!(upper.contains("USER_ID") && upper.contains("ITEM_ID")
                && upper.contains("EVENT_TYPE") && upper.contains("TIMESTAMP"))) {
            throw new IllegalStateException("interactions.csv required columns missing: " + header);
        }
    }

    private String readFirstLine(File file) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            return br.readLine();
        } catch (Exception e) {
            throw new RuntimeException("Read header failed: " + file.getName(), e);
        }
    }

    private void putCsv(String bucket, String key, File file) {
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("text/csv; charset=utf-8")
                    .build();
            s3Client.putObject(req, RequestBody.fromFile(file));
        } catch (Exception e) {
            throw new RuntimeException("S3 putObject failed: s3://" + bucket + "/" + key, e);
        }
    }

    private void headOk(String bucket, String key) {
        try {
            HeadObjectRequest head = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.headObject(head);
        } catch (Exception e) {
            throw new RuntimeException("S3 headObject failed: s3://" + bucket + "/" + key, e);
        }
    }

    private String generateRunId() {
        String ts = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return ts + "-" + rand;
    }

    private String trim(String s) {
        if (s == null) return "";
        String t = s;
        if (t.startsWith("/")) t = t.substring(1);
        if (t.endsWith("/")) t = t.substring(0, t.length() - 1);
        return t;
    }
}
