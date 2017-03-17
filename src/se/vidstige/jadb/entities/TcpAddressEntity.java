package se.vidstige.jadb.entities;


/**
 * Tcp address
 */
public final class TcpAddressEntity {
    private final String host;
    private final Integer port;

    public TcpAddressEntity(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TcpAddressEntity)) return false;

        TcpAddressEntity that = (TcpAddressEntity) o;

        if (!host.equals(that.host)) return false;
        return port.equals(that.port);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }
}

