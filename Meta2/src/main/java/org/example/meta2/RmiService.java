package org.example.meta2;

import googol.gateway.IRmiService;
import org.springframework.stereotype.Service;
import java.rmi.RemoteException;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;


/**
 * Class to handle the RMI service
 */
@EnableScheduling
@Service
public class RmiService implements IRmiService {
    private SimpMessagingTemplate template;

    /**
     * Constructor for the RmiService class
     * @param template The template to use
     * @throws RemoteException
     */
    @Autowired
    public RmiService(SimpMessagingTemplate template) throws RemoteException {
        super();
        this.template = template;

    }

    /**
     * Method to update the admin
     * @param info The information to update
     * @return The updated information
     * @throws RemoteException
     */
    @Override
    public StatusData updateAdmin(StatusData info) throws RemoteException {
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
