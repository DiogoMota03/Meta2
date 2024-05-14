package googol.barrel;

import googol.queue.URLData;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IISBs extends Remote {
    void setSavedURLs(ArrayList<URLData> savedURLs) throws RemoteException;

    void setAssociatedURLs(HashMap<String, ArrayList<String>> associatedURLs) throws RemoteException;

    int getType() throws RemoteException;

    void setType(int type) throws RemoteException;

    int getId() throws RemoteException;

    void setId(int id) throws RemoteException;

    BarrelData getBarrelData() throws RemoteException;

    void setBarrelData(BarrelData bd) throws RemoteException;

    ArrayList<String> getAssociatedURLs(String url) throws RemoteException;

    void setNumSearches(HashMap<String, Integer> numSearches) throws RemoteException;

    void guardaDados() throws RemoteException;

    List<URLData> clientRequest(String[] words) throws RemoteException;

    void areYouAlive() throws RemoteException;

    ArrayList<String> getSavedURLs() throws RemoteException;

    void saveSearch(String s) throws RemoteException;

    String getTop10() throws RemoteException;

    Double getAvgSearchTime() throws RemoteException;

    void saveSearchTime(double i) throws RemoteException;
}
