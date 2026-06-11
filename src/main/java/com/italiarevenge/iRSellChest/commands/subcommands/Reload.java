/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.commands.subcommands;

import java.util.List;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.commands.SubCommad;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.Logger;

public class Reload
implements SubCommad {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the plugin";
    }

    @Override
    public String getSyntax() {
        return "/asc reload";
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length >= 1) {
            long start = System.currentTimeMillis();
            Config.reload();
            Lang.reload();
            AutoSellChests.getInstance().reloadManager();
            Logger.sendMessage(logger, "Reloaded successful, took " + (System.currentTimeMillis() - start) + "ms to complete");
        }
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        return null;
    }
}

