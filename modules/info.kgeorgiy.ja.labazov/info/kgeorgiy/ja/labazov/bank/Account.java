package info.kgeorgiy.ja.labazov.bank;

import java.rmi.*;

public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(int amount) throws RemoteException;

    static String getOwnerId(Account account) throws RemoteException {
        int separator = account.getId().indexOf(':');
        if (separator < 0) {
            return null;
        }
        return account.getId().substring(0, separator);
    }
}