/*
 Copyright (c) 2020, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.minie.test;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.SoftPhysicsAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.MultiSphere;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.collision.shapes.infos.DebugMeshNormals;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.bullet.debug.DebugInitListener;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.font.Rectangle;
import com.jme3.input.CameraInput;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.math.MyMath;
import jme3utilities.minie.test.common.AbstractDemo;
import jme3utilities.minie.test.shape.ShapeGenerator;
import jme3utilities.ui.CameraOrbitAppState;
import jme3utilities.ui.InputMode;

/**
 * Test/demonstrate dynamic physics by launching projectiles
 * (small/dynamic/rigid bodies) at various targets.
 * <p>
 * Collision objects are rendered entirely by debug visualization.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class TargetDemo
        extends AbstractDemo
        implements DebugInitListener {
    // *************************************************************************
    // constants and loggers

    /**
     * Y coordinate for the top surface of the platform (in physics-space
     * coordinates)
     */
    final private static float platformTopY = 0f;
    /**
     * number of colors/materials for targets
     */
    final private static int numTargetColors = 4;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(TargetDemo.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = TargetDemo.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * AppState to manage the PhysicsSpace
     */
    private BulletAppState bulletAppState;
    /**
     * selected rigid body, or null if none
     */
    private PhysicsRigidBody selectedBody = null;
    /**
     * AppState to manage the status overlay
     */
    private TargetDemoStatus status;
    // *************************************************************************
    // new methods exposed

    /**
     * Count how many rigid bodies are active.
     */
    int countActive() {
        int result = 0;

        Collection<PhysicsRigidBody> rigidBodies
                = getPhysicsSpace().getRigidBodyList();
        for (PhysicsRigidBody rigidBody : rigidBodies) {
            if (rigidBody.isActive()) {
                ++result;
            }
        }

        return result;
    }

    /**
     * Main entry point for the TargetDemo application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        /*
         * Mute the chatty loggers in certain packages.
         */
        Heart.setLoggingLevels(Level.WARNING);
        /*
         * Enable direct-memory tracking.
         */
        BufferUtils.setTrackDirectMemoryEnabled(true);

        Application application = new TargetDemo();
        /*
         * Customize the window's title bar.
         */
        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setTitle(applicationName);

        settings.setGammaCorrection(true);
        settings.setSamples(4); // anti-aliasing
        settings.setVSync(true);
        application.setSettings(settings);

        application.start();
    }

    /**
     * Restart the current scenario.
     */
    void restartScenario() {
        selectBody(null);

        PhysicsSpace physicsSpace = getPhysicsSpace();
        Collection<PhysicsCollisionObject> pcos = physicsSpace.getPcoList();
        for (PhysicsCollisionObject pco : pcos) {
            physicsSpace.removeCollisionObject(pco);
        }

        assert physicsSpace.isEmpty();

        String platformName = status.platformType();
        addPlatform(platformName, platformTopY);

        setUpScenario();
        setDebugMaterialsAll();
    }

    /**
     * Update the debug materials of all collision objects.
     */
    void setDebugMaterialsAll() {
        PhysicsSpace physicsSpace = getPhysicsSpace();
        for (PhysicsCollisionObject pco : physicsSpace.getPcoList()) {
            setDebugMaterial(pco);
        }
    }

    /**
     * Update the ShadowMode of the debug scene.
     */
    void setDebugShadowMode() {
        RenderQueue.ShadowMode mode;
        if (status.isWireframe()) {
            mode = RenderQueue.ShadowMode.Off;
        } else {
            mode = RenderQueue.ShadowMode.CastAndReceive;
        }
        bulletAppState.setDebugShadowMode(mode);
    }
    // *************************************************************************
    // AbstractDemo methods

    /**
     * Initialize this application.
     */
    @Override
    public void actionInitializeApplication() {
        status = new TargetDemoStatus();
        boolean success = stateManager.attach(status);
        assert success;

        configureCamera();
        configureDumper();
        generateMaterials();
        configurePhysics();
        generateShapes();

        ColorRGBA skyColor = new ColorRGBA(0.1f, 0.2f, 0.4f, 1f);
        viewPort.setBackgroundColor(skyColor);

        String platformName = status.platformType();
        addPlatform(platformName, platformTopY);

        renderer.setDefaultAnisotropicFilter(8);
        setUpScenario();
    }

    /**
     * Initialize the library of named materials during startup.
     */
    @Override
    public void generateMaterials() {
        super.generateMaterials();

        ColorRGBA red = new ColorRGBA(0.5f, 0f, 0f, 1f);
        Material projectile = MyAsset.createShinyMaterial(assetManager, red);
        projectile.setFloat("Shininess", 15f);
        registerMaterial("projectile", projectile);

        ColorRGBA lightGray = new ColorRGBA(0.6f, 0.6f, 0.6f, 1f);
        Material selected = MyAsset.createShinyMaterial(assetManager, lightGray);
        selected.setFloat("Shininess", 15f);
        registerMaterial("selected", selected);
        /*
         * shiny, lit materials for targetss
         */
        ColorRGBA targetColors[] = new ColorRGBA[numTargetColors];
        targetColors[0] = new ColorRGBA(0.2f, 0f, 0f, 1f); // ruby
        targetColors[1] = new ColorRGBA(0f, 0.07f, 0f, 1f); // emerald
        targetColors[2] = new ColorRGBA(0f, 0f, 0.3f, 1f); // sapphire
        targetColors[3] = new ColorRGBA(0.2f, 0.1f, 0f, 1f); // topaz

        for (int index = 0; index < targetColors.length; ++index) {
            ColorRGBA color = targetColors[index];
            Material material
                    = MyAsset.createShinyMaterial(assetManager, color);
            material.setFloat("Shininess", 15f);

            registerMaterial("target" + index, material);
        }
    }

    /**
     * Access the active BulletAppState.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    protected BulletAppState getBulletAppState() {
        assert bulletAppState != null;
        return bulletAppState;
    }

    /**
     * Determine the length of debug axis arrows (when they're visible).
     *
     * @return the desired length (in physics-space units, &ge;0)
     */
    @Override
    protected float maxArrowLength() {
        return 2f;
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode dim = getDefaultInputMode();

        dim.bind(AbstractDemo.asCollectGarbage, KeyInput.KEY_G);

        dim.bind("delete last", KeyInput.KEY_BACK);
        dim.bind("delete last", KeyInput.KEY_SUBTRACT);
        dim.bind("delete selected", KeyInput.KEY_DECIMAL);
        dim.bind("delete selected", KeyInput.KEY_DELETE);

        dim.bind(AbstractDemo.asDumpPhysicsSpace, KeyInput.KEY_O);
        dim.bind("dump selected", KeyInput.KEY_LBRACKET);
        dim.bind(AbstractDemo.asDumpViewport, KeyInput.KEY_P);

        dim.bind("launch projectile", KeyInput.KEY_RETURN);
        dim.bind("launch projectile", KeyInput.KEY_INSERT);
        dim.bind("launch projectile", KeyInput.KEY_NUMPAD0);

        dim.bind("next statusLine", KeyInput.KEY_NUMPAD2);
        dim.bind("next value", KeyInput.KEY_EQUALS);
        dim.bind("next value", KeyInput.KEY_NUMPAD6);

        dim.bind("pick", "RMB");
        dim.bind("pick", KeyInput.KEY_R);

        dim.bind("pop selected", KeyInput.KEY_PGUP);

        dim.bind("previous statusLine", KeyInput.KEY_NUMPAD8);
        dim.bind("previous value", KeyInput.KEY_MINUS);
        dim.bind("previous value", KeyInput.KEY_NUMPAD4);

        dim.bind("restart scenario", KeyInput.KEY_NUMPAD5);

        dim.bind("signal " + CameraInput.FLYCAM_LOWER, KeyInput.KEY_DOWN);
        dim.bind("signal " + CameraInput.FLYCAM_RISE, KeyInput.KEY_UP);
        dim.bind("signal orbitLeft", KeyInput.KEY_LEFT);
        dim.bind("signal orbitRight", KeyInput.KEY_RIGHT);

        dim.bind(AbstractDemo.asToggleAabbs, KeyInput.KEY_APOSTROPHE);
        dim.bind(AbstractDemo.asToggleCcdSpheres, KeyInput.KEY_L);
        dim.bind("toggle childColoring", KeyInput.KEY_COMMA);
        dim.bind(AbstractDemo.asToggleGravities, KeyInput.KEY_J);
        dim.bind(AbstractDemo.asToggleHelp, KeyInput.KEY_H);
        dim.bind(AbstractDemo.asTogglePause, KeyInput.KEY_PAUSE);
        dim.bind(AbstractDemo.asTogglePause, KeyInput.KEY_PERIOD);
        dim.bind(AbstractDemo.asTogglePcoAxes, KeyInput.KEY_SEMICOLON);
        dim.bind(AbstractDemo.asToggleVelocities, KeyInput.KEY_K);
        dim.bind("toggle wireframe", KeyInput.KEY_SLASH);
        /*
         * The help node can't be created until all hotkeys are bound.
         */
        addHelp();
    }

    /**
     * Process an action that wasn't handled by the active input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case "delete selected":
                    deleteSelected();
                    return;
                case "dump selected":
                    dumpSelected();
                    return;
                case "launch projectile":
                    launchProjectile();
                    return;

                case "next statusLine":
                    status.advanceSelectedField(+1);
                    return;
                case "next value":
                    status.advanceValue(+1);
                    return;

                case "pick":
                    pick();
                    return;
                case "pop selected":
                    popSelected();
                    return;

                case "previous statusLine":
                    status.advanceSelectedField(-1);
                    return;
                case "previous value":
                    status.advanceValue(-1);
                    return;

                case "restart scenario":
                    restartScenario();
                    return;

                case "toggle childColoring":
                    status.toggleChildColoring();
                    return;
                case "toggle wireframe":
                    status.toggleWireframe();
                    return;
            }
        }
        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Callback invoked after adding a collision object to the PhysicsSpace.
     *
     * @param pco the object that was added (not null)
     */
    @Override
    public void postAdd(PhysicsCollisionObject pco) {
        if (pco instanceof PhysicsRigidBody) {
            PhysicsRigidBody rigidBody = (PhysicsRigidBody) pco;

            float damping = status.damping();
            rigidBody.setDamping(damping, damping);

            float linearThreshold = 1f;
            float angularThreshold = 1f;
            rigidBody.setSleepingThresholds(linearThreshold, angularThreshold);
        }

        float friction = status.friction();
        pco.setFriction(friction);

        float restitution = status.restitution();
        pco.setRestitution(restitution);

        setDebugMaterial(pco);
        pco.setDebugMeshResolution(DebugShapeFactory.highResolution);
    }
    // *************************************************************************
    // DebugInitListener methods

    /**
     * Callback from BulletDebugAppState, invoked just before the debug scene is
     * added to the debug viewports.
     *
     * @param physicsDebugRootNode the root node of the debug scene (not null)
     */
    @Override
    public void bulletDebugInit(Node physicsDebugRootNode) {
        addLighting(physicsDebugRootNode);
    }
    // *************************************************************************
    // private methods

    /**
     * Attach a Node to display hotkey help/hints.
     */
    private void addHelp() {
        float width = 360f;
        float y = cam.getHeight() - 30f;
        float x = cam.getWidth() - width - 10f;
        float height = cam.getHeight() - 20f;
        Rectangle rectangle = new Rectangle(x, y, width, height);

        attachHelpNode(rectangle);
    }

    /**
     * Add lighting and shadows to the specified scene.
     */
    private void addLighting(Spatial rootSpatial) {
        ColorRGBA ambientColor = new ColorRGBA(0.5f, 0.5f, 0.5f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        rootSpatial.addLight(ambient);
        ambient.setName("ambient");

        ColorRGBA directColor = new ColorRGBA(0.7f, 0.7f, 0.7f, 1f);
        Vector3f direction = new Vector3f(1f, -3f, -1f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction, directColor);
        rootSpatial.addLight(sun);
        sun.setName("sun");

        rootSpatial.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        viewPort.clearProcessors();
        DirectionalLightShadowRenderer dlsr
                = new DirectionalLightShadowRenderer(assetManager, 2_048, 3);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        dlsr.setEdgesThickness(5);
        dlsr.setLight(sun);
        dlsr.setShadowIntensity(0.7f);
        viewPort.addProcessor(dlsr);
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        float near = 0.1f;
        float far = 500f;
        MyCamera.setNearFar(cam, near, far);

        flyCam.setDragToRotate(true);
        flyCam.setMoveSpeed(10f);
        flyCam.setZoomSpeed(10f);

        cam.setLocation(new Vector3f(0f, platformTopY + 20f, 40f));
        cam.setRotation(new Quaternion(0f, 0.9649f, -0.263f, 0f));

        CameraOrbitAppState orbitState
                = new CameraOrbitAppState(cam, "orbitLeft", "orbitRight");
        stateManager.attach(orbitState);
    }

    /**
     * Create and configure a new PhysicsSpace.
     */
    private void configurePhysics() {
        DebugShapeFactory.setIndexBuffers(200);

        bulletAppState = new SoftPhysicsAppState();
        bulletAppState.setDebugEnabled(true);
        bulletAppState.setDebugInitListener(this);
        stateManager.attach(bulletAppState);

        float gravity = status.gravity();
        setGravityAll(gravity);
    }

    /**
     * Delete the selected rigid body, if any.
     */
    private void deleteSelected() {
        if (selectedBody != null) {
            getPhysicsSpace().removeCollisionObject(selectedBody);
            selectBody(null);
            activateAll();
        }
    }

    /**
     * Dump the selected rigid body, if any.
     */
    private void dumpSelected() {
        if (selectedBody == null) {
            System.out.printf("%nNo body selected.");
        } else {
            getDumper().dump(selectedBody, "");
        }
    }

    /**
     * Launch a new projectile, its starting position and velocity determined by
     * the camera and mouse cursor.
     */
    private void launchProjectile() {
        Vector2f screenXY = inputManager.getCursorPosition();
        Vector3f from = cam.getWorldCoordinates(screenXY, 0f);
        Vector3f to = cam.getWorldCoordinates(screenXY, 1f);

        Vector3f direction = to.subtract(from).normalizeLocal();
        float initialSpeed = 200f; // psu per second
        Vector3f initialVelocity = direction.mult(initialSpeed);

        float radius = 0.5f; // psu
        CollisionShape shape = new MultiSphere(radius);
        float mass = 0.5f; // pmu
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);

        Material debugMaterial = findMaterial("projectile");
        body.setApplicationData(debugMaterial);
        body.setCcdMotionThreshold(0.01f);
        body.setCcdSweptSphereRadius(radius);
        body.setDebugMaterial(debugMaterial);
        body.setDebugMeshNormals(DebugMeshNormals.Sphere);
        body.setDebugMeshResolution(DebugShapeFactory.highResolution);
        body.setLinearVelocity(initialVelocity);
        body.setPhysicsLocation(from);

        addCollisionObject(body);
    }

    /**
     * Cast a physics ray from the cursor and select the nearest rigid body in
     * the result.
     */
    private void pick() {
        Vector2f screenXY = inputManager.getCursorPosition();
        Vector3f from = cam.getWorldCoordinates(screenXY, 0f);
        Vector3f to = cam.getWorldCoordinates(screenXY, 1f);
        PhysicsSpace space = getPhysicsSpace();
        List<PhysicsRayTestResult> hits = space.rayTest(from, to);

        for (PhysicsRayTestResult hit : hits) {
            PhysicsCollisionObject pco = hit.getCollisionObject();
            if (pco instanceof PhysicsRigidBody) {
                selectBody((PhysicsRigidBody) pco);
                return;
            }
        }
        selectBody(null);
    }

    /**
     * Apply an upward impulse to the selected rigid body.
     */
    private void popSelected() {
        if (selectedBody instanceof PhysicsRigidBody) {
            float gravity = status.gravity();
            float deltaV = FastMath.sqrt(30f * gravity);
            float impulse = selectedBody.getMass() * deltaV;
            Vector3f impulseVector = new Vector3f(0f, impulse, 0f);
            ShapeGenerator random = getGenerator();
            Vector3f offset = random.nextVector3f().multLocal(0.2f);
            PhysicsRigidBody rigidBody = (PhysicsRigidBody) selectedBody;
            rigidBody.applyImpulse(impulseVector, offset);
        }
    }

    /**
     * Register a spherical shape with the specified radius.
     *
     * @param radius the desired radius (in physics-space units, &gt;0)
     */
    private void registerBallShape(float radius) {
        unregisterShape("ball");
        CollisionShape shape = new SphereCollisionShape(radius);
        registerShape("ball", shape);
    }

    /**
     * Register a bowling-pin shape with the specified radius.
     *
     * @param radius the desired radius (in physics-space units, &gt;0)
     */
    private void registerBowlingPinShape(float radius) {
        unregisterShape("bowlingPin");

        String bowlingPinPath = "CollisionShapes/bowlingPin.j3o";
        CollisionShape shape
                = (CollisionShape) assetManager.loadAsset(bowlingPinPath);
        shape = (CollisionShape) Heart.deepCopy(shape);

        BoundingBox bounds
                = shape.boundingBox(Vector3f.ZERO, Matrix3f.IDENTITY, null);
        float xHalfExtent = bounds.getXExtent();
        float yHalfExtent = bounds.getYExtent();
        float unscaledRadius = (xHalfExtent + yHalfExtent) / 2f;
        float scale = radius / unscaledRadius;
        shape.setScale(scale);

        registerShape("bowlingPin", shape);
    }

    /**
     * Register a brick shape with the specified name, height, and length.
     *
     * @param shapeName (Z axis, not null)
     * @param height the total height (Y axis, in physics-space units, &gt;0)
     * @param length the total length (X axis, in physics-space units, &gt;0)
     */
    private void registerBrickShape(String shapeName, float height,
            float length, float depth) {
        unregisterShape(shapeName);

        float halfHeight = height / 2f;
        float halfLength = length / 2f;
        float halfDepth = depth / 2f;
        CollisionShape shape
                = new BoxCollisionShape(halfLength, halfHeight, halfDepth);

        registerShape(shapeName, shape);
    }

    /**
     * Register a can shape with the specified radius and height.
     *
     * @param radius the radius (in physics-space units, &gt;0)
     * @param height the total height (Y axis, in physics-space units, &gt;0)
     */
    private void registerCanShape(float radius, float height) {
        unregisterShape("can");
        CollisionShape shape = new CylinderCollisionShape(radius, height,
                PhysicsSpace.AXIS_Y);
        registerShape("can", shape);
    }

    /**
     * Register a domino shape with the specified length.
     *
     * @param length the total length (Y axis, in physics-space units, &gt;0)
     */
    private void registerDominoShape(float length) {
        unregisterShape("domino");

        float halfLength = length / 2f;
        float halfThickness = halfLength / 5f;
        float halfWidth = halfLength / 2f;
        CollisionShape shape
                = new BoxCollisionShape(halfThickness, halfLength, halfWidth);

        registerShape("domino", shape);
    }

    /**
     * Alter which rigid body is selected.
     *
     * @param rigidBody the body to select (or null)
     */
    private void selectBody(PhysicsRigidBody rigidBody) {
        if (rigidBody != selectedBody) {
            selectedBody = rigidBody;
            setDebugMaterialsAll();
        }
    }

    /**
     * Update the debug materials of the specified collision object.
     */
    private void setDebugMaterial(PhysicsCollisionObject pco) {
        CollisionShape shape = pco.getCollisionShape();

        Material debugMaterial;
        if (selectedBody == pco) {
            debugMaterial = findMaterial("selected");

        } else if (status.isWireframe()) {
            debugMaterial = null;

        } else if (status.isChildColoring()
                && shape instanceof CompoundCollisionShape) {
            debugMaterial = BulletDebugAppState.enableChildColoring;

        } else {
            // Use the previously set lit material.
            debugMaterial = (Material) pco.getApplicationData();
        }

        pco.setDebugMaterial(debugMaterial);
    }

    /**
     * Set up a single ball as a target.
     *
     * @param location the desired location (in physics-space coordinates, not
     * null, unaffected)
     */
    private void setUpBall(Vector3f location) {
        CollisionShape shape = findShape("ball");
        float mass = 0.2f;
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        body.setDebugMeshNormals(DebugMeshNormals.Sphere);
        body.setPhysicsLocation(location);

        setUpTarget(body);
    }

    /**
     * Set up a single bowling pin as a target.
     *
     * @param location the desired location (in physics-space coordinates, not
     * null, unaffected)
     */
    private void setUpBowlingPin(Vector3f location) {
        CollisionShape shape = findShape("bowlingPin");
        float mass = 0.2f;
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        body.setDebugMeshNormals(DebugMeshNormals.Smooth);
        body.setPhysicsLocation(location);

        Quaternion rotation = new Quaternion();
        rotation.fromAngleNormalAxis(FastMath.HALF_PI, Vector3f.UNIT_X);
        body.setPhysicsRotation(rotation);

        setUpTarget(body);
    }

    /**
     * Set up a single brick as a target.
     *
     * @param shapeName
     * @param height half the desired height (in world units, &gt;0)
     * @param length half the desired length (in world units, &gt;0)
     */
    private void setUpBrick(String shapeName, Vector3f location,
            Quaternion orientation) {
        CollisionShape shape = findShape(shapeName);

        float mass = 3f;
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        body.setDebugMeshNormals(DebugMeshNormals.Facet);
        body.setPhysicsLocation(location);
        body.setPhysicsRotation(orientation);

        setUpTarget(body);
    }

    /**
     * Set up a round tower of bricks.
     */
    private void setUpBrickTower(int numRings, int numBricksPerRing,
            float thickness) {
        float innerDiameter = 32f - 2f * thickness;
        float innerCircumference = FastMath.PI * innerDiameter;
        float insideSpacing = innerCircumference / numBricksPerRing;
        float insideGap = 0.05f * insideSpacing;
        float length = insideSpacing - insideGap;
        float height = Math.min(length, thickness) / MyMath.phi;
        registerBrickShape("tower", height, length, thickness);

        float angleStep = FastMath.TWO_PI / numBricksPerRing;
        float midRadius = (innerDiameter + thickness) / 2f;
        float y0 = platformTopY + height / 2f;
        Quaternion orientation = new Quaternion();
        Vector3f location = new Vector3f(0f, y0, midRadius);
        for (int ringIndex = 0; ringIndex < numRings; ++ringIndex) {
            float theta0;
            if (MyMath.isOdd(ringIndex)) {
                theta0 = angleStep / 2f;
            } else {
                theta0 = 0f;
            }
            for (int j = 0; j < numBricksPerRing; ++j) {
                float theta = theta0 + j * angleStep;
                location.x = midRadius * FastMath.sin(theta);
                location.z = midRadius * FastMath.cos(theta);
                orientation.fromAngleNormalAxis(theta, Vector3f.UNIT_Y);
                setUpBrick("tower", location, orientation);
            }
            location.y += height;
        }
    }

    /**
     * Erect a brick wall along the X axis.
     */
    private void setUpBrickWall(int numRows, int numBricksPerRow) {
        float xSpacing = 32f / numBricksPerRow; // center-to-center
        float xGap = 0.1f * xSpacing;
        float length = xSpacing - xGap;
        float shortLength = (length - xGap) / 2f;
        float thickness = length / MyMath.phi;
        float height = thickness / MyMath.phi;
        registerBrickShape("short", height, shortLength, thickness);
        registerBrickShape("long", height, length, thickness);

        float x0even = -xSpacing * (numBricksPerRow - 1) / 2f;
        float endSpacing = xGap + (length + shortLength) / 2f;
        float x1odd = x0even + xSpacing / 2f - endSpacing;
        float y0 = platformTopY + height / 2f;
        float ySpacing = 1f * height; // center-to-center
        Vector3f location = new Vector3f(x0even, y0, 0f);
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            if (MyMath.isOdd(rowIndex)) {
                location.x = x1odd;
                setUpBrick("short", location, Quaternion.IDENTITY);
                location.x += endSpacing;

                for (int j = 1; j < numBricksPerRow; ++j) {
                    setUpBrick("long", location, Quaternion.IDENTITY);
                    location.x += xSpacing;
                }

                location.x += endSpacing - xSpacing;
                setUpBrick("short", location, Quaternion.IDENTITY);

            } else {
                location.x = x0even;
                for (int j = 0; j < numBricksPerRow; ++j) {
                    setUpBrick("long", location, Quaternion.IDENTITY);
                    location.x += xSpacing;
                }
            }
            location.y += ySpacing;
        }
    }

    /**
     * Set up a single can as a target.
     *
     * @param location the desired location (in physics-space coordinates, not
     * null, unaffected)
     */
    private void setUpCan(Vector3f location) {
        CollisionShape shape = findShape("can");
        float mass = 10f;
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        body.setDebugMeshNormals(DebugMeshNormals.Smooth);
        body.setPhysicsLocation(location);

        setUpTarget(body);
    }

    private void setUpCanPyramid(int numRows) {
        float xSpacing = 32f / numRows; // center-to-center
        float xGap = 0.1f * xSpacing;
        float radius = (xSpacing - xGap) / 2f;
        float height = 2.65f * radius;
        registerCanShape(radius, height);

        float ySpacing = 1f * height; // center-to-center
        float y0 = platformTopY + height / 2f;
        Vector3f location = new Vector3f(0, y0, 0f);
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            int numCansInRow = numRows - rowIndex;
            location.x = -(numCansInRow - 1) * xSpacing / 2f;
            for (int j = 0; j < numCansInRow; ++j) {
                setUpCan(location);
                location.x += xSpacing;
            }
            location.y += ySpacing;
        }
    }

    /**
     * Set up a single domino as a target.
     *
     * @param length the desired length (in world units, &gt;0)
     * @param location the desired location (in physics-space coordinates, not
     * null, unaffected)
     * @param orientation the desired orientation (in physics-space coordinates,
     * not null, unaffected)
     */
    private void setUpDomino(Vector3f location, Quaternion orientation) {
        CollisionShape shape = findShape("domino");

        float mass = 10f;
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        body.setDebugMeshNormals(DebugMeshNormals.Facet);
        body.setPhysicsLocation(location);
        body.setPhysicsRotation(orientation);

        setUpTarget(body);
    }

    /**
     * Set up a row of dominos, evenly-spaced along the X axis.
     */
    private void setUpDominoRow(int numDominos) {
        float xSpacing = 32f / numDominos; // center-to-center
        float length = 1.6f * xSpacing;
        registerDominoShape(length);

        float x0 = -xSpacing * (numDominos - 1) / 2f;
        float y = platformTopY + length / 2;
        Vector3f location = new Vector3f(x0, y, 0f);
        for (int j = 0; j < numDominos; ++j) {
            setUpDomino(location, Quaternion.IDENTITY);
            location.x += xSpacing;
        }
    }

    /**
     * Set up 15 balls in a wedge, as if for a game of Pool.
     */
    private void setUpPool() {
        int numRows = 5;
        float xSpacing = 32f / numRows; // center-to-center
        float radius = 0.48f * xSpacing;
        registerBallShape(radius);

        float zSpacing = xSpacing / FastMath.sqrt(1.5f); // center-to-center
        float z0 = (numRows - 1) * zSpacing / 2f;
        Vector3f location = new Vector3f(0, radius, z0);
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            int numBallsInRow = rowIndex + 1;
            location.x = -(numBallsInRow - 1) * xSpacing / 2f;
            for (int j = 0; j < numBallsInRow; ++j) {
                setUpBall(location);
                location.x += xSpacing;
            }
            location.z -= zSpacing;
        }
    }

    /**
     * Set up all targets for selected scenario.
     */
    private void setUpScenario() {
        String scenarioName = status.scenarioName();
        switch (scenarioName) {
            case "brick tower": {
                int numRings = 10;
                int numBricksPerRing = 16;
                float thickness = 2f;
                setUpBrickTower(numRings, numBricksPerRing, thickness);
                break;
            }

            case "brick wall": {
                int numRows = 15;
                int numBricksPerRow = 10;
                setUpBrickWall(numRows, numBricksPerRow);
                break;
            }

            case "can pyramid": {
                int numRows = 15;
                setUpCanPyramid(numRows);
                break;
            }

            case "domino row": {
                int numDominos = 25;
                setUpDominoRow(numDominos);
                break;
            }

            case "empty":
                break;

            case "pool":
                setUpPool();
                break;

            case "tenpin":
                setUpTenpin();
                break;

            default:
                String msg = "scenarioName = " + MyString.quote(scenarioName);
                throw new RuntimeException(msg);
        }
    }

    /**
     * Set up a single target body whose position and debug normals have already
     * been configured.
     *
     * @param body the body to set up (not null, not in world)
     */
    private void setUpTarget(PhysicsRigidBody body) {
        assert body != null;
        assert !body.isInWorld();

        ShapeGenerator random = getGenerator();
        String materialName = "target" + random.nextInt(numTargetColors);
        Material debugMaterial = findMaterial(materialName);
        body.setApplicationData(debugMaterial);

        addCollisionObject(body);
    }

    /**
     * Set up 10 bowling pins in a wedge, as if for a game of (ten-pin) bowling.
     */
    private void setUpTenpin() {
        int numRows = 4;
        float xSpacing = 32f / numRows; // center-to-center
        float radius = 0.2f * xSpacing;
        registerBowlingPinShape(radius);

        float y0 = 2.50f * radius;
        float zSpacing = xSpacing / FastMath.sqrt(1.5f); // center-to-center
        float z0 = (numRows - 1) * zSpacing / 2f;
        Vector3f location = new Vector3f(0, y0, z0);
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            int numPinsInRow = rowIndex + 1;
            location.x = -(numPinsInRow - 1) * xSpacing / 2f;
            for (int j = 0; j < numPinsInRow; ++j) {
                setUpBowlingPin(location);
                location.x += xSpacing;
            }
            location.z -= zSpacing;
        }
    }
}
