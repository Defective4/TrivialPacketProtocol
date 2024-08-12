package io.github.defective4.cmdserver.common.token;

/**
 * This interface is meant to provide tokens to the client or server depending
 * on current conditions. <br>
 * It could be useful to, for example, create a time based token for better
 * security.<br>
 * {@link FixedTokenProvider} is the default, fixed implementation of this
 * interface.
 */
public interface TokenProvider {
    /**
     * Retrieve the token
     *
     * @return token
     */
    char[] provide();
}
