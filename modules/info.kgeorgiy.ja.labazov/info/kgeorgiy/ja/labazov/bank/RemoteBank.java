package info.kgeorgiy.ja.labazov.bank;

import java.rmi.NoSuchObjectException;
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

        final String ownerId = Bank.getOwnerId(localAccount.getId());
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
    public Account getAccount(final String id) throws RemoteException {
        System.out.println("Retrieving account " + id);

        final String ownerId = Bank.getOwnerId(id);

        if (ownerId == null) {
            return orphanedAccounts.get(id);
        }

        Person owner = persons.get(ownerId);
        if (owner == null) {
            System.out.println("Owner with id does not exist: " + ownerId);
            return null;
        }

        return owner.getAccount(id.substring(ownerId.length() + 1));
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
                final LocalPerson localPerson = person.getLocalPerson();
                return new LocalPerson(localPerson);
            }
        } else {
            return null;
        }
    }
}
