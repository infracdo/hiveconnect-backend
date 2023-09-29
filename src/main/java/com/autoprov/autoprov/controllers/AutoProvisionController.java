package com.autoprov.autoprov.controllers;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jcraft.jsch.*;

@CrossOrigin(origins = "*")
@RestController
public class AutoProvisionController {
    // Insert playbook invokes here]

    PrintWriter toChannel;

    @Async("asyncExecutor")
    @PostMapping("/runPlaybook")
    public String runPlaybook(@RequestBody Map<String, String> params)
            throws JSchException, IOException, InterruptedException {

        try {
            // SSH connection parameters
            String host = "172.91.10.108";
            String user = "ubuntu";
            String password = "ap0ll0";

            // Command to execute
            StringBuilder command = new StringBuilder();
            command.append("ansible-playbook ");
            command.append("-e \"device_name=" + params.get("device_name") + "\" ");
            command.append("-e \"serial_number=" + params.get("serial_number") + "\" ");
            command.append("-e \"mac_address=" + params.get("mac_address") + "\" ");
            command.append("-e \"olt_ip=" + params.get("olt_ip") + "\" ");
            command.append("-e \"account_number=" + params.get("account_no") + "\" ");
            command.append("-e \"status=" + params.get("status") + "\" ");
            command.append("-e \"onu_private_ip=" + params.get("onu_private_ip") + "\" ");
            command.append("-e \"olt_interface=" + params.get("olt_interface") + "\" ");
            command.append("/home/ubuntu/ansible/playbooks/onboard_newly_activated_subscribers.yml -vvv");

            // Establish SSH session
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);
            session.connect();

            // Execute command
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command.toString());

            // Get command output
            InputStream inputStream = channelExec.getInputStream();
            channelExec.connect();

            // Read the output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Disconnect SSH session
            channelExec.disconnect();
            session.disconnect();

            // Return the output or handle it as needed
            return output.toString();
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return "Error executing command";
        }

        // try {
        // JSch jsch = new JSch();

        // jsch.setKnownHosts("~/.ssh/known_hosts");

        // Session session = jsch.getSession("ubuntu", "172.91.10.112", 22);
        // session.setPassword("ap0ll0");
        // session.connect(0);

        // Channel channel = session.openChannel("shell");

        // InputStream inStream = channel.getInputStream();

        // OutputStream outStream = channel.getOutputStream();
        // toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);

        // channel.connect();
        // readerThread(new InputStreamReader(inStream));

        // Thread.sleep(1000);

        // // Playbook fields
        // // device_name = "mary_bw1";
        // // onu_serial_number = "111AAA";
        // // mac_address = "1c:18:4a:f9:16:b1";
        // // olt_ip = "172.16.0.2";
        // // account_no = "00111";
        // // status = "Activated";
        // // onu_private_ip = "172.16.0.52";
        // // olt_interface = "0/1:3";

        // StringBuilder command = new StringBuilder();
        // command.append("ansible-playbook ");
        // command.append("-e \"device_name=" + params.get("device_name") + "\" ");
        // command.append("-e \"serial_number=" + params.get("serial_number") + "\" ");
        // command.append("-e \"mac_address=" + params.get("mac_address") + "\" ");
        // command.append("-e \"olt_ip=" + params.get("olt_ip") + "\" ");
        // command.append("-e \"account_number=" + params.get("account_no") + "\" ");
        // command.append("-e \"status=" + params.get("status") + "\" ");
        // command.append("-e \"onu_private_ip=" + params.get("onu_private_ip") + "\"
        // ");
        // command.append("-e \"olt_interface=" + params.get("olt_interface") + "\" ");
        // command.append("/home/ubuntu/ansible/playbooks/onboard_newly_activated_subscribers.yml
        // -vvv");

        // sendCommand(session, command.toString());

        // return CompletableFuture.completedFuture("Successful");

        // } catch (IOError e) {
        // return CompletableFuture.completedFuture("Error Occured");

        // } finally {

        // }
        // }

        // @Async("asyncExecutor")
        // @PostMapping("/setBandwidth")
        // public CompletableFuture<String> setBandwidth(String host, String
        // olt_interface, String downstream, String upstream)
        // throws JSchException, IOException, InterruptedException {

        // try {
        // JSch jsch = new JSch();

        // jsch.setKnownHosts("~/.ssh/known_hosts");

        // Session session = jsch.getSession("ubuntu", "192.168.250.35", 22);
        // session.setPassword("ap0ll0ap0ll0");
        // session.connect(0);

        // Channel channel = session.openChannel("shell");

        // InputStream inStream = channel.getInputStream();

        // OutputStream outStream = channel.getOutputStream();
        // toChannel = new PrintWriter(new OutputStreamWriter(outStream), true);

        // channel.connect();
        // readerThread(new InputStreamReader(inStream));

        // Thread.sleep(1000);

        // // ansible-playbook /home/ubuntu/ansible/playbooks/set_bandwidth.yml -e
        // // "host=172.16.0.2" -e "interface=0/1:3" -e "downstream=11000" -e
        // // "upstream=11000" -vvv

        // // Playbook fields
        // host = "mario";
        // olt_interface = "111AAA";
        // downstream = "1c:18:4a:f9:16:b1";
        // upstream = "172.16.0.3";

        // StringBuilder command = new StringBuilder();
        // command.append("ansible-playbook
        // /home/ubuntu/ansible/playbooks/set_bandwidth.yml ");
        // command.append("-e \"host=" + host + "\" ");
        // command.append("-e \"olt_interface=" + olt_interface + "\" ");
        // command.append("-e \"downstream=" + downstream + "\" ");
        // command.append("-e \"upstream=" + upstream + "\" ");
        // command.append("-vvv");

        // sendCommand(session, command.toString());

        // return CompletableFuture.completedFuture("Successful");

        // } catch (IOError e) {
        // return CompletableFuture.completedFuture("Error Occured");

        // } finally {

        // }
        // }

        // public void sendCommand(Session myLocalSession, final String command) {
        // if (myLocalSession != null && myLocalSession.isConnected()) {
        // try {
        // toChannel.println(command);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // }

        // void readerThread(final InputStreamReader tout) {
        // Thread read2 = new Thread() {
        // @Override
        // public void run() {
        // StringBuilder line = new StringBuilder();
        // char toAppend = ' ';
        // try {
        // while (true) {
        // try {
        // while (tout.ready()) {
        // toAppend = (char) tout.read();
        // if (toAppend == '\n') {
        // System.out.print(line.toString());
        // line.setLength(0);
        // } else
        // line.append(toAppend);
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // System.out.println("\n\n\n************errorrrrrrr reading
        // character**********\n\n\n");
        // }
        // Thread.sleep(1000);
        // }
        // } catch (Exception ex) {
        // System.out.println(ex);
        // try {
        // tout.close();
        // } catch (Exception e) {
        // }
        // }
        // }
        // };
        // read2.start();
        // }
    }
}
