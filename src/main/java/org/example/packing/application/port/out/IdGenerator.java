package org.example.packing.application.port.out;

import java.util.UUID;

@FunctionalInterface
public interface IdGenerator {

	UUID newId();
}
