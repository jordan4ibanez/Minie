/*
 * Copyright (c) 2009-2018 jMonkeyEngine
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
package com.jme3.bullet.debug;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import java.util.logging.Logger;

/**
 * Debugging aids. This class exists solely for compatibility with the
 * jme3-bullet library.
 *
 * @author normenhansen
 */
public class DebugTools {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DebugTools.class.getName());
    /**
     * local copy of {@link com.jme3.math.Vector3f#UNIT_XYZ}
     */
    final protected static Vector3f UNIT_XYZ_CHECK = new Vector3f(1f, 1f, 1f);
    /**
     * local copy of {@link com.jme3.math.Vector3f#UNIT_X}
     */
    final protected static Vector3f UNIT_X_CHECK = new Vector3f(1f, 0f, 0f);
    /**
     * local copy of {@link com.jme3.math.Vector3f#UNIT_Y}
     */
    final protected static Vector3f UNIT_Y_CHECK = new Vector3f(0f, 1f, 0f);
    /**
     * local copy of {@link com.jme3.math.Vector3f#UNIT_Z}
     */
    final protected static Vector3f UNIT_Z_CHECK = new Vector3f(0f, 0f, 1f);
    /**
     * local copy of {@link com.jme3.math.Vector3f#ZERO}
     */
    final protected static Vector3f ZERO_CHECK = new Vector3f(0f, 0f, 0f);
    // *************************************************************************
    // fields

    /**
     * mesh for the blue arrow
     */
    public Arrow arrowBlue = new Arrow(Vector3f.ZERO);
    /**
     * mesh for the green arrow
     */
    public Arrow arrowGreen = new Arrow(Vector3f.ZERO);
    /**
     * mesh for the magenta arrow
     */
    public Arrow arrowMagenta = new Arrow(Vector3f.ZERO);
    /**
     * mesh for the pink arrow
     */
    public Arrow arrowPink = new Arrow(Vector3f.ZERO);
    /**
     * mesh for the red arrow
     */
    public Arrow arrowRed = new Arrow(Vector3f.ZERO);
    /**
     * mesh for the yellow arrow
     */
    public Arrow arrowYellow = new Arrow(Vector3f.ZERO);
    /**
     * asset manager
     */
    final protected AssetManager manager;
    /**
     * geometry for the blue arrow
     */
    public Geometry arrowBlueGeom = new Geometry("Blue Arrow", arrowBlue);
    /**
     * geometry for the green arrow
     */
    public Geometry arrowGreenGeom = new Geometry("Green Arrow", arrowGreen);
    /**
     * geometry for the magenta arrow
     */
    public Geometry arrowMagentaGeom = new Geometry("Magenta Arrow",
            arrowMagenta);
    /**
     * geometry for the pink arrow
     */
    public Geometry arrowPinkGeom = new Geometry("Pink Arrow", arrowPink);
    /**
     * geometry for the red arrow
     */
    public Geometry arrowRedGeom = new Geometry("Red Arrow", arrowRed);
    /**
     * geometry for the yellow arrow
     */
    public Geometry arrowYellowGeom = new Geometry("Yellow Arrow", arrowYellow);
    /**
     * unshaded blue material
     */
    public Material DEBUG_BLUE;
    /**
     * unshaded green material
     */
    public Material DEBUG_GREEN;
    /**
     * unshaded magenta material
     */
    public Material DEBUG_MAGENTA;
    /**
     * unshaded pink material
     */
    public Material DEBUG_PINK;
    /**
     * unshaded red material
     */
    public Material DEBUG_RED;
    /**
     * unshaded yellow material
     */
    public Material DEBUG_YELLOW;
    /**
     * node for attaching debug geometries
     */
    public Node debugNode = new Node("Debug Node");
    // *************************************************************************
    // constructors

    /**
     * Instantiate a set of debug tools.
     *
     * @param manager for loading assets (not null, alias created)
     */
    public DebugTools(AssetManager manager) {
        this.manager = manager;
        setupMaterials();
        setupDebugNode();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Alter the location and extent of the blue arrow.
     *
     * @param location the coordinates of the tail (not null, unaffected)
     * @param extent the offset of the tip from the tail (not null, unaffected)
     */
    public void setBlueArrow(Vector3f location, Vector3f extent) {
        arrowBlueGeom.setLocalTranslation(location);
        arrowBlue.setArrowExtent(extent);
    }

    /**
     * Alter the location and extent of the green arrow.
     *
     * @param location the coordinates of the tail (not null, unaffected)
     * @param extent the offset of the tip from the tail (not null, unaffected)
     */
    public void setGreenArrow(Vector3f location, Vector3f extent) {
        arrowGreenGeom.setLocalTranslation(location);
        arrowGreen.setArrowExtent(extent);
    }

    /**
     * Alter the location and extent of the magenta arrow.
     *
     * @param location the coordinates of the tail (not null, unaffected)
     * @param extent the offset of the tip from the tail (not null, unaffected)
     */
    public void setMagentaArrow(Vector3f location, Vector3f extent) {
        arrowMagentaGeom.setLocalTranslation(location);
        arrowMagenta.setArrowExtent(extent);
    }

    /**
     * Alter the location and extent of the pink arrow.
     *
     * @param location the coordinates of the tail (not null, unaffected)
     * @param extent the offset of the tip from the tail (not null, unaffected)
     */
    public void setPinkArrow(Vector3f location, Vector3f extent) {
        arrowPinkGeom.setLocalTranslation(location);
        arrowPink.setArrowExtent(extent);
    }

    /**
     * Alter the location and extent of the red arrow.
     *
     * @param location the coordinates of the tail (not null, unaffected)
     * @param extent the offset of the tip from the tail (not null, unaffected)
     */
    public void setRedArrow(Vector3f location, Vector3f extent) {
        arrowRedGeom.setLocalTranslation(location);
        arrowRed.setArrowExtent(extent);
    }

    /**
     * Alter the location and extent of the yellow arrow.
     *
     * @param location the coordinates of the tail (not null, unaffected)
     * @param extent the offset of the tip from the tail (not null, unaffected)
     */
    public void setYellowArrow(Vector3f location, Vector3f extent) {
        arrowYellowGeom.setLocalTranslation(location);
        arrowYellow.setArrowExtent(extent);
    }

    /**
     * Render all the debug geometries to the specified view port.
     *
     * @param rm the render manager (not null)
     * @param vp the view port (not null)
     */
    public void show(RenderManager rm, ViewPort vp) {
        if (!Vector3f.UNIT_X.equals(UNIT_X_CHECK)
                || !Vector3f.UNIT_Y.equals(UNIT_Y_CHECK)
                || !Vector3f.UNIT_Z.equals(UNIT_Z_CHECK)
                || !Vector3f.UNIT_XYZ.equals(UNIT_XYZ_CHECK)
                || !Vector3f.ZERO.equals(ZERO_CHECK)) {
            throw new IllegalStateException("Unit vectors compromised!"
                    + "\nX: " + Vector3f.UNIT_X
                    + "\nY: " + Vector3f.UNIT_Y
                    + "\nZ: " + Vector3f.UNIT_Z
                    + "\nXYZ: " + Vector3f.UNIT_XYZ
                    + "\nZERO: " + Vector3f.ZERO);
        }
        debugNode.updateLogicalState(0f);
        debugNode.updateGeometricState();
        rm.renderScene(debugNode, vp);
    }
    // *************************************************************************
    // new protected methods

    /**
     * Attach all the debug geometries to the debug node.
     */
    protected void setupDebugNode() {
        arrowBlueGeom.setMaterial(DEBUG_BLUE);
        arrowGreenGeom.setMaterial(DEBUG_GREEN);
        arrowRedGeom.setMaterial(DEBUG_RED);
        arrowMagentaGeom.setMaterial(DEBUG_MAGENTA);
        arrowYellowGeom.setMaterial(DEBUG_YELLOW);
        arrowPinkGeom.setMaterial(DEBUG_PINK);

        debugNode.attachChild(arrowBlueGeom);
        debugNode.attachChild(arrowGreenGeom);
        debugNode.attachChild(arrowRedGeom);
        debugNode.attachChild(arrowMagentaGeom);
        debugNode.attachChild(arrowYellowGeom);
        debugNode.attachChild(arrowPinkGeom);
    }

    /**
     * Initialize all the DebugTools materials.
     */
    protected void setupMaterials() {
        this.DEBUG_BLUE
                = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_BLUE.getAdditionalRenderState().setWireframe(true);
        DEBUG_BLUE.setColor("Color", ColorRGBA.Blue);

        this.DEBUG_GREEN
                = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_GREEN.getAdditionalRenderState().setWireframe(true);
        DEBUG_GREEN.setColor("Color", ColorRGBA.Green);

        this.DEBUG_RED
                = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_RED.getAdditionalRenderState().setWireframe(true);
        DEBUG_RED.setColor("Color", ColorRGBA.Red);

        this.DEBUG_YELLOW
                = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_YELLOW.getAdditionalRenderState().setWireframe(true);
        DEBUG_YELLOW.setColor("Color", ColorRGBA.Yellow);

        this.DEBUG_MAGENTA
                = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_MAGENTA.getAdditionalRenderState().setWireframe(true);
        DEBUG_MAGENTA.setColor("Color", ColorRGBA.Magenta);

        this.DEBUG_PINK
                = new Material(manager, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_PINK.getAdditionalRenderState().setWireframe(true);
        DEBUG_PINK.setColor("Color", ColorRGBA.Pink);
    }
}
