package info.kgeorgiy.ja.labazov.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @return created or existing account.
     */
    Account createAccount(String id) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exists.
     */
    Account getAccount(String id) throws RemoteException;

    boolean createPerson(String firstName, String lastName, long passportId) throws RemoteException;
    Person getPerson(String id, boolean remote) throws RemoteException;

    static String getOwnerId(final String id) throws RemoteException {
        int separator = id.indexOf(':');
        if (separator < 0) {
            return null;
        }
        return id.substring(0, separator);
    }
}
