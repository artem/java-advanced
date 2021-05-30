package info.kgeorgiy.ja.labazov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Person extends Remote {
    String getFirstName() throws RemoteException;

    String getLastName() throws RemoteException;

    long getPassportId() throws RemoteException;

    Account getAccount(String subId) throws RemoteException;
}
