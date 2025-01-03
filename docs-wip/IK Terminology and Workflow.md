# IK Terminology and Workflow

An IK **chain** is a collection of IK **bones** which are connected to each other. The very first bone in the chain is
called the **_basebone_**, and the start location of the basebone is called the **_base location_**.

Each bone has a start location and an end location which, depending on whether we are working in 2D or in 3D, will be
defined in either 2D or 3D space. The end location of the final bone in a chain is called the **end-effector**.

Each bone also has a single **_joint_** which may control the allowable movement of the bone.

The typical workflow of an IK setup is that:

- A chain is created,
- One or more bones are added to this chain, then
- An attempt is made to **solve** the chain for a given target location.

It is during this solve attempt that, in this case, the FABRIK algorithm is executed which may alter the
configuration of the chain so that the end-effector is as close to the target location as possible.

During this solve attempt:

- The length of the bones does not change, and
- The end location of one bone and the start location of the following bone overlap precisely.

## Structures

Multiple IK chains may be connected to each other by placing them into a special 'holder' object called a
**_structure_**. For example, after adding an initial chain to a structure, we might decide to add a second
chain to the structure where the basebone of that second chain is connected to the end joint of the third bone
in the initial chain (or the start joint of the second bone etc. - it's entirely up to you).

As such, when the first chain moves, the second chain's base location also moves in order to remain attached.

