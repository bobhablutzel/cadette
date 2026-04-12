package com.jigger.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central store for all joints in the scene.
 */
public class JointRegistry {

    private final Map<String, Joint> joints = new LinkedHashMap<>();

    public void addJoint(Joint joint) {
        joints.put(joint.getId(), joint);
    }

    public void removeJoint(String jointId) {
        joints.remove(jointId);
    }

    public Joint getJoint(String jointId) {
        return joints.get(jointId);
    }

    /** Get all joints where the given part is either receiver or inserted. */
    public List<Joint> getJointsForPart(String partName) {
        return joints.values().stream()
                .filter(j -> j.getReceivingPartName().equals(partName)
                        || j.getInsertedPartName().equals(partName))
                .toList();
    }

    /** Get all joints where both parts belong to the given assembly. */
    public List<Joint> getJointsForAssembly(Assembly assembly) {
        Set<String> partNames = assembly.getParts().stream()
                .map(Part::getName)
                .collect(Collectors.toSet());
        return joints.values().stream()
                .filter(j -> partNames.contains(j.getReceivingPartName())
                        && partNames.contains(j.getInsertedPartName()))
                .toList();
    }

    public List<Joint> getAllJoints() {
        return List.copyOf(joints.values());
    }

    /** Remove all joints involving the given part. Returns the removed joints. */
    public List<Joint> removeJointsForPart(String partName) {
        List<Joint> removed = getJointsForPart(partName);
        for (Joint j : removed) {
            joints.remove(j.getId());
        }
        return removed;
    }

    /** Summary counts by joint type. */
    public Map<JointType, Long> getSummary() {
        return joints.values().stream()
                .collect(Collectors.groupingBy(Joint::getType, Collectors.counting()));
    }

    public void clear() {
        joints.clear();
    }

    public boolean isEmpty() {
        return joints.isEmpty();
    }
}
