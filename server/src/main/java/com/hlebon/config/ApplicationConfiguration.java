package com.hlebon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.DatagramSocket;
import java.net.SocketException;

@Configuration
@ComponentScan("com.hlebon")
public class ApplicationConfiguration {

    private static final int SERVER_PORT = 9876;

    @Bean
    public DatagramSocket serverSocket() throws SocketException {
        return new DatagramSocket(SERVER_PORT);
    }
}
