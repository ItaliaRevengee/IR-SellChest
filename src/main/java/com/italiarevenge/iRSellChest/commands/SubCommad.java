/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.commands;

import java.util.List;

public interface SubCommad {
    public String getName();

    public String getDescription();

    public String getSyntax();

    public void perform(Object var1, String[] var2);

    public List<String> getTabCompletion(String[] var1);
}

