package com.sushiy.tektopiaaddons;

public final class OreDictStack {
    private final String oreName;
    private int stackSize;

    public OreDictStack(String oreName) {
        this(oreName, 1);
    }

    public OreDictStack(String oreName, int stackSize) {
        this.oreName = oreName;
        this.stackSize = stackSize;
    }

    public String getOreName() {
        return oreName;
    }

    public int getCount() {
        return stackSize;
    }
}
