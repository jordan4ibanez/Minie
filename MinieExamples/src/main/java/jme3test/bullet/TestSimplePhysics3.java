/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3test.bullet;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.DebugShapeFactory;
import com.jme3.export.Savable;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import jme3utilities.math.noise.Generator;

public class TestSimplePhysics3
        extends SimpleApplication
        implements BulletDebugAppState.DebugAppStateFilter {

    private boolean insPressed = false;
    private BulletAppState bulletAppState;
    final private Generator random = new Generator();
    private PhysicsRigidBody boxBody;

    public static void main(String[] args) {
        TestSimplePhysics3 app = new TestSimplePhysics3();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        configurePhysics();

        // the floor box, does not move (mass=0)
        Node node3 = PhysicsTestHelper.createPhysicsTestNode(assetManager, new BoxCollisionShape(new Vector3f(40f, 1f, 40f)), 0);
        boxBody = node3.getControl(RigidBodyControl.class);
        boxBody.setPhysicsLocation(new Vector3f(0f, -6f, 0f));
        rootNode.attachChild(node3);
        getPhysicsSpace().add(node3);

        // Configure InputManager to respond to the the Insert key.
        inputManager.addMapping("insert", new KeyTrigger(KeyInput.KEY_INSERT));
        ActionListener actionListener = new ActionListener() {
            @Override
            public void onAction(String actionString, boolean ongoing, float tpf) {
                if (actionString.equals("insert")) {
                    insPressed = ongoing;
                }
            }
        };
        inputManager.addListener(actionListener, "insert");
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        if (insPressed) {
            addAGem();
        }
    }

    /**
     * Test whether the specified physics object should be displayed in the
     * debug scene.
     *
     * @param object the joint or collision object to test (unaffected)
     * @return return true if the object should be displayed, false if not
     */
    @Override
    public boolean displayObject(Savable object) {
        return object != boxBody;
    }

    /**
     * Add a dynamic rigid body to the scene.
     */
    private void addAGem() {
        int numVertices = 5 + random.nextInt(16);
        List<Vector3f> vertices = new ArrayList<>(numVertices);

        vertices.add(new Vector3f(0f, 0f, 0f));
        for (int vertexIndex = 1; vertexIndex < numVertices; ++vertexIndex) {
            Vector3f center = random.nextUnitVector3f();
            center.multLocal(0.6f);
            vertices.add(center);
        }
        CollisionShape shape = new HullCollisionShape(vertices);

        Vector3f startLocation = random.nextVector3f();
        startLocation.multLocal(0.5f, 1f, 0.5f);
        startLocation.y += 4f;

        float mass = 1f;
        PhysicsRigidBody body = new PhysicsRigidBody(shape, mass);
        body.setDamping(0.6f, 0.6f);
        body.setDebugMeshResolution(DebugShapeFactory.highResolution);
        body.setFriction(1f);
        body.setKinematic(false);
        body.setPhysicsLocation(startLocation);

        getPhysicsSpace().add(body);
        body.setGravity(new Vector3f(0f, -9f, 0f));
    }

    /**
     * Configure physics during startup.
     */
    private void configurePhysics() {
        CollisionShape.setDefaultMargin(0.005f); // 5 mm margin

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(true);
        bulletAppState.setDebugFilter(this);
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
}
