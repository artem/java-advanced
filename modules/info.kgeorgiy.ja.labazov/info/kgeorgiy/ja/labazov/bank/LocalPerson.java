package info.kgeorgiy.ja.labazov.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPerson implements Person, Serializable {
    private final String firstName;
    private final String lastName;
    private final long passportId;
    private final Map<String, LocalAccount> accounts;

    public LocalPerson(String firstName, String lastName, long passportId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passportId = passportId;
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    @Override
    public long getPassportId() throws RemoteException {
        return passportId;
    }

    @Override
    public Account getAccount(String subId) {
        return getLocalAccount(subId);
    }

    LocalAccount getLocalAccount(String subId) {
        return accounts.get(passportId + ":" + subId);
    }

    LocalAccount createAccount(LocalAccount acc) {
        if (accounts.putIfAbsent(acc.getId(), acc) == null) {
            return acc;
        } else {
            return accounts.get(acc.getId());
        }
    }
}
