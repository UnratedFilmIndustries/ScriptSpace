
package de.unratedfilms.scriptspace.common.script.api.wrapper.entity;

import net.minecraft.entity.DataWatcher;
import de.unratedfilms.scriptspace.common.script.api.wrapper.world.ScriptItemStack;

public class ScriptDataWatcher {

    public final DataWatcher watcher;

    public ScriptDataWatcher(DataWatcher watcher) {

        this.watcher = watcher;
    }

    public void addObject(int id, Object obj) {

        watcher.addObject(id, obj);
    }

    public void addObjectByDataType(int id, int type) {

        watcher.addObjectByDataType(id, type);
    }

    public byte getWatchableObjectByte(int id) {

        return watcher.getWatchableObjectByte(id);
    }

    public short getWatchableObjectShort(int id) {

        return watcher.getWatchableObjectShort(id);
    }

    public int getWatchableObjectInt(int id) {

        return watcher.getWatchableObjectInt(id);
    }

    public float getWatchableObjectFloat(int id) {

        return watcher.getWatchableObjectFloat(id);
    }

    public String getWatchableObjectString(int id) {

        return watcher.getWatchableObjectString(id);
    }

    public ScriptItemStack getWatchableObjectItemStack(int id) {

        return new ScriptItemStack(watcher.getWatchableObjectItemStack(id));
    }

    public void updateObject(int id, Object obj) {

        watcher.updateObject(id, obj);
    }

    public void setObjectWatched(int id) {

        watcher.setObjectWatched(id);
    }

}
