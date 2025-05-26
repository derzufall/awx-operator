package de.wolkenzentrale.operator.awx.model.common;

import java.util.Objects;

/**
 * Key object for identifying connections in the client map
 */
public class ConnectionKey {
    private final String namespace;
    private final String name;
    
    public ConnectionKey(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getName() {
        return name;
    }
    
    public static ConnectionKey fromConnection(Connection connection) {
        return new ConnectionKey(connection.getNamespace(), connection.getName());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionKey that = (ConnectionKey) o;
        return Objects.equals(namespace, that.namespace) && 
               Objects.equals(name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }
    
    @Override
    public String toString() {
        return namespace + "/" + name;
    }
}