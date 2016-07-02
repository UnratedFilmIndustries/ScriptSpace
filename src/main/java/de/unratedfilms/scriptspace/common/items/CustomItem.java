
package de.unratedfilms.scriptspace.common.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.unratedfilms.scriptspace.common.Consts;

public class CustomItem extends Item {

    @Override
    public Item setUnlocalizedName(String unlocalizedName) {

        return super.setUnlocalizedName(Consts.MOD_ID + "." + unlocalizedName);
    }

    @Override
    @SuppressWarnings ({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {

        lines.add(I18n.format(getUnlocalizedName() + ".desc"));
    }

    @Override
    @SideOnly (Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {

        itemIcon = iconRegister.registerIcon(Consts.MOD_ID + ":" + StringUtils.substringAfter(getUnlocalizedName(), "."));
    }

}
