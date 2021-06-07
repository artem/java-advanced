package info.kgeorgiy.ja.labazov.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

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

    @Override
    public void addAmount(int amount) throws RemoteException {
        account.addAmount(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RemoteAccount that = (RemoteAccount) o;
        return Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), account);
    }
}
