package com.altbionics.GripTool.util;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;


public class Grip {
    String name;
    String description;
    int[] values;
    boolean uploaded;

    public Grip(String args) {
        String[] tempGripVariables = args.split("::");
        String[] tempGripValues = tempGripVariables[1].split(",");
        values = new int[5];
        this.name = tempGripVariables[0];
        this.description = tempGripVariables[2];
        values[0] = Integer.parseInt(tempGripValues[0]);
        values[1] = Integer.parseInt(tempGripValues[1]);
        values[2] = Integer.parseInt(tempGripValues[2]);
        values[3] = Integer.parseInt(tempGripValues[3]);
        values[4] = Integer.parseInt(tempGripValues[4]);
        uploaded = Boolean.parseBoolean(tempGripVariables[3]);
    }

    @NonNull
    @NotNull
    @Override
    public String toString() {
        String tempValues = "";
        tempValues = tempValues.concat(values[0] + "," + values[1]
                + "," + values[2] + "," + values[3] + "," + values[4]);
        return name + "::" + tempValues + "::" + description + "::" + uploaded;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumb(int thumb) {
        values[0] = thumb;
    }

    public void setIndex(int index) {
        values[1] = index;
    }

    public void setMiddle(int middle) {
        values[2] = middle;
    }

    public void setRing(int ring) {
        values[3] = ring;
    }

    public void setPinky(int pinky) {
        values[4] = pinky;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /*public String getValues() {
        String tempValues = "";
        tempValues = tempValues.concat(values[0] + "," + values[1]
                + "," + values[2] + "," + values[3] + "," + values[4]);
        return tempValues;
    }*/

    public int getThumb() {
        return values[0];
    }

    public int getIndex() {
        return values[1];
    }

    public int getMiddle() {
        return values[2];
    }

    public int getRing() {
        return values[3];
    }

    public int getPinky() {
        return values[4];
    }

    public void setUploaded(boolean setUploaded) {
        uploaded = setUploaded;
    }

    public boolean getUploaded() {
        return uploaded;
    }
}
