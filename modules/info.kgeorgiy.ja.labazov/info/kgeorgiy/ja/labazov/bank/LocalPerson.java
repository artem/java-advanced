package info.kgeorgiy.ja.labazov.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    LocalPerson(final LocalPerson other) {
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.passportId = other.passportId;
        this.accounts = new ConcurrentHashMap<>(other.accounts);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalPerson that = (LocalPerson) o;
        return passportId == that.passportId && firstName.equals(that.firstName) && lastName.equals(that.lastName) && accounts.equals(that.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, passportId, accounts);
    }
}
