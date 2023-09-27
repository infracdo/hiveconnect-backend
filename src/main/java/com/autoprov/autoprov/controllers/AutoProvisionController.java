package com.autoprov.autoprov.controllers;

import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jcraft.jsch.*;

@RestController
public class AutoProvisionController {
    // Insert playbook invokes here]

    @Async("asyncExecutor")
    @PostMapping("/runPlaybook")
    public CompletableFuture<String> runPlaybook() throws JSchException {

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession("ubuntu", "192.168.250.35", 22);
            session.setPassword("ap0ll0ap0ll0");
            session.connect(0);

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(
                    "ansible-playbook -e \"device_name=luigi\" -e \"serial_number=111AAA\" -e \"mac_address=1c:18:4a:f9:16:b1\" -e \"olt_ip=172.16.0.2\" -e \"account_number=00111\" -e \"status=Activated\" -e \"onu_private_ip=172.16.0.52\" -e \"olt_interface=0/1:3\" /home/ubuntu/ansible/playbooks/onboard_newly_activated_subscribers.yml -vvv");

            channel.setInputStream(null, false);
            channel.setOutputStream(System.out);
            ((ChannelExec) channel).setErrStream(System.err);

            return CompletableFuture.completedFuture("Successful");

        } catch (IOError e) {
            return CompletableFuture.completedFuture("Error Occured");

        } finally {

        }
    }
}
