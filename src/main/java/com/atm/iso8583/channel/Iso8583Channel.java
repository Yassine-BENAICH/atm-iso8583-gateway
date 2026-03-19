package com.atm.iso8583.channel;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@Slf4j
public class Iso8583Channel {

    @Value("${iso8583.switch.host}")
    private String host;

    @Value("${iso8583.switch.port}")
    private int port;

    @Value("${iso8583.switch.timeout}")
    private int timeout;

}
