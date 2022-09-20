/*
 * Copyright (c) 2019-2022 jMonkeyEngine
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
package com.jme3.bullet.collision.shapes.infos;

import com.jme3.bullet.NativePhysicsObject;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A scalable mesh that combines multiple indexed meshes. Based on Bullet's
 * {@code btTriangleIndexVertexArray}.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class CompoundMesh
        extends NativePhysicsObject
        implements JmeCloneable, Savable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(CompoundMesh.class.getName());
    /**
     * field names for serialization
     */
    final private static String tagScale = "scale";
    final private static String tagSubmeshes = "submeshes";
    // *************************************************************************
    // fields

    /**
     * component meshes
     */
    private ArrayList<IndexedMesh> submeshes = new ArrayList<>(4);
    /**
     * copy of scale factors: one for each local axis
     */
    private Vector3f scale = new Vector3f(1f, 1f, 1f);
    // *************************************************************************
    // constructors

    /**
     * Instantiate an empty mesh.
     */
    public CompoundMesh() {
        createEmpty();
    }

    /**
     * Instantiate a mesh based on the specified JME mesh(es).
     *
     * @param jmeMeshes the JME mesh(es) (all non-null,
     * modes=Triangles/TriangleStrip/TriangleFan, unaffected)
     */
    public CompoundMesh(Mesh... jmeMeshes) {
        createEmpty();

        for (Mesh jmeMesh : jmeMeshes) {
            IndexedMesh indexedMesh = new IndexedMesh(jmeMesh);
            add(indexedMesh);
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a submesh to this mesh.
     *
     * @param submesh (not null, alias created)
     */
    final public void add(IndexedMesh submesh) {
        Validate.nonNull(submesh, "submesh");

        submeshes.add(submesh);

        long compoundMeshId = nativeId();
        long submeshId = submesh.nativeId();
        addIndexedMesh(compoundMeshId, submeshId);
    }

    /**
     * Count how many triangles are in this mesh.
     *
     * @return the count (&ge;0)
     */
    public int countTriangles() {
        int numTriangles = 0;
        for (IndexedMesh submesh : submeshes) {
            numTriangles += submesh.countTriangles();
        }

        assert numTriangles >= 0 : numTriangles;
        return numTriangles;
    }

    /**
     * Count how many vertices are in this mesh.
     *
     * @return the count (&ge;0)
     */
    public int countVertices() {
        int numVertices = 0;
        for (IndexedMesh submesh : submeshes) {
            numVertices += submesh.countVertices();
        }

        assert numVertices >= 0 : numVertices;
        return numVertices;
    }

    /**
     * Copy the scale factors.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the scale factor for each local axis (either storeResult or a new
     * vector, not null)
     */
    public Vector3f getScale(Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        assert checkScale(result);
        result.set(scale);

        return result;
    }

    /**
     * Alter the scale factors.
     *
     * @param scale the desired scale factor for each local axis (not null, no
     * negative component, unaffected, default=(1,1,1))
     */
    public void setScale(Vector3f scale) {
        long compoundMeshId = nativeId();
        setScaling(compoundMeshId, scale.x, scale.y, scale.z);
        logger.log(Level.FINE, "Scaled {0}", this);
        this.scale.set(scale);
    }
    // *************************************************************************
    // JmeCloneable methods

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned mesh into a deep-cloned one, using the specified Cloner
     * and original to resolve copied fields.
     *
     * @param cloner the Cloner that's cloning this mesh (not null)
     * @param original the instance from which this mesh was shallow-cloned (not
     * null, unaffected)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        submeshes = cloner.clone(submeshes);
        scale = cloner.clone(scale);

        unassignNativeObject();
        createEmpty();
        setScale(scale);

        long compoundMeshId = nativeId();
        for (IndexedMesh submesh : submeshes) {
            long submeshId = submesh.nativeId();
            addIndexedMesh(compoundMeshId, submeshId);
        }
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new instance
     */
    @Override
    public CompoundMesh jmeClone() {
        try {
            CompoundMesh clone = (CompoundMesh) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }
    // *************************************************************************
    // Savable methods

    /**
     * De-serialize this mesh from the specified importer, for example when
     * loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from the importer
     */
    @Override
    @SuppressWarnings("unchecked")
    public void read(JmeImporter importer) throws IOException {
        InputCapsule capsule = importer.getCapsule(this);

        scale = (Vector3f) capsule.readSavable(
                tagScale, new Vector3f(1f, 1f, 1f));
        submeshes = capsule.readSavableArrayList(tagSubmeshes, submeshes);

        setScale(scale);

        long compoundMeshId = nativeId();
        for (IndexedMesh submesh : submeshes) {
            long submeshId = submesh.nativeId();
            addIndexedMesh(compoundMeshId, submeshId);
        }
    }

    /**
     * Serialize this mesh to the specified exporter, for example when saving to
     * a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from the exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        OutputCapsule capsule = exporter.getCapsule(this);

        capsule.write(scale, tagScale, null);
        capsule.writeSavableArrayList(submeshes, tagSubmeshes, null);
    }
    // *************************************************************************
    // Java private methods

    /**
     * Compare Bullet's scale factors to the local copy.
     *
     * @param tempVector caller-allocated temporary storage (not null)
     * @return true if scale factors are exactly equal, otherwise false
     */
    private boolean checkScale(Vector3f tempVector) {
        assert tempVector != null;

        long compoundMeshId = nativeId();
        getScaling(compoundMeshId, tempVector);
        boolean result = scale.equals(tempVector);

        return result;
    }

    /**
     * Create an empty {@code btTriangleIndexVertexArray}.
     */
    private void createEmpty() {
        long compoundMeshId = createEmptyTiva();
        setNativeId(compoundMeshId);

        logger.log(Level.FINE, "Created {0}", this);
    }

    /**
     * Free the identified tracked native object. Invoked by reflection.
     *
     * @param compoundMeshId the native identifier (not zero)
     */
    private static void freeNativeObject(long compoundMeshId) {
        assert compoundMeshId != 0L;
        finalizeNative(compoundMeshId);
    }
    // *************************************************************************
    // native private methods

    native private static void
            addIndexedMesh(long compoundMeshId, long submeshId);

    native private static long createEmptyTiva();

    native private static void finalizeNative(long compoundMeshId);

    native private static void
            getScaling(long compoundMeshId, Vector3f storeVector);

    native private static void setScaling(
            long compoundMeshId, float xScale, float yScale, float zScale);
}
