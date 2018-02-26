package com.sequenceiq.cloudbreak.service.stack.flow;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

public class ProxySocketFactory extends SocketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxySocketFactory.class);

    private final String proxyHost;
    private final String proxyPort;
    private final String username;
    private final String password;

    public ProxySocketFactory(String proxyHost, String proxyPort, String username, String password) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.username = username;
        this.password = password;
    }

    @Override
    public Socket createSocket() {
        LOGGER.info("Create proxy socket thourgh HTTP proxy: {}:{}", proxyHost, proxyPort);
        int port = Integer.valueOf(proxyPort);
        try {
            SocketAddress proxyAddr = new InetSocketAddress(proxyHost, port);
            Proxy proxy = new Proxy(Type.HTTP, proxyAddr);
            Socket socket = new Socket(proxy);
            if (username != null && password != null) {
                LOGGER.info("Use basic auth for HTTP proxy, username: {}", username);
                // RFC 2616
                String proxyUserPass = String.format("%s:%s", username, password);
                String proxyConnect = String.format("CONNECT %s:%d HTTP/1.0\nProxy-Authorization:Basic %s\n\n", proxyHost, port,
                    Arrays.toString(Base64Utils.encode(proxyUserPass.getBytes())));
                socket.getOutputStream().write(proxyConnect.getBytes());
                byte[] tmpBuffer = new byte[512];
                InputStream socketInput = socket.getInputStream();
                int len = socketInput.read(tmpBuffer, 0, tmpBuffer.length);
                if (len == 0) {
                    throw new SocketException("Invalid response from proxy");
                }
                String proxyResponse = new String(tmpBuffer, 0, len, "UTF-8");
                if (proxyResponse.contains("200")) {
                    LOGGER.info("Successfully connected to HTTP proxy with basic auth, host: {}, port:{}, username:{}", proxyHost, port, username);
                    // Flush any outstanding message in buffer
                    if (socketInput.available() > 0)
                        socketInput.skip(socketInput.available());
                    return socket;
                } else {
                    LOGGER.error("Failed to authenticate to HTTP proxy");
                }
            }
            return socket;
        } catch (Exception e) {
            LOGGER.error("Failed to connect to HTTP proxy", e);
        }
        return new Socket();
    }

    @Override
    public Socket createSocket(String var1, int var2) throws IOException, UnknownHostException {
        return new Socket(var1, var2);
    }

    @Override
    public Socket createSocket(String var1, int var2, InetAddress var3, int var4) throws IOException, UnknownHostException {
        return new Socket(var1, var2, var3, var4);
    }

    @Override
    public Socket createSocket(InetAddress var1, int var2, InetAddress var3, int var4) throws IOException {
        return new Socket(var1, var2, var3, var4);
    }

    @Override
    public Socket createSocket(InetAddress var1, int var2) throws IOException {
        return new Socket(var1, var2);
    }

}
