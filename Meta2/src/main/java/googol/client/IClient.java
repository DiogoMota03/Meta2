package googol.client;

import googol.queue.URLData;

import java.rmi.*;
import java.util.List;

public interface IClient extends Remote{

    void print_on_client(String s) throws RemoteException;

    void setCurrentPageURLs(List<URLData> currentPageURLs) throws RemoteException;

    List<URLData> getCurrentPageURLs() throws RemoteException;
}
