package googol.downloader;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDownloader extends Remote {
    void run(String url) throws RemoteException;
}