package googol.gateway;

import googol.barrel.IISBs;
import googol.client.IClient;

import java.rmi.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IGateway extends Remote {
    int subscribe(String name, IClient client) throws RemoteException;

    int getMaxIndex(int size) throws RemoteException;

    void getStatus(String name) throws RemoteException;

    void insertURL(String name, String s) throws RemoteException;

    String requestNext() throws RemoteException;

    void showURLs() throws RemoteException;

    void addURLs(ArrayList<String> newURLs) throws RemoteException;

    void finishProcess(String url) throws RemoteException;

    int searchWords(String name, String s, int index, boolean flag) throws RemoteException;

    void showMenuAssociated(String name) throws RemoteException;

    int showAssociatedURLs (String name, String url) throws RemoteException;

    void deleteClient(String name) throws RemoteException;

    ArrayList<String> requestSavedURLs() throws RemoteException;

    void barrelRegistration(IISBs barrel) throws RemoteException;

    void printOnServer(String s) throws RemoteException;
}