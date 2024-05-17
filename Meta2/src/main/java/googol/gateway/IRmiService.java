package googol.gateway;

import org.example.meta2.StatusData;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiService extends Remote {
    StatusData updateAdmin(StatusData info) throws RemoteException;
}
