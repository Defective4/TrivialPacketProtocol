package io.github.defective4.cmdserver.common.packet.handler;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@SuppressWarnings("javadoc")
@Retention(RUNTIME)
@Target(METHOD)
public @interface PacketReceiver {

}
