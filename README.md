<img height="150" src="https://i.imgur.com/YEPFEcx.png">

The [Minie Project][minie] is about improving the integration of
[Bullet Real-Time Physics][bullet] into the
[jMonkeyEngine Game Engine][jme].

It contains 4 sub-projects:

 1. MinieLibrary: the Minie runtime library (in Java)
 2. MinieExamples: demos, examples, and test software (in Java)
 3. [DacWizard][]: a GUI application to configure a ragdoll (in Java)
 4. MinieAssets: generate assets used in MinieExamples (in Java)

Java source code is provided under
[a FreeBSD license](https://github.com/stephengold/Minie/blob/master/LICENSE).

## Contents of this document

 + [Why use Minie?](#why)
 + [Downloads](#downloads)
 + [Conventions](#conventions)
 + [History](#history)
 + [How to install the SDK and the Minie Project](#install)
 + [How to add Minie to an existing project](#add)
 + [Choosing a collision shape](#shape)
 + [An introduction to DynamicAnimControl](#dac)
 + [Collision detection](#detect)
 + [An introduction to soft-body physics](#softbody)
 + [External links](#links)
 + [Acknowledgments](#acks)

<a name="why"/>

## Why use Minie?

jMonkeyEngine comes with 2 Bullet integration libraries.
Why use Minie instead of `jme3-bullet` or `jme3-jbullet`?

 + Minie has many more features. (See the feature list below.)
 + Minie fixes many bugs found in the jMonkeyEngine libraries.
   (See the fix list below.)
 + Due to its shorter release cycle, future features and bug fixes
   will probably appear first in Minie.
 + Minie has automated tests that reduce the risk of regressions and new bugs.
 + Minie's classes are better encapsulated, with fewer public/protected fields
   and less aliasing of small objects like vectors.  This reduces the risk
   of accidentally corrupting its internal data structures.
 + Minie validates method arguments.  This helps detect usage errors that
   can lead to subtle bugs.
 + Minie's source code is more readable and better documented.

Summary of added features:

 + `DynamicAnimControl` for ragdoll/rope simulation:
    + set dynamic/kinematic mode per bone
    + understands attachments
    + highly configurable, with many options for bone mass, center, and shape
    + apply inverse-kinematic controllers and joints
 + Soft-body simulation based on `btSoftBody` and `btSoftRigidDynamicsWorld`,
    including anchors and soft-body joints
 + `MultiSphere` collision shapes based on `btMultiSphereShape`
 + `EmptyShape` collision shapes based on `btEmptyShape`
 + debugging aids:
    + dump the contents of a `BulletAppState` or `PhysicsSpace`
    + visualize physics objects in multiple viewports
    + customize debug material per collision object
    + visualize the local axes, bounding boxes, and/or CCD swept spheres
      of collision objects
    + optional double-sided debug materials
    + optional high-resolution debug meshes for convex shapes
    + options to generate debug meshes that include normals (for shading)
      and/or texture coordinates (for texturing)
 + all joints, shapes, and collision objects implement the `JmeCloneable`
   and `Comparable` interfaces
 + enable/disable a joint
 + single-ended joints
 + settable global default for collision margin
 + access more parameters of rigid bodies:
    + anisotropic friction
    + contact damping
    + contact stiffness
    + contact-processing threshold
    + deactivation time
    + linear factor
    + rolling friction
    + spinning friction
 + option to apply scaling with a `RigidBodyControl`

Some `jme3-bullet` bugs that have been fixed in Minie:

 + 772 scale of a physics shape is applied 2x
 + 877 physics joints don't work unless both bodies are dynamic
 + 883 extra `physicsTick()` callbacks
 + 887 debug mesh ignores scaling of `CollisionShape`
 + 889 disabled physics control gets added to a physics space
 + 894 `setRestitutionOrthoLin()` sets wrong joint parameter
 + 901 collision margin initialized to 0
 + 911 sleeping-threshold setters have unexpected side effects
 + 913 missing implementation of `PhysicsJoint.finalizeNative()`
 + 917 `HingeJoint.read()` fails
 + 918 `getImpulseClamp()` returns the wrong value
 + 919 `UnsatisfiedLinkError` in `getLimitSoftness()`
 + 928 crash caused by too many parallel threads
 + 969 linear factors not cloned
 + 1029 sphere-sphere collisions not reported
 + 1037 performance issue with `HullCollisionShape`
 + 1043 `TestCCD` fails
 + 1058 crash while removing body from `BroadphaseType.SIMPLE` `PhysicsSpace`
 + 1060 doesn't implement `bt32BitAxisSweep3`
 + 1120 scaled GImpact shapes fall through floor
 + 1125 heightfield collision shapes don't match `TerrainQuad`
 + 1134 missing collisions for some rotations of a `GImpactCollisionShape`
 + 1135 `ConeJoint` causes rigid body to disappear on Linux

Some `jme3-bullet`/`jme3-jbullet` classes that Minie omits:

 + `CharacterControl`: use `MinieCharacterControl` or `BetterCharacterControl`
   instead, or else use `PhysicsCharacter` directly
 + `KinematicRagdollControl`, `HumanoidRagdollPreset`, and `RagdollPreset`:
   use `DynamicAnimControl` instead
 + `RagdollUtils`: not needed

Other important differences:

 + The default collision margin increased from 0 to 0.04 .
 + `PhysicsSpace.addAll()` and `PhysicsSpace.removeAll()` add/remove collision
   objects only; they do not add/remove joints.
 + `RagdollCollisionListener` interface changed and moved
   from the `com.jme3.bullet.collision` package
   to the `com.jme3.bullet.animation` package.

<a name="downloads"/>

## Downloads

Newer releases (since v0.5.0) can be downloaded from
[GitHub](https://github.com/stephengold/Minie/releases).

Older releases (v0.1.1 through v0.4.5) can be downloaded from
[the Jme3-utilities Project](https://github.com/stephengold/jme3-utilities/releases).

Maven artifacts are available from
[JFrog Bintray](https://bintray.com/stephengold/jme3utilities).

<a name="conventions"/>

## Conventions

Package names begin with
`jme3utilities.minie.` (if Stephen Gold holds the copyright) or
`com.jme3.` (if the jMonkeyEngine Project holds the copyright).

The source code is compatible with JDK 7.

<a name="history"/>

## History

Most of Minie was originally forked from `jme3-bullet`,
a library in the [jMonkeyEngine Game Engine][jme].

From January 2018 to November 2018, Minie was a sub-project of
[the Jme3-utilities Project][utilities].

Since November 2018, the Minie Project has been an independent project at
[GitHub][minie].

The evolution of Minie is chronicled in
[its release notes](https://github.com/stephengold/Minie/blob/master/MinieLibrary/release-notes.md).

<a name="install"/>

## How to install the SDK and the Minie Project

### jMonkeyEngine3 (jME3) Software Development Kit (SDK)

Minie currently targets Version 3.2.3 of jMonkeyEngine.
You are welcome to use the Engine without also using the SDK, but I use the SDK,
and the following installation instructions assume you will too.

The hardware and software requirements of the SDK are documented on
[the JME wiki](https://jmonkeyengine.github.io/wiki/jme3/requirements.html).

 1. Download a jMonkeyEngine 3.2 SDK from
    [GitHub](https://github.com/jMonkeyEngine/sdk/releases).
 2. Install the SDK, which includes:
    + the engine itself,
    + an integrated development environment (IDE) based on NetBeans,
    + various plugins, and
    + the Blender 3D application.
 3. To open the Minie project in the IDE (or NetBeans), you will need the
    `Gradle Support` plugin.  Download and install it before proceeding.
    If this plugin isn't shown in the IDE's "Plugins" tool,
    you can download it from
    [GitHub](https://github.com/kelemen/netbeans-gradle-project/releases).
    You don't need this plugin if you merely want to use a pre-built Minie
    release in an Ant project.

### Source files

Clone the Minie repository using Git:

 1. Open the "Clone Repository" wizard in the IDE:
     + Menu bar -> "Team" -> "Git" -> "Clone..." or
     + Menu bar -> "Team" -> "Remote" -> "Clone..."
 2. For "Repository URL:" specify
    `https://github.com/stephengold/Minie.git`
 3. Clear the "User:" and "Password:" text boxes.
 4. For "Clone into:" specify a writable folder (on a local filesystem)
    that doesn't already contain "Minie".
 5. Click on the "Next >" button.
 6. Make sure the "master" remote branch is checked.
 7. Click on the "Next >" button again.
 8. Make sure the Checkout Branch is set to "master".
 9. Make sure the "Scan for NetBeans Projects after Clone" box is checked.
10. Click on the "Finish" button.
11. When the "Clone Completed" dialog appears, click on the "Open Project..."
    button.
12. Expand the root project node to reveal the sub-projects.
13. Select both sub-projects using control-click, then click on the
    "Open" button.

### Build the project

 1. In the "Projects" window of the IDE,
    right-click on the "Minie [root]" project to select it.
 2. Select "Build".

<a name="add"/>

## How to add Minie to an existing project

Adding Minie to an existing JME3 project should be a simple 6-step process:

 1. Remove any existing physics libraries which might interfere with Minie.
 2. Add libraries to the classpath.
 3. Create, configure, and attach a `BulletAppState`,
    if the application doesn't already do so.
 4. Configure the `PhysicsSpace`,
    if the application doesn't already do so.
 5. Create physics controls, collision objects, and joints
    and add them to the `PhysicsSpace`,
    if the application doesn't already do so.
 6. Test and tune as necessary.

### Remove any existing physics libraries

Minie replaces (and is therefore incompatible with) the following
jMonkeyEngine libraries:

 + `jme3-bullet`
 + `jme3-bullet-native`
 + `jme3-jbullet`

Before adding Minie, you should remove these libraries from your project so
they won't interfere with Minie.

#### For Gradle projects

Look for artifacts with these names in the `dependencies` section
of your project's `gradle.build` file and remove them.

#### For Ant projects

Open the project's properties in the IDE (JME 3.2 SDK or NetBeans 8.2):

 1. Right-click on the project (not its assets) in the "Projects" window.
 2. Select "Properties to open the "Project Properties" dialog.
 3. Under "Categories:" select "Libraries".
 4. Click on the "Compile" tab.
 5. Look for libraries with these names in the "Compile-time Libraries"
    listbox.  Select them and click on the "Remove" button.
 6. Click on the "OK" button to exit the "Project Properties" dialog.

### Add libraries to the classpath

Minie comes pre-built as a single library that includes both Java classes
and native libraries.  The Minie library depends on the
jme3-utilities-heart library, which in turn depends on
the standard jme3-core library from jMonkeyEngine.

#### For Gradle projects

For projects built using Maven or Gradle, it is sufficient to specify the
dependency on the Minie library.  The build tools should automatically
resolve the remaining dependencies automatically.

Because Minie is not on JCenter yet, you must explicitly specify the
repository location:

    repositories {
        maven { url 'https://dl.bintray.com/stephengold/jme3utilities' }
        jcenter()
    }
    dependencies {
        compile 'jme3utilities:Minie:0.9.5for32'
    }

#### For Ant projects

For projects built using Ant, download the 2 non-standard
libraries from GitHub:

 + https://github.com/stephengold/Minie/releases/tag/0.9.5for32
 + https://github.com/stephengold/jme3-utilities/releases/tag/heart-2.29.0for32

You'll want both class JARs
and probably the `-sources` and `-javadoc` JARs as well.

Open the project's properties in the IDE (JME 3.2 SDK or NetBeans 8.2):

 1. Right-click on the project (not its assets) in the "Projects" window.
 2. Select "Properties to open the "Project Properties" dialog.
 3. Under "Categories:" select "Libraries".
 4. Click on the "Compile" tab.
 5. Add the `jme3-utilities-heart` class JAR:
    + Click on the "Add JAR/Folder" button.
    + Navigate to the "jme3-utilities" project folder.
    + Open the "heart" sub-project folder.
    + Navigate to the "build/libs" folder.
    + Select the "jme3-utilities-heart-2.29.0for32.jar" file.
    + Click on the "Open" button.
 6. (optional) Add JARs for javadoc and sources:
    + Click on the "Edit" button.
    + Click on the "Browse..." button to the right of "Javadoc:"
    + Select the "jme3-utilities-heart-2.29.0for32-javadoc.jar" file.
    + Click on the "Open" button.
    + Click on the "Browse..." button to the right of "Sources:"
    + Select the "jme3-utilities-heart-2.29.0for32-sources.jar" file.
    + Click on the "Open" button again.
    + Click on the "OK" button to close the "Edit Jar Reference" dialog.
 7. Similarly, add the Minie JAR(s).
 8. Click on the "OK" button to exit the "Project Properties" dialog.

### Create, configure, and attach a BulletAppState

Strictly speaking, a `BulletAppState` isn't required for Minie, but
it does provide a convenient interface for configuring, accessing, updating,
and debugging a `PhysicsSpace`.

If your application already has a `BulletAppState`, the code will probably
work fine with Minie.
If not, here is a snippet to guide you:

        SoftPhysicsAppState bas = new SoftPhysicsAppState();
        stateManager.attach(bas);
        PhysicsSoftSpace physicsSpace = bas.getPhysicsSoftSpace();

If you don't need soft bodies, you can instantiate a `BulletAppState` directly:

        BulletAppState bas = new BulletAppState();
        stateManager.attach(bas);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();

By default, the physics simulation executes on the render thread.
To execute it on a parallel thread, use:

        bas.setThreadingType(BulletAppState.ThreadingType.PARALLEL);

By default, simulation advances based on the time per frame (tpf)
calculated by the renderer.
To advance the physics simulation at a different rate, use:

        bas.setSpeed(0.5f); // simulate physics at half speed

By default, a Dynamic Bounding-Volume Tree (DBVT) is used for broadphase
collision detection.
To specify a different data structure, use `setBroadphaseType()`:

        SoftPhysicsAppState bas = new SoftPhysicsAppState();
        bas.setBroadphaseType(PhysicsSpace.BroadphaseType.AXIS_SWEEP_3);
        bas.setWorldMax(new Vector3f(1000f, 10f, 1000f));
        bas.setWorldMin(new Vector3f(-1000f, -10f, -1000f));
        stateManager.attach(bas);
        PhysicsSoftSpace physicsSpace = bas.getPhysicsSoftSpace();

By default, debug visualization is disabled. To enable it, use:

        bas.setDebugEnabled(true);

By default, debug visualization renders only to the
application's main `ViewPort`.
To specify a different `ViewPort` (or an array of viewports) use:

        bas.setDebugViewPorts(viewPortArray);

By default, debug visualization renders the shape of every
`PhysicsCollisionObject`, but not its bounding box or swept sphere.
To override these defaults, set filters to identify which objects
should render each feature:

        BulletDebugAppState.DebugAppStateFilter all = new FilterAll(true);
        BulletDebugAppState.DebugAppStateFilter none = new FilterAll(false);
        bas.setDebugBoundingBoxFilter(all); // all bounding boxes
        bas.setDebugFilter(none);           // no collision shapes
        bas.setDebugSweptSphereFilter(all); // all swept spheres

By default, debug visualization doesn't render the local axes of
collision objects.
To override this default, set the axis lengths to a positive value:

        bas.setAxisLength(1f);

If local axes are rendered, then by default they are drawn using
lines one pixel wide.
You can specify wider lines:

        bas.setDebugAxisLineWidth(3f); // axis arrows 3 pixels wide

or you can specify 3-D arrows:

        bas.setDebugAxisLineWidth(0f); // solid axis arrows

By default, Minie visualizes collision shapes using wire materials:

 + yellow for any collision object without contact response,
   which includes any `PhysicsGhostObject`
 + magenta for a `PhysicsRigidBody` (with contact response)
   that's both dynamic and active
 + blue for a `PhysicsRigidBody` (with contact response) that's either
   static or kinematic or sleeping
 + pink for a `PhysicsCharacter` (with contact response)
 + red for a `PhysicsSoftBody` with faces
 + orange for a `PhysicsSoftBody` with links but no faces

Wireframe materials don't require lighting.
However, it's possible to override the default debug materials
on a per-object basis, and such materials might require lighting.
`BulletAppState` invokes a callback during initialization that can
be used to add lighting for debug visualization:

        DebugInitListener callbackObject = new DebugInitListener() {
            public void bulletDebugInit(Node physicsDebugRootNode) {
                AmbientLight ambient = new AmbientLight(aColor);
                physicsDebugRootNode.addLight(ambient);
                DirectionalLight sun = new DirectionalLight(direction, dColor);
                physicsDebugRootNode.addLight(sun);
            }
        };
        bas.setDebugInitListener(callbackObject);

### Configure the PhysicsSpace

Attaching a `BulletAppState` instantiates a `PhysicsSpace` that
you can access immediately:

        PhysicsSpace space = bas.getPhysicsSpace();

`SoftPhysicsAppState` instantiates a `PhysicsSoftSpace`, which is a subclass:

        PhysicsSoftSpace space = bas.getPhysicsSoftSpace();

Physics simulation can run with a fixed time step or a variable time step.
The default configuration is a fixed time step of 1/60 second
with up to 4 time steps per frame.

To configure a variable time step with a maximum of 0.25 seconds:

        space.setMaxSubSteps(0);
        space.setMaxTimeStep(0.25f);

To configure a fixed time step of 0.01 second with up to 6 time steps per frame:

        space.setAccuracy(0.01f);
        space.setMaxSubSteps(6);

Note that `setAccuracy()` has no effect when `maxSubSteps==0`,
whereas `setMaxTimeStep()` has no effect when `maxSubSteps>0`.

Bullet's contact solver performs a fixed number of iterations per time step,
by default, 10.  For higher-quality simualtion, increase this number.  For
instance, to use 20 iterations:

        space.setSolverNumIterations(20);

TODO: gravity, ray-test flags, SoftBodyWorldInfo

### Global configuration

By default, the native library prints a startup message to `System.out`.
Once the library is loaded (but not started) you can disable this message:

        DebugTools.setStartupMessageEnabled(false);

The default collision margin for new shapes is 0.04 physics-space units.
To configure a default margin of 0.1 psu:

        CollisionShape.setDefaultMargin(0.1f);

### Create physics controls, collision objects, and joints

Section to be written.

### Test and tune

Section to be written.

<a name="shape"/>

## Choosing a collision shape

Minie provides more than a dozen `CollisionShape` subclasses.
Because jMonkeyEngine models are composed of triangular meshes,
beginners are often tempted to use mesh-based shapes
(such as `GImpactCollisionShape`) for everything.
However, since mesh-based collision detection is CPU-intensive, primitive
convex shapes (such as boxes and spheres) are usually a better choice, even
if they don't match the model's shape exactly.
In particular, `CapsuleCollisionShape` is often used with humanoid models.

    if (the object isn't involved in collisions) {
        use an EmptyShape
    } else if (its shape can be approximated by an infinite plane) {
        use a PlaneCollisionShape
    } else if (its shape can be approximated by a triangle or a tetrahedron) {
        use a SimplexCollisionShape
    } else if (its shape can be approximated by a centered sphere) {
        use a SphereCollisionShape
    } else if (its shape can be approximated by a centered rectangular solid) {
        use a BoxCollisionShape
    } else if (its shape can be approximated by a centered capsule) {
        use a CapsuleCollisionShape
    } else if (its shape can be approximated by a centered cylinder) {
        use a CylinderCollisionShape
    } else if (its shape can be approximated by a centered cone) {
        use a ConeCollisionShape
    } else if (its shape can be approximated by an ellipsoid
                or an eccentric sphere
                or an eccentric capsule
                or the convex hull of multiple spheres) {
        use a MultiSphere
    } else if (its shape can be approximated by an eccentric rectangular solid
                or an eccentric cylinder
                or an eccentric cone
                or a combination of convex primitives) {
            use a CompoundCollisionShape
    } else if (the object does not move) {
        if (it is a 2-D heightfield) {
            use a HeightfieldCollisionShape
        } else {
            use a MeshCollisionShape
        }
    } else { // if the object moves
        if (its shape can be approximated by the convex hull of a mesh) {
            use a HullCollisionShape
        } else {
            use a GImpactCollisionShape
        }
    }

(Pseudocode adapted from the flowchart on page 13 of the [Bullet User Manual][manual].)

<a name="dac"/>

## An introduction to DynamicAnimControl

The centerpiece of Minie is `DynamicAnimControl`, a new `PhysicsControl`.
Adding a `DynamicAnimControl` to an animated model provides ragdoll physics and
inverse kinematics.

Configuration of `DynamicAnimControl` mostly takes place before the `Control`
is added to a model `Spatial`.  Adding the `Control` to a `Spatial`
automatically creates the ragdoll, including rigid bodies and joints.
No ragdoll exists before the `Control` is added to a `Spatial`,
and removing a `Control` from its controlled `Spatial` destroys the ragdoll.

The controlled `Spatial` must include the model's `SkeletonControl`.
Usually this is the model's root `Spatial`, but not always.
For a very simple example, see
[HelloDac.java](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/tutorial/HelloDac.java).

A model's ragdoll is composed of rigid bodies joined by 6-DOF joints.
Within the `Control`, each `PhysicsRigidBody` is represented by
a `PhysicsLink`, and the links are organized into a tree hierarchy.

`PhysicsLink` has 3 subclasses:

 + `BoneLink`: manages one or more bones in the model’s `Skeleton`.
   Each `BoneLink` has a parent link, to which it is jointed.
   Its parent may be another `BoneLink` or it may be a `TorsoLink`.
 + `TorsoLink`: is always the root of a link hierarchy,
   so it has no parent link.
   It manages all root bones in the model's `Skeleton`.  It also manages any
   `Skeleton` bones that aren't managed by a `BoneLink`.
 + `AttachmentLink`: manages a non-animated model that's
   attached to the main model by means of an attachment `Node`.
   An `AttachmentLink` cannot be the parent of a link.

The default constructor for `DynamicAnimControl` is configured to create a
ragdoll with no bone links, only a `TorsoLink`.
Before adding the `Control` to a `Spatial`, specify which `Skeleton` bones
should be linked, by invoking the `link()` method for each of those bones.

I recommend starting with a default `LinkConfig` and a generous range of motion
for each linked bone:

    dynamicAnimControl.link(boneName, new LinkConfig(), new RangeOfMotion(1f, 1f, 1f));

For a simple example, see
[HelloBoneLink.java](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/tutorial/HelloBoneLink.java).

When you run `HelloBoneLink`, press the space bar to put the control into
dynamic mode.
You'll see the linked bones go limp while the remainder of the ninja model
stays rigid.

As an alternative to hand-coding the control configuration,
you can generate configuration code for a specific model using
the [DacWizard application][dacwizard], which uses animation data to estimate
the range of motion for each linked bone.

You probably don't want to link every `Bone` in the model's `Skeleton`.
For instance, if the model has articulated fingers, you probably want to link
the hand bones but not the individual finger bones.
Unlinked bones will be managed by the nearest linked ancestor `Bone`.
The `TorsoLink` will manage any bones for which no ancestor `Bone` is linked.
If you link too many bones, the ragdoll may become inflexible or jittery
due to collisions between rigid bodies that don't share a `PhysicsJoint`.

<a name="detect"/>

## Collision detection

Minie provides 4 collision-detection interfaces:

 1. You can add collision listeners to a `PhysicsSpace` to be notified about
    up to 4 collision contacts per colliding object, including references to
    both objects.
 2. You can add collision-group listeners to a `PhysicsSpace` to be notified
    about collisions involving particular groups.
 3. You can add ragdoll-collision listeners to any `DynamicAnimControl` to be
    notified about collision contacts involving its ragdoll where the applied
    impulse exceeds a certain threshold. (This is built atop interface #1.)
 4. You can invoke `getOverlappingObjects()` on any `PhysicsGhostObject` to
    enumerate all collision objects that overlap with it, based on
    axis-aligned bounding boxes.

<a name="softbody"/>

## An introduction to soft-body physics

While rope, cloth, and foam rubber can be simulated using many small rigid
bodies, it is more convenient and efficient to treat them as individual bodies
that can be deformed.
To this end, Minie supports simulation of soft bodies in a manner
roughly analogous to that for rigid bodies:

 + In place of `BulletAppState`, use `SoftPhysicsAppState`.
 + In place of `PhysicsSpace`, use `PhysicsSoftSpace`.
 + In place of `PhysicsRigidBody`, use `PhysicsSoftBody`.
 + In place of `RigidBodyControl`, use `SoftBodyControl`.

Soft bodies can collide with both rigid bodies and soft bodies.
They can also be joined to other bodies of both types, using special subclasses
of `PhysicsJoint`.

Unlike a rigid body, a soft body doesn't have a `CollisionShape` or
a physics transform.
Instead, it is composed of point masses (called "nodes") whose locations
are specified in physics-space coordinates.
A soft body's shape, structure, and position are all defined
by its mesh of nodes:

 + To simulate rope, nodes can be connected in pairs (called "links").
 + To simulate cloth, nodes can be connected to form triangles (called "faces").
 + To simulate foam rubber, nodes can be connected to form tetrahedra (also
   called "tetras").

(Soft-body nodes are unrelated to `com.jme3.scene.Node`,
the kind of node used to define the scene graph.)

Like rigid bodies, soft bodies can be constructed directly (using `new`)
or they can be created using physics controls (such as `SoftBodyControl`)
which tie them to particular spatials in the scene graph.
However, unlike a `RigidBodyControl`, a `SoftBodyControl` can only be
dynamic (spatial follows body) never kinematic (body follows spatial).

### Soft-body configuration and pose matching

Each soft body has numerous properties that can affect its behavior.
Most of these are stored in its configuration object, which can be
accessed using `getSoftConfig()`.
Soft bodies and configuration objects are one-to-one.

Configuration properties with `float` values are enumerated
by the `Sbcp` ("soft-body configuration parameter") enum.
For instance, a soft body can have a preferred shape (called its "default pose")
that it tends to return to if deformed.
The strength of this tendency depends on the configuration object's
"pose matching" parameter, which defaults to zero.

For a simple example using `SoftPhysicsAppState`, `SoftBodyControl`, and
pose matching, see
[HelloSoftBody.java](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/tutorial/HelloSoftBody.java).

### Soft-soft collisions

By default, collisions between soft bodies are not handled (ignored).
One way to handle soft-soft collisions for a specific body is to
set the `VF_SS` collision flag in its configuration object:

    SoftBodyConfig config = softBody.getSoftConfig();
    int oldFlags = config.getCollisionFlags();
    config.setCollisionFlags(oldFlags, ConfigFlag.VF_SS);

For a simple example of a collision between 2 soft bodies, see
[HelloSoftSoft.java](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/tutorial/HelloSoftSoft.java).

### Solver iterations

During each physics timestep, the simulator applies a series of
iterative solvers to each soft body:

 + a cluster solver
 + a drift solver
 + a position solver
 + a velocity solver

The number of iterations for each solver is stored in the body's
configuration object.
When simulating collisions, you can improve accuracy by increasing the
number of position-solver iterations:

    SoftBodyConfig config = softBody.getSoftConfig();
    config.setPositionIterations(numIterations);  // default = 1

### Stiffness coefficients

Each soft body has 3 stiffness coefficients.
These are stored in its "material" object,
which is accessed using `getSoftMaterial()`.
Soft bodies and their material objects are one-to-one.
(Soft-body materials are unrelated to `com.jme3.material.Material`,
the kind of material used to render geometries.)

To simulate an object that flexes easily (such as cloth), create a soft
body with many faces and set the angular-stiffness coefficient of its material
to a small value (such as zero):

    PhysicsSoftBody.Material softMaterial = softBody.getSoftMaterial();
    softMaterial.setAngularStiffness(0f); // default=1

For a simple example of cloth simulation, see

[HelloCloth.java](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/tutorial/HelloCloth.java).

TODO: ropes, applying forces, anchors, soft joints, world info, aerodynamics

### Clusters

By default, soft-body collisions are handled using nodes and faces.
As an alternative, they can be handled using groups of connected nodes
(called "clusters").
To enable cluster-based rigid-soft collisions for a specific soft body,
set its `CL_RS` collision flag.
To enable cluster-based soft-soft collisions, set its `CL_SS` flag.

Clusters can overlap, but they can't span multiple bodies.
In other words, a single node can belong to multiple clusters,
but a single cluster can't contain nodes from multiple bodies.

When a soft body is created, it contains no nodes and no clusters.
Once nodes are appended to a body, clusters can be generated automatically,
using an iterative algorithm that's built into Bullet:

    softBody.generateClusters(k, numIterations);

TODO: describe the demo apps

<a name="links"/>

## External links

  + [The Bullet Physics SDK Manual](https://github.com/bulletphysics/bullet3/blob/master/docs/Bullet_User_Manual.pdf)
  + [The Physics section of the JME Wiki](https://wiki.jmonkeyengine.org/jme3/advanced/physics.html)

YouTube videos about Minie:

  + June 2019 teaser #2 (rubber duck)
    [watch](https://www.youtube.com/watch?v=GKc-_SqcpZo) (0:16)
    [source code](https://github.com/stephengold/Minie/blob/d0326f636dbed76c809cb8ec654bfaf94786e988/MinieExamples/src/main/java/jme3utilities/minie/test/TestSoftBodyControl.java)
  + June 2019 teaser #1 (jogger in skirt)
    [watch](https://www.youtube.com/watch?v=lLMBIASzAAM) (0:24)
    [source code](https://github.com/stephengold/Minie/blob/40add685ec9243c3fa1e10f8b38b805a04a32863/MinieExamples/src/main/java/jme3utilities/minie/test/TestSoftBody.java)
  + May 2019 teaser #3 (wind-blown flag)
    [watch](https://www.youtube.com/watch?v=7dcBr0j6sKw) (0:06)
    [source code](https://github.com/stephengold/Minie/blob/9fb33ce21c5082af36ce2969daa79d63b57c0641/MinieExamples/src/main/java/jme3utilities/minie/test/TestSoftBody.java)
  + May 2019 teaser #2 (squishy ball and tablecloth)
    [watch](https://www.youtube.com/watch?v=-ECGEe4CpcY) (0:12)
    [source code](https://github.com/stephengold/Minie/blob/fe55f9baf83158d6347f765b4ff6bbf892056919/MinieExamples/src/main/java/jme3utilities/minie/test/TestSoftBody.java)
  + May 2019 teaser #1 (squishy ball)
    [watch](https://www.youtube.com/watch?v=W3x4gdDn-Ko) (0:13)
    [source code](https://github.com/stephengold/Minie/blob/b1a83f8a6440d8374f09258c6b1d471279833cfa/MinieExamples/src/main/java/jme3utilities/minie/test/TestSoftBody.java)
  + April 2019 walkthru of the DacWizard application
    [watch](https://www.youtube.com/watch?v=iWyrzZe45jA) (8:12)
    [source code](https://github.com/stephengold/Minie/blob/master/DacWizard/src/main/java/jme3utilities/minie/wizard/DacWizard.java)
  + March 2019 short demo (IK for head/eye directions)
    [watch](https://www.youtube.com/watch?v=8zquudx3A1A) (1:25)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/WatchDemo.java)
  + March 2019 teaser (buoyancy)
    [watch](https://www.youtube.com/watch?v=eq09m7pbk5A) (0:10)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/BuoyDemo.java)
  + February 2019 demo (ropes)
    [watch](https://www.youtube.com/watch?v=7PYDAyB5RCE) (4:47)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/RopeDemo.java)
  + December 2018 demo (inverse kinematics)
    [watch](https://www.youtube.com/watch?v=ZGqN9ZCCu-8) (6:27)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/BalanceDemo.java)
  + December 2018 teaser (inverse kinematics)
    [watch](https://www.youtube.com/watch?v=fTWQ9m47GIA) (0:51)
  + November 2018 demo (single-ended joints)
    [watch](https://www.youtube.com/watch?v=Mh9k5AfWzbg) (5:50)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/SeJointDemo.java)
  + November 2018 demo (`MultiSphere` shape)
    [watch](https://www.youtube.com/watch?v=OS2zjB01c6E) (0:13)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/MultiSphereDemo.java)
  + October 2018 demo (`DynamicAnimControl` ragdolls)
    [watch](https://www.youtube.com/watch?v=A1Rii99nb3Q) (2:49)
    [source code](https://github.com/stephengold/Minie/blob/master/MinieExamples/src/main/java/jme3utilities/minie/test/TestDac.java)

[blender]: https://docs.blender.org "Blender Project"
[bsd3]: https://opensource.org/licenses/BSD-3-Clause "3-Clause BSD License"
[bullet]: https://pybullet.org/wordpress "Bullet Real-Time Physics Simulation"
[chrome]: https://www.google.com/chrome "Chrome"
[dacwizard]: https://github.com/stephengold/Minie/tree/master/DacWizard "DacWizard Application"
[elements]: http://www.adobe.com/products/photoshop-elements.html "Photoshop Elements"
[findbugs]: http://findbugs.sourceforge.net "FindBugs Project"
[git]: https://git-scm.com "Git"
[github]: https://github.com "GitHub"
[gradle]: https://gradle.org "Gradle Project"
[jfrog]: https://www.jfrog.com "JFrog"
[jme]: http://jmonkeyengine.org  "jMonkeyEngine Project"
[makehuman]: http://www.makehumancommunity.org/ "MakeHuman Community"
[manual]: https://github.com/bulletphysics/bullet3/blob/master/docs/Bullet_User_Manual.pdf "Bullet User Manual"
[markdown]: https://daringfireball.net/projects/markdown "Markdown Project"
[minie]: https://github.com/stephengold/Minie "Minie Project"
[mint]: https://linuxmint.com "Linux Mint Project"
[netbeans]: https://netbeans.org "NetBeans Project"
[nifty]: http://nifty-gui.github.io/nifty-gui "Nifty GUI Project"
[obs]: https://obsproject.com "Open Broadcaster Software Project"
[utilities]: https://github.com/stephengold/jme3-utilities "Jme3-utilities Project"
[vegdahl]: http://www.cessen.com "Nathan Vegdahl"
[winmerge]: http://winmerge.org "WinMerge Project"

<a name="acks"/>

## Acknowledgments

Like most projects, the Minie Project builds on the work of many who
have gone before.  I therefore acknowledge the following
artists and software developers:

+ Normen Hansen (aka "normen") for creating most of the `jme3-bullet` library
 (on which Minie is based) and also for helpful insights
+ Rémy Bouquet (aka "nehon") for co-creating
  `KinematicRagdollControl` (on which `DynamicAnimControl` is based)
  and also for many helpful insights
+ Jules (aka "dokthar") for creating the soft-body fork of jMonkeyEngine
  from which Minie's soft-body support is derived
+ Paul Speed, for helpful insights
+ "oxplay2", for reporting a `PhysicsRigidBody` bug and helping me pin it down.
+ [Nathan Vegdahl][vegdahl], for creating the Puppet model (used in
  the TestDac walkthru video)
+ plus the creators of (and contributors to) the following software:
    + the [Blender][] 3-D animation suite
    + the [Bullet][] real-time physics library
    + the [FindBugs][] source-code analyzer
    + the [Git][] revision-control system and GitK commit viewer
    + the [Google Chrome web browser][chrome]
    + the [Gradle][] build tool
    + the Java compiler, standard doclet, and runtime environment
    + [jMonkeyEngine][jme] and the jME3 Software Development Kit
    + the [Linux Mint][mint] operating system
    + LWJGL, the Lightweight Java Game Library
    + the [MakeHuman][] Community
    + the [Markdown][] document conversion tool
    + Microsoft Windows
    + the [NetBeans][] integrated development environment
    + the [Nifty][] graphical user interface library
    + [Open Broadcaster Software Studio][obs]
    + the PMD source-code analyzer
    + the [WinMerge][] differencing and merging tool

I am grateful to [JFrog][] and [Github][] for providing free hosting for the
Minie Project and many other open-source projects.

I'm also grateful to my dear Holly, for keeping me sane.

If I've misattributed anything or left anyone out, please let me know so I can
correct the situation: sgold@sonic.net