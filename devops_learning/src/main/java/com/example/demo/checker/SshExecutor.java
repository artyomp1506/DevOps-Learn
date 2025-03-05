package com.example.demo.checker;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;

public class SshExecutor {
    private String ip;
    private String username;
    private int port;
    private String keyPath;
    private String password;

    public SshExecutor(String ip, String username, String password, int port, String keyPath) {
        this.ip = ip;
        this.username = username;
        this.port = port;
        this.keyPath = keyPath;
        this.password = password;

    }


    public  String getResult(String command) throws Exception {

        Session session = null;
        ChannelExec channel = null;

        try {
            var jsch = new JSch();


            session = jsch.getSession(username, ip, port);
            if (this.password!=null)
                session.setPassword(this.password);
           else jsch.addIdentity(keyPath);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();
            session.setTimeout(500);

            while (channel.isConnected()) {
                Thread.currentThread().sleep(500);
            }

            return new String(responseStream.toByteArray());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}
