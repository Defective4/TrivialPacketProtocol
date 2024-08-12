package io.github.defective4.cmdserver.common.token;

import java.util.Objects;

/**
 * A fixed token provider implementation. <br>
 * It always returns the same token provided in the constructor.<br>
 * It's used by default in newly created clients and servers.
 */
public class FixedTokenProvider implements TokenProvider {

    private final char[] token;

    /**
     * Default constructor
     *
     * @param  token
     * @throws NullPointerException if token is null
     */
    public FixedTokenProvider(char[] token) {
        Objects.requireNonNull(token);
        this.token = token;
    }

    @Override
    public char[] provide() {
        return token;
    }

}
