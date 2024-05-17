package org.example.meta2;

import googol.gateway.IRmiService;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiServiceExporter;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.*;
import java.util.Properties;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * Configuration class for the RMI service
 */
@EnableScheduling
@Configuration
public class RmiConfig {
    @Autowired
    private SimpMessagingTemplate template;

    /**
     * Method to configure the RMI service
     * @return The RMI service exporter
     * @throws Exception
     */
    @Bean
    public RmiServiceExporter rmiServiceExporter() throws Exception {
        try{
            RmiServiceExporter rmiServiceExporter = new RmiServiceExporter();

            //definição dos parâmetros do serviço
            rmiServiceExporter.setRegistryPort(1090);
            rmiServiceExporter.setServiceName("MyService");
            rmiServiceExporter.setServiceInterface(IRmiService.class);
            rmiServiceExporter.setService(new RmiService(template));
            return rmiServiceExporter;
        }
        catch(Exception e){
            System.out.println("exception occurred while trying to configure rmi");
            return null;
        }
    }
}
