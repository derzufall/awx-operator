package de.wolkenzentrale.operator.awx.model.common;

import lombok.Builder;
import lombok.Value;
import java.util.Objects;

/**
 * Immutable class representing connection information for an AWX instance.
 * All fields are final and must be set at construction time.
 */
@Value
@Builder
public class Connection {
    /**
     * Name of the connection
     */
    String name;
    
    /**
     * Kubernetes namespace of the connection
     */
    String namespace;

    /**
     * URL of the AWX instance
     */
    String url;
    
    /**
     * Username for authentication
     */
    String username;
    
    /**
     * Password for authentication
     */
    String password;
    
    /**
     * Whether to skip TLS verification
     */
    boolean insecureSkipTlsVerify;

    /**
     * Checks if the connection details (URL, username, password, TLS settings) have changed
     * compared to another connection. This ignores the name and namespace fields.
     * 
     * @param other The other connection to compare with
     * @return true if any connection details have changed, false otherwise
     */
    public boolean hasConnectionDetailsChanged(Connection other) {
        if (other == null) {
            return true;
        }
        return !Objects.equals(url, other.url) ||
               !Objects.equals(username, other.username) ||
               !Objects.equals(password, other.password) ||
               insecureSkipTlsVerify != other.insecureSkipTlsVerify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(namespace, that.namespace) &&
               Objects.equals(url, that.url) &&
               Objects.equals(username, that.username) &&
               Objects.equals(password, that.password) &&
               insecureSkipTlsVerify == that.insecureSkipTlsVerify;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace, url, username, password, insecureSkipTlsVerify);
    }

    /**
     * Gets the connection key for this connection
     * @return A new ConnectionKey instance
     */
    public ConnectionKey getKey() {
        return new ConnectionKey(namespace, name);
    }

    /**
     * Custom toString that excludes the password for security reasons
     */
    @Override
    public String toString() {
        return "Connection{" +
               "name='" + name + '\'' +
               ", namespace='" + namespace + '\'' +
               ", url='" + url + '\'' +
               ", username='" + username + '\'' +
               ", password='[REDACTED]'" +
               ", insecureSkipTlsVerify=" + insecureSkipTlsVerify +
               '}';
    }
} 