package com.atm.iso8583.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Iso8583Config {

    @Value("${iso8583.host}")
    private String host;

    @Value("${iso8583.port}")
    private int port;

    @Value("${iso8583.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${iso8583.read-timeout:30000}")
    private int readTimeout;

    @Value("${iso8583.header-length:4}")
    private int headerLength;

    @Value("${iso8583.institution-id:000001}")
    private String institutionId;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public String getInstitutionId() {
        return institutionId;
    }
}
