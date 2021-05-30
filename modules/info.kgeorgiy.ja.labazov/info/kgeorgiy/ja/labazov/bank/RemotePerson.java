package info.kgeorgiy.ja.labazov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemotePerson extends UnicastRemoteObject implements Person {
    private final LocalPerson person;
    private final int port;

    public RemotePerson(LocalPerson person, int port) throws RemoteException {
        super(port);
        this.person = person;
        this.port = port;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return person.getFirstName();
    }

    @Override
    public String getLastName() throws RemoteException {
        return person.getLastName();
    }

    @Override
    public long getPassportId() throws RemoteException {
        return person.getPassportId();
    }

    @Override
    public Account getAccount(String subId) throws RemoteException {
        return new RemoteAccount(person.getLocalAccount(subId), port);
    }

    LocalPerson getLocalPerson() {
        return person;
    }

    RemoteAccount createAccount(LocalAccount acc) throws RemoteException {
        return new RemoteAccount(person.createAccount(acc), port);
    }
}
