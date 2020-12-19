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
package jme3utilities.tutorial;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.Materials;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

/**
 * A simple example of a CharacterControl.
 *
 * Builds upon HelloCharacter.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class HelloCharacterControl
        extends SimpleApplication
        implements PhysicsTickListener {
    // *************************************************************************
    // fields

    private CharacterControl characterControl;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the HelloCharacterControl application.
     *
     * @param ignored array of command-line arguments (not null)
     */
    public static void main(String[] ignored) {
        HelloCharacterControl application = new HelloCharacterControl();

        // Enable gamma correction for accurate lighting.
        boolean loadDefaults = true;
        AppSettings settings = new AppSettings(loadDefaults);
        settings.setGammaCorrection(true);
        application.setSettings(settings);

        application.start();
    }
    // *************************************************************************
    // SimpleApplication methods

    /**
     * Initialize this application.
     */
    @Override
    public void simpleInitApp() {
        PhysicsSpace physicsSpace = configurePhysics();

        // Load the Jaime model from jme3-testdata-3.1.0-stable.jar
        Spatial jaime = assetManager.loadModel("Models/Jaime/Jaime.j3o");
        jaime.setShadowMode(RenderQueue.ShadowMode.Cast);
        jaime.move(0f, -1f, 0f);
        jaime.scale(1.4f);

        // Attach Jaime to a new scene-graph node located near its center.
        Node centerNode = new Node("center node");
        centerNode.attachChild(jaime);

        // Attach the center node to the scene.
        rootNode.attachChild(centerNode);

        // Create a CharacterControl with a capsule shape.
        float characterRadius = 0.5f;
        float characterHeight = 1f;
        CapsuleCollisionShape characterShape
                = new CapsuleCollisionShape(characterRadius, characterHeight);
        float stepHeight = 0.3f;
        characterControl = new CharacterControl(characterShape, stepHeight);

        // Add the control to the center node and the PhysicsSpace.
        centerNode.addControl(characterControl);
        characterControl.setPhysicsSpace(physicsSpace);

        // Add a square to represent the ground.
        float halfExtent = 4f;
        float y = -2f;
        addSquare(halfExtent, y, physicsSpace);

        // Add lighting.
        addLighting(rootNode);
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // If the character is touching the ground, cause it to jump.
        if (characterControl.onGround()) {
            characterControl.jump();
        }
    }

    /**
     * Callback from Bullet, invoked just after the physics has been stepped.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }
    // *************************************************************************
    // private methods

    /**
     * Add lighting and shadows to the specified scene.
     */
    private void addLighting(Spatial scene) {
        ColorRGBA ambientColor = new ColorRGBA(0.02f, 0.02f, 0.02f, 1f);
        AmbientLight ambient = new AmbientLight(ambientColor);
        scene.addLight(ambient);
        ambient.setName("ambient");

        ColorRGBA directColor = new ColorRGBA(0.2f, 0.2f, 0.2f, 1f);
        Vector3f direction = new Vector3f(-7f, -3f, -5f).normalizeLocal();
        DirectionalLight sun = new DirectionalLight(direction, directColor);
        scene.addLight(sun);
        sun.setName("sun");

        // Render shadows based on the directional light.
        viewPort.clearProcessors();
        int shadowMapSize = 2_048; // in pixels
        int numSplits = 3;
        DirectionalLightShadowRenderer dlsr
                = new DirectionalLightShadowRenderer(assetManager,
                        shadowMapSize, numSplits);
        dlsr.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        dlsr.setEdgesThickness(5);
        dlsr.setLight(sun);
        dlsr.setShadowIntensity(0.5f);
        viewPort.addProcessor(dlsr);

        // Set the viewport's background color to light blue.
        ColorRGBA skyColor = new ColorRGBA(0.1f, 0.2f, 0.4f, 1f);
        viewPort.setBackgroundColor(skyColor);
    }

    /**
     * Attach a horizontal square to the scene and also to the specified
     * PhysicsSpace.
     *
     * @param halfExtent (half of the desired side length)
     * @param y (the desired elevation, in physics-space coordinates)
     * @param physicsSpace (not null)
     */
    private void addSquare(float halfExtent, float y,
            PhysicsSpace physicsSpace) {
        // Add a Quad to the scene.
        Mesh quad = new Quad(2 * halfExtent, 2 * halfExtent);
        Geometry geometry = new Geometry("square", quad);
        Material material = createGrassyMaterial();
        geometry.setMaterial(material);
        geometry.setShadowMode(RenderQueue.ShadowMode.Receive);
        rootNode.attachChild(geometry);

        // Rotate it 90 degrees to a horizontal orientation.
        geometry.rotate(-FastMath.HALF_PI, 0f, 0f);

        // Translate it to the desired elevation.
        geometry.move(-halfExtent, y, halfExtent);

        // Add a static RBC to the Geometry, to make it solid.
        RigidBodyControl rbc = new RigidBodyControl(PhysicsBody.massForStatic);
        geometry.addControl(rbc);
        physicsSpace.addCollisionObject(rbc);
    }

    /**
     * Configure physics during startup.
     */
    private PhysicsSpace configurePhysics() {
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        PhysicsSpace result = bulletAppState.getPhysicsSpace();

        // Activate the PhysicsTickListener interface.
        result.addTickListener(this);

        return result;
    }

    /**
     * Generate a grassy lit material.
     */
    private Material createGrassyMaterial() {
        Material result = new Material(assetManager, Materials.LIGHTING);
        result.setBoolean("UseMaterialColors", true);

        result.setColor("Ambient", new ColorRGBA(20f, 20f, 20f, 1f));
        result.setName("grass");

        Texture grassTexture
                = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        result.setTexture("DiffuseMap", grassTexture);

        return result;
    }
}
