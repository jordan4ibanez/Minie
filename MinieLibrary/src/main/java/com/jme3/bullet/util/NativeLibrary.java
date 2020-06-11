/*
 * Copyright (c) 2019-2020 jMonkeyEngine
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
package com.jme3.bullet.util;

import com.jme3.bullet.NativePhysicsObject;

/**
 * Static interface to the Libbulletjme native library.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class NativeLibrary {
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private NativeLibrary() {
    }
    // *************************************************************************
    // native methods exposed

    /**
     * Dump all native-memory allocation/free events to standard output. This
     * feature is enabled only in native libraries built with the
     * BT_DEBUG_MEMORY_ALLOCATIONS macro defined.
     *
     * @return the number of bytes outstanding (&ge;0), or -1 if this feature is
     * not enabled
     */
    native public static int dumpMemoryLeaks();

    /**
     * Test whether the native library was built with debugging enabled.
     *
     * @return true if Debug buildType, false if Release buildType
     */
    native public static boolean isDebug();

    /**
     * Test whether the native library uses double-precision arithmetic.
     *
     * @return true if double-precision, false if single-precision
     */
    native public static boolean isDoublePrecision();

    /**
     * Alter whether the native library will print its startup message during
     * initialization.
     *
     * @param printFlag true &rarr; print message, false &rarr; no message
     * (default=true)
     */
    native public static void setStartupMessageEnabled(boolean printFlag);

    /**
     * Determine the native library's core version number.
     *
     * @return the version number (typically of the form Major.Minor.Patch)
     */
    native public static String versionNumber();
    // *************************************************************************
    // private Java methods

    /**
     * Callback invoked (from native code) upon successful initialization of the
     * native library, to start the Physics Cleaner thread.
     */
    private static void postInitialization() {
        Thread physicsCleaner = new Thread("Physics Cleaner") {
            @Override
            public void run() {
//                NativePhysicsObject.freeUnusedObjects();
            }
        };
        physicsCleaner.setDaemon(true);
//        physicsCleaner.start();
//
//        System.out.println(physicsCleaner.getName() + " thread started.");
//        System.out.flush();
    }
}
