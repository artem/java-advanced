package info.kgeorgiy.ja.labazov.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, RemoteAccount> orphanedAccounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        final LocalAccount localAccount = new LocalAccount(id);

        final String ownerId = Account.getOwnerId(localAccount);
        if (ownerId != null) {
            final RemotePerson owner = persons.get(ownerId);
            if (owner == null) {
                System.out.println("Owner with id does not exist: " + ownerId);
                return null;
            }
            return owner.createAccount(localAccount);
        } else {
            System.out.println("Creating account " + id);
            final RemoteAccount account = new RemoteAccount(localAccount, port);

            if (orphanedAccounts.putIfAbsent(id, account) == null) {
                return account;
            } else {
                return getAccount(id);
            }
        }
    }

    @Override
    public Account getAccount(final String id) { // todo owned
        System.out.println("Retrieving account " + id);
        return orphanedAccounts.get(id);
    }

    @Override
    public boolean createPerson(final String firstName, final String lastName, final long passportId) throws RemoteException {
        final String id = String.valueOf(passportId);
        System.out.println("Creating person " + id);
        final RemotePerson person = new RemotePerson(new LocalPerson(firstName, lastName, passportId), port);

        return persons.putIfAbsent(id, person) == null;
    }

    @Override
    public Person getPerson(final String id, final boolean remote) {
        System.out.println("Retrieving person " + id);

        final RemotePerson person = persons.get(id);
        if (person != null) {
            if (remote) {
                return person;
            } else {
                return person.getLocalPerson();
            }
        } else {
            return null;
        }
    }
}
