package com.github.elenterius.fabiko.core;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A `structure` contains one or more [FabrikChain3f] objects, which we can solve for a specified target locations.
///
/// The class is a holder for a list of [FabrikChain3f] objects which allows
/// multiple chains to have their target location updated, as well as solving them.
///
/// If you do not intend on attaching multiple [Chain][FabrikChain3f] objects into a complex structure, for example one with
/// multiple effectors, then you may be better served by creating individual [Chain][FabrikChain3f] objects and using those
/// directly.
public class FabrikStructure3f {

	private final List<FabrikChain3f> chains = new ArrayList<>();

	public List<FabrikChain3f> getChains() {
		return Collections.unmodifiableList(chains);
	}

	public int addChain(FabrikChain3f chain) {
		chains.add(chain);
		return chains.size() - 1;
	}

	public FabrikChain3f getChain(int index) {
		return chains.get(index);
	}

	public void removeChain(int index) {
		chains.remove(index);
	}

	public int getChainCount() {
		return chains.size();
	}

	/// Connect a chain to an existing chain in this structure.
	///
	/// Both chains and bones are zero indexed.
	///
	/// @param chain               The chain to connect to this structure
	/// @param existingChainIndex The index of the chain to connect the new chain to
	/// @param existingBoneIndex  The index of the bone to connect the new chain to within the existing chain
	/// @throws IllegalArgumentException If the existingChainNumber or existingBoneNumber specified to connect to does not exist in this structure then an  is thrown
	public void connectChain(FabrikChain3f chain, int existingChainIndex, int existingBoneIndex) {
		if (existingChainIndex > chains.size()) {
			throw new IllegalArgumentException("Cannot connect to chain " + existingChainIndex + " - no such chain (remember that chains are zero indexed).");
		}

		FabrikChain3f hostChain = chains.get(existingChainIndex);
		if (existingBoneIndex > hostChain.getBoneCount()) {
			throw new IllegalArgumentException("Cannot connect to bone " + existingBoneIndex + " of chain " + existingChainIndex + " - no such bone (remember that bones are zero indexed).");
		}

		// Connect the copy of the provided chain to the specified chain and bone in this structure
		chain.connectToStructure(this, existingChainIndex, existingBoneIndex);

		// The chain as we were provided should be centred on the origin, so we must now make it
		// relative to the start location of the given bone in the given chain.

		Vector3f connectionLocation = hostChain.getBone(existingBoneIndex).getBoneConnectionPointLocation();
		chain.setBaseLocation(connectionLocation);

		// When we have a chain connected to another 'host' chain, the chain is which is connecting in
		// MUST have a fixed base, even though that means the base location is 'fixed' to the connection
		// point on the host chain, rather than a static location.
		chain.setFixedBaseMode(true);

		Vector3f tmp1 = new Vector3f();
		Vector3f tmp2 = new Vector3f();

		// Translate the chain we're connecting to the connection point
		for (int i = 0; i < chain.getBoneCount(); i++) {
			FabrikBone3f bone = chain.getBone(i);

			Vector3fc translatedStart = bone.getStartLocation().add(connectionLocation, tmp1);
			Vector3fc translatedEnd = bone.getEndLocation().add(connectionLocation, tmp2);

			bone.setStartLocation(translatedStart);
			bone.setEndLocation(translatedEnd);
		}

		addChain(chain);
	}

	/// Connect a chain to an existing chain in this structure.
	///
	/// Both chains and bones are zero indexed.
	///
	/// @param chain              the chain to connect to this structure
	/// @param existingChainIndex the index of the chain to connect the new chain to
	/// @param existingBoneIndex  the index of the bone to connect the new chain to within the existing chain
	/// @param connectionPoint    Whether the new chain should connect to the START or END of the specified bone in the specified chain
	/// @throws IllegalArgumentException If the existingChainNumber or existingBoneNumber specified to connect to does not exist in this structure then an  is thrown
	public void connectChain(FabrikChain3f chain, int existingChainIndex, int existingBoneIndex, BoneConnectionPoint connectionPoint) {
		if (existingChainIndex > chains.size()) {
			throw new IllegalArgumentException("Cannot connect to chain " + existingChainIndex + " - no such chain (remember that chains are zero indexed).");
		}

		FabrikChain3f hostChain = chains.get(existingChainIndex);
		if (existingBoneIndex > hostChain.getBoneCount()) {
			throw new IllegalArgumentException("Cannot connect to bone " + existingBoneIndex + " of chain " + existingChainIndex + " - no such bone (remember that bones are zero indexed).");
		}

		// Connect the copy of the provided chain to the specified chain and bone in this structure
		chain.connectToStructure(this, existingChainIndex, existingBoneIndex);

		// The chain as we were provided should be centred on the origin, so we must now make it
		// relative to the start location of the given bone in the given chain.

		hostChain.getBone(existingBoneIndex).setBoneConnectionPoint(connectionPoint);

		Vector3f connectionLocation = hostChain.getBone(existingBoneIndex).getBoneConnectionPointLocation();
		chain.setBaseLocation(connectionLocation);

		// When we have a chain connected to another 'host' chain, the chain is which is connecting in
		// MUST have a fixed base, even though that means the base location is 'fixed' to the connection
		// point on the host chain, rather than a static location.
		chain.setFixedBaseMode(true);

		Vector3f tmp1 = new Vector3f();
		Vector3f tmp2 = new Vector3f();

		// Translate the chain we're connecting to the connection point
		for (int i = 0; i < chain.getBoneCount(); i++) {
			FabrikBone3f bone = chain.getBone(i);

			Vector3fc translatedStart = bone.getStartLocation().add(connectionLocation, tmp1);
			Vector3fc translatedEnd = bone.getEndLocation().add(connectionLocation, tmp2);

			bone.setStartLocation(translatedStart);
			bone.setEndLocation(translatedEnd);
		}

		addChain(chain);
	}

	/// Set the fixed base mode for all chains in this structure
	///
	/// @param flag Whether all chains should operate in fixed base mode `true` or not `false`
	public void setFixedBaseMode(boolean flag) {
		chains.forEach(chain -> chain.setFixedBaseMode(flag));
	}

}
