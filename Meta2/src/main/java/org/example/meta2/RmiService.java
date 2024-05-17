package org.example.meta2;

import googol.gateway.IRmiService;
import org.springframework.stereotype.Service;
import java.rmi.RemoteException;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@EnableScheduling
@Service
public class RmiService implements IRmiService {
    private SimpMessagingTemplate template;

    @Autowired
    public RmiService(SimpMessagingTemplate template) throws RemoteException {
        super();
        this.template = template;

    }

    @Override
    public String updateAdmin(String info) throws RemoteException {
        System.out.println(info);

        try{
            template.convertAndSend("/topic/status", info);
        }
        catch(Exception e){
            System.out.println("Exception occurred " + e);
        }
        return info;
    }
}
