package info.kgeorgiy.ja.labazov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteAccount extends UnicastRemoteObject implements Account {
    private final LocalAccount account;

    public RemoteAccount(LocalAccount account, int port) throws RemoteException {
        super(port);
        this.account = account;
    }

    @Override
    public String getId() throws RemoteException {
        return account.getId();
    }

    @Override
    public int getAmount() throws RemoteException {
        return account.getAmount();
    }

    @Override
    public void setAmount(int amount) throws RemoteException {
        account.setAmount(amount);
    }
}
