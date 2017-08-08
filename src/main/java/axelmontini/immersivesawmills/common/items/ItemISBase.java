package axelmontini.immersivesawmills.common.items;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import axelmontini.immersivesawmills.common.Config;
import axelmontini.immersivesawmills.common.IFuel;
import axelmontini.immersivesawmills.common.ISContent;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Taken from Immersive Engineering and adapted to fit in this mod.
 */
public class ItemISBase extends Item implements IEItemInterfaces.IColouredItem, IFuel
{
    public String itemName;
    protected String[] subNames;
    boolean[] isMetaHidden;
    public boolean registerSubModels=true;

    public static ItemISBase instance;

    /**@param name the unlocalized name
     * @param stackSize the stack size
     * @param subNames*/
    public ItemISBase(String name, int stackSize, String... subNames)
    {
        this.setUnlocalizedName(ImmersiveSawmills.modid+"."+name);
        this.setHasSubtypes(subNames!=null&&subNames.length>0);
        this.setCreativeTab(ImmersiveSawmills.creativeTab);
        this.setMaxStackSize(stackSize);
        this.itemName = name;
        this.subNames = subNames!=null&&subNames.length>0?subNames:null;
        this.isMetaHidden = new boolean[this.subNames!=null?this.subNames.length:1];
        ImmersiveSawmills.register(this, name);
        ISContent.registeredISItems.add(this);
        instance = this;
    }

    public String[] getSubNames()
    {
        return subNames;
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
        if(getSubNames()!=null)
        {
            for(int i=0;i<getSubNames().length;i++)
                if(!isMetaHidden(i))
                    list.add(new ItemStack(this,1,i));
        }
        else
            list.add(new ItemStack(this));

    }
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        if(getSubNames()!=null)
        {
            String subName = stack.getItemDamage()<getSubNames().length?getSubNames()[stack.getItemDamage()]:"";
            return this.getUnlocalizedName()+"."+subName;
        }
        return this.getUnlocalizedName();
    }

    public ItemISBase setMetaHidden(int... meta)
    {
        for(int i : meta)
            if(i>=0 && i<this.isMetaHidden.length)
                this.isMetaHidden[i] = true;
        return this;
    }
    public ItemISBase setMetaUnhidden(int... meta)
    {
        for(int i : meta)
            if(i>=0 && i<this.isMetaHidden.length)
                this.isMetaHidden[i] = false;
        return this;
    }
    public boolean isMetaHidden(int meta)
    {
        return this.isMetaHidden[Math.max(0, Math.min(meta, this.isMetaHidden.length-1))];
    }

    public ItemISBase setRegisterSubModels(boolean register)
    {
        this.registerSubModels = register;
        return this;
    }

    @Override
    public int getBurnTime() {
        return Config.burnTimes.getOrDefault(getUnlocalizedName(), 0);
    }
}
