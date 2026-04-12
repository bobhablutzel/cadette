package com.jigger;

import com.jigger.command.CommandExecutor;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for headless jME3 tests. Starts the engine without a display
 * so we can create objects and inspect their scene-graph positions.
 */
public abstract class HeadlessTestBase {

    protected static SceneManager sceneManager;
    protected static CommandExecutor executor;

    @BeforeAll
    static void startEngine() throws Exception {
        AppSettings settings = new AppSettings(true);
        settings.setRenderer(null);  // headless — no GPU
        settings.setAudioRenderer(null);

        sceneManager = new SceneManager();
        sceneManager.setSettings(settings);
        sceneManager.setPauseOnLostFocus(false);

        // Start in headless mode
        sceneManager.start(JmeContext.Type.Headless);

        // Wait for initialization
        CountDownLatch ready = new CountDownLatch(1);
        sceneManager.enqueue(() -> { ready.countDown(); return null; });
        assertTrue(ready.await(10, TimeUnit.SECONDS), "Engine failed to start");

        executor = new CommandExecutor(sceneManager);
    }

    @AfterAll
    static void stopEngine() {
        if (sceneManager != null) {
            sceneManager.stop();
        }
    }

    /** Reset scene to a clean state for test isolation. */
    protected void resetScene() {
        exec("delete all");
        flushQueue();
        exec("set units mm");
        executor.clearUndoHistory();
        flushQueue();
    }

    /** Execute a command and return the result. */
    protected String exec(String command) {
        String result = executor.execute(command);
        // Flush the render queue so scene graph updates are applied
        flushQueue();
        return result;
    }

    /** Wait for all enqueued jME3 tasks to complete and update the scene graph. */
    protected void flushQueue() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            sceneManager.enqueue(() -> {
                // Force scene graph update so world bounds are current
                sceneManager.getRootNode().updateGeometricState();
                latch.countDown();
                return null;
            });
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Get world bounding box [min, max] for an object. */
    protected Vector3f[] bounds(String name) {
        return sceneManager.getWorldBounds(name);
    }

    /** Assert a part's bounding box min corner is approximately at the expected position. */
    protected void assertBoundsMin(String name, float x, float y, float z, float tolerance) {
        Vector3f[] b = bounds(name);
        assertNotNull(b, "Object '" + name + "' not found or has no bounds");
        assertEquals(x, b[0].x, tolerance, name + " bounds min X");
        assertEquals(y, b[0].y, tolerance, name + " bounds min Y");
        assertEquals(z, b[0].z, tolerance, name + " bounds min Z");
    }

    /** Assert a part's bounding box max corner is approximately at the expected position. */
    protected void assertBoundsMax(String name, float x, float y, float z, float tolerance) {
        Vector3f[] b = bounds(name);
        assertNotNull(b, "Object '" + name + "' not found or has no bounds");
        assertEquals(x, b[1].x, tolerance, name + " bounds max X");
        assertEquals(y, b[1].y, tolerance, name + " bounds max Y");
        assertEquals(z, b[1].z, tolerance, name + " bounds max Z");
    }

    /** Assert a part occupies the expected box in world space. */
    protected void assertBounds(String name,
                                float minX, float minY, float minZ,
                                float maxX, float maxY, float maxZ,
                                float tolerance) {
        assertBoundsMin(name, minX, minY, minZ, tolerance);
        assertBoundsMax(name, maxX, maxY, maxZ, tolerance);
    }

    /** Print debug info for a part (useful during test development). */
    protected void debugPart(String name) {
        Vector3f[] b = bounds(name);
        SceneManager.ObjectRecord rec = sceneManager.getObjectRecord(name);
        Vector3f rot = sceneManager.getRotation(name);
        if (b != null && rec != null) {
            System.out.printf("  %-25s pos=(%7.1f,%7.1f,%7.1f) rot=(%5.1f,%5.1f,%5.1f) bounds=[(%7.1f,%7.1f,%7.1f) → (%7.1f,%7.1f,%7.1f)]%n",
                    name, rec.position().x, rec.position().y, rec.position().z,
                    rot.x, rot.y, rot.z,
                    b[0].x, b[0].y, b[0].z, b[1].x, b[1].y, b[1].z);
        } else {
            System.out.println("  " + name + " — NOT FOUND");
        }
    }
}
