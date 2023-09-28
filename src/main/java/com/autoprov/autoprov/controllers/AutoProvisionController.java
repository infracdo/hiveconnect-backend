package com.autoprov.autoprov.controllers;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jcraft.jsch.*;

@CrossOrigin(origins = "*")
@RestController
public class AutoProvisionController {
    // Insert playbook invokes here]

    PrintWriter toChannel;

    @Async("asyncExecutor")
    @PostMapping("/runPlaybook")
    public CompletableFuture<String> runPlaybook() throws JSchException, IOException, InterruptedException {

        try {
            JSch jsch = new JSch();

            jsch.setKnownHosts("~/.ssh/known_hosts");

            Session session = jsch.getSession("ubuntu", "192.168.250.35", 22);
            session.setPassword("ap0ll0ap0ll0");
            session.connect(0);

            Channel channel = session.openChannel("shell");

            InputStream inStream = channel.getInputStream();

            OutputStream outStream = channel.getOutputStream();
            toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);

            channel.connect();
            readerThread(new InputStreamReader(inStream));

            Thread.sleep(1000);
            String command = "ansible-playbook -e \"device_name=mario\" -e \"serial_number=111AAA\" -e \"mac_address=1c:18:4a:f9:16:b1\" -e \"olt_ip=172.16.0.3\" -e \"account_number=00111\" -e \"status=Activated\" -e \"onu_private_ip=172.16.0.53\" -e \"olt_interface=0/1:3\" /home/ubuntu/ansible/playbooks/onboard_newly_activated_subscribers.yml -vvv";
            sendCommand(session, command);

            // InputStream in = channel.getInputStream();
            // channel.connect();

            // byte[] tmp = new byte[1024];
            // while (true) {
            // while (in.available() > 0) {
            // int i = in.read(tmp, 0, 1024);
            // if (i < 0)
            // break;
            // }
            // if (channel.isClosed()) {
            // break;
            // }
            // try {
            // Thread.sleep(1000);
            // } catch (Exception ee) {
            // }
            // }

            // channel.setOutputStream(System.out);
            // ((ChannelExec) channel).setErrStream(System.err);

            return CompletableFuture.completedFuture("Successful");

        } catch (IOError e) {
            return CompletableFuture.completedFuture("Error Occured");

        } finally {

        }
    }

    public void sendCommand(Session myLocalSession, final String command) {
        if (myLocalSession != null && myLocalSession.isConnected()) {
            try {
                toChannel.println(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void readerThread(final InputStreamReader tout) {
        Thread read2 = new Thread() {
            @Override
            public void run() {
                StringBuilder line = new StringBuilder();
                char toAppend = ' ';
                try {
                    while (true) {
                        try {
                            while (tout.ready()) {
                                toAppend = (char) tout.read();
                                if (toAppend == '\n') {
                                    System.out.print(line.toString());
                                    line.setLength(0);
                                } else
                                    line.append(toAppend);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("\n\n\n************errorrrrrrr reading character**********\n\n\n");
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception ex) {
                    System.out.println(ex);
                    try {
                        tout.close();
                    } catch (Exception e) {
                    }
                }
            }
        };
        read2.start();
    }
}
