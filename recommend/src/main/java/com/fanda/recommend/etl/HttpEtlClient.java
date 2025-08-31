package com.fanda.recommend.etl;

import com.fanda.recommend.property.EtlProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpEtlClient implements EtlClient{

    private final HttpClient httpClient;
    private final EtlProperties etlProperties;

    public HttpEtlClient(EtlProperties etlProperties) {
        this.etlProperties = etlProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(etlProperties.getConnectTimeoutMillis()))
                .build();
    }

    @Override
    public EtlZipStream runZipStream(){
        try{
            String url = etlProperties.getBaseUrl() + "/api/etl/run";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(etlProperties.getReadTimeoutMillis()))
                    .header("Accept", "application/zip")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if(status!= 200){
                throw new IllegalStateException("ETL run failed: HTTP "+status);
            }

            InputStream raw = response.body();
            InputStream zipStream = ensureZipSignature(raw);

            String runId = response.headers().firstValue("X-Run-Id").orElse(null);
            long contentLength = -1L;
            if(response.headers().firstValue("Content-Length").isPresent()){
                try{
                    contentLength = Long.parseLong(response.headers().firstValue("Content-Length").get());
                } catch (NumberFormatException ignore){}
            }
            return new EtlZipStream(zipStream, runId, contentLength);
        }
        catch (Exception e){
            throw new RuntimeException("ETL request failed: "+e.getMessage(), e);
        }
    }

    private InputStream ensureZipSignature(InputStream in) throws IOException{
        PushbackInputStream pbis = new PushbackInputStream(in, 4);
        byte[] magic = new byte[4];
        int n = pbis.read(magic);
        if(n<4){
            throw new IOException("Not enough bytes to verify ZIP signature");
        }
        pbis.unread(magic, 0, n);
        boolean ok = (magic[0] == 0x50 && magic[1] == 0x4B && magic[2] == 0x03 && magic[3] == 0x04);
        if(!ok){
            throw new IOException("Response is not a ZIP stream (signature mismatch)");
        }
        return pbis;
    }
}
