/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.util.exceptions;

public class UpgradeLoadException
extends Exception {
    public final Exception e;

    public UpgradeLoadException(String reason, Exception e) {
        super(reason);
        this.e = e;
    }
}

