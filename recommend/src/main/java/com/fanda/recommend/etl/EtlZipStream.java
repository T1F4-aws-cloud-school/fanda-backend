package com.fanda.recommend.etl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class EtlZipStream implements Closeable {
    private final InputStream zipStream;
    private final String runId;
    private final long contentLength;

    public EtlZipStream(InputStream zipStream, String runId, long contentLength){
        this.zipStream = zipStream;
        this.runId = runId;
        this.contentLength = contentLength;
    }

    public InputStream getZipStream(){
        return zipStream;
    }

    public String getRunId(){
        return runId;
    }

    public long getContentLength(){
        return contentLength;
    }

    @Override
    public void close() throws IOException{
        zipStream.close();
    }
}
