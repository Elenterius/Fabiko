# Solving IK Chains and Structures

Once a chain exists and contains one or more bones, it can be solved by calling the `solveForTarget()` method
and providing it a target location to solve the chain for. Targets may be specified as Vec2f or Vec3f objects,
or as floats.

If multiple chains exists in a structure, then calling `solveForTarget()` along with a target location results
in each chain in the structure being solved in a first-in-first-solved manner for the same target location.
However, if a chain in a structure has the `useEmbeddedTarget` mode enabled, then the chain will be solved for its
own embedded target location rather than any provided target. In this manner, a structure may contain a combination of
chains which can be solved for the specified target or their own embedded target location with a single call to
`solveForTarget()` on the structure. If all chains in a structure use embedded targets then the provided target location
is effectively ignored.

Chains that use embedded targets may update their targets via the chain’s `updateEmbededTarget()` method,
and individual chains may be solved for their embedded targets via a call to `solveForEmbeddedTarget()`.