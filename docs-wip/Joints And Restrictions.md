# Joints and Restrictions

Each bone in a chain may be configured to allow only a restricted subset of motion.
There are two specific types of restriction:

- **Basebone restrictions** - which are applied to the **_chain_**, and
- **Joint restrictions** - which are applied to individual **_bones_**.

Depending on whether you are working in 2D or 3D there are a variety of different basebone and standard joint
restriction types.

## Basebone Restrictions

A basebone restriction is a special-case of standard restriction, and is required because the basebone itself is the
very first bone in a chain and as such does not have a previous bone which it may be restricted about.

### 2D Constraints

| Type                | Description                                                                                                            |
|---------------------|------------------------------------------------------------------------------------------------------------------------|
| **NONE**            | Unconstrained                                                                                                          |
| **GLOBAL_ABSOLUTE** | Constrained about a global/world-space direction.                                                                      |
| **LOCAL_ABSOLUTE**  | Constrained about the direction of the connected bone. Basebone needs to be connected to a other chain.                |
| **LOCAL_RELATIVE**  | Constrained about a direction relative to that of the connected bone. Basebone needs to be connected to a other chain. |

### 3D Constraints

| Type             | Description                                                             |
|------------------|-------------------------------------------------------------------------|
| **NONE**         | Unconstrained                                                           |
| **GLOBAL_ROTOR** | Ball-joint constrained about a global / world-space direction.          |
| **LOCAL_ROTOR**  | Ball-joint constrained about the direction of the connected bone.       |
| **GLOBAL_HINGE** | Hinge constrained about a world-space axis.                             |
| **LOCAL_HINGE**  | Hinge constrained about an axis relative to that of the connected bone. |

As before, the **LOCAL_ROTOR** and **LOCAL_HINGE** basebone constraint types are only available to be used by chains
which are connected to other chains. In addition, hinge constraints may have an additional **reference axis** constraint
which is the direction within the axis of the hinge about which clockwise/anticlockwise movement is allowed.

To put this in perspective - if you think of the front door of your house rotating on its hinges: its reference axis
would be when the door is closed, and it can likely rotate zero degrees one way (i.e. it doesn't open outwards) and
maybe up to 100 degrees or such the other way to let people in and out.

## Joint Restrictions

As previously mentioned, each bone has a single joint which can be thought of as being at the start of the bone.

### 2D Constraints

In 2D there no specific joint types as the bones may only rotate clockwise and anticlockwise, however, 2D joint
constraints may be configured to operate in a GLOBAL or LOCAL manner - where a local constraint is relative to the
coordinate system of the previous bone in the chain.

### 3D Constraints

In 3D, there are three distinct types of joint, which are:

| Joint Type       | Description                                                             |
|------------------|-------------------------------------------------------------------------|
| **BALL**         | A 'rotor' / ball-joint constraint about the previous bone in the chain. |
| **GLOBAL_HINGE** | A world-space hinge constraint.                                         |
| **LOCAL_HINGE**  | A hinge constraint relative to the previous bone in the chain.          |

The default joint type of a 3D bone is BALL, and the default constraint angle is 180 degrees - which in effect means
that no constraint is applied. Ball joints only have a single constraint angle which describes the 'rotor' arc the joint
allows, while hinges both have clockwise and anticlockwise constraint angles which may be enforced about a given
reference axis that falls within the plane of the hinge.
