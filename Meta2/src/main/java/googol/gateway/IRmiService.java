package googol.gateway;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRmiService extends Remote {
    String updateAdmin(String info) throws RemoteException;
}
