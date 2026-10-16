package org.example.packing.application.port;

import java.util.UUID;

@FunctionalInterface
public interface IdGenerator {

	UUID newId();
}
