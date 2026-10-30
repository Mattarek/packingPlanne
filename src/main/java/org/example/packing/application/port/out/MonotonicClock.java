package org.example.packing.application.port.out;

@FunctionalInterface
public interface MonotonicClock {

	long nanoTime();
}
