package org.example.packing.domain.packing;

import org.example.packing.domain.model.PlacedPackage;
import org.example.packing.domain.model.Position;

import java.util.Set;

@FunctionalInterface
public interface CandidatePointGenerator {

	Set<Position> pointsAfterPlacing(PlacedPackage placed);
}
