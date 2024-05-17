package googol.gateway;

import org.example.meta2.StatusData;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the RMI service
 */
public interface IRmiService extends Remote {
    StatusData updateAdmin(StatusData info) throws RemoteException;
}
