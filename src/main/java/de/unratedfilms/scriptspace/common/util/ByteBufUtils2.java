
package de.unratedfilms.scriptspace.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class ByteBufUtils2 {

    public static String[] readStringArray(ByteBuf from) {

        String[] arr = new String[from.readInt()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = ByteBufUtils.readUTF8String(from);
        }
        return arr;
    }

    public static void writeStringArray(ByteBuf to, String[] arr) {

        to.writeInt(arr.length);
        for (String s : arr) {
            ByteBufUtils.writeUTF8String(to, s);
        }
    }

    public static Vec3i readVec3i(ByteBuf from) {

        return new Vec3i(from.readInt(), from.readInt(), from.readInt());
    }

    public static void writeVec3i(ByteBuf to, Vec3i vec3i) {

        writeVec3i(to, vec3i.x, vec3i.y, vec3i.z);
    }

    public static void writeVec3i(ByteBuf to, int x, int y, int z) {

        to.writeInt(x);
        to.writeInt(y);
        to.writeInt(z);
    }

    public static ItemStack readItemStack(ByteBuf from) {

        ItemStack stack = ByteBufUtils.readItemStack(from);
        return stack == null ? new ItemStack((Item) null) : stack;
    }

    public static void writeItemStack(ByteBuf to, ItemStack stack) {

        ByteBufUtils.writeItemStack(to, stack.getItem() == null ? null : stack);
    }

    private ByteBufUtils2() {

    }

}
