package googol.queue;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IURL_Queue extends Remote {

    void addURL(String url) throws RemoteException;

    void showURLs() throws RemoteException;

    String requestNextURL() throws RemoteException;

    void finishedURLProcessing(String url) throws RemoteException;
    
    void addAll(ArrayList<String> newURLs) throws RemoteException;
}
