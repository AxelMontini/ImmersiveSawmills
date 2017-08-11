package axelmontini.immersivesawmills.common.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**Composed of multiple stacks*/
public class MultiStackItemStack {
    List<ItemStack> inventory = new ArrayList<ItemStack>();

    private int capacity;
    private int items = 0;



    /**@param capacityTot the max amount of items this can hold.
     * @param stacks the first stacks to insert.*/
    public MultiStackItemStack(int capacityTot, ItemStack... stacks) {
        capacity = capacityTot;
        putStacks(stacks);
    }

    /**init from NBT tag compound*/
    public MultiStackItemStack(NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    /**Adds this item to the list, if possible.
     * @return the items that couldn't be put in.*/
    public ItemStack[] putStack(ItemStack stack) {
        return putStacks(stack);
    }

    /**@return the items that couldn't be put in.*/
    public ItemStack[] putStacks(ItemStack... stacks) {
        for(int i=0; i<stacks.length; i++) {
            if(items == capacity)
                continue;

            int index = getIndex(stacks[i]);
            if(index!=-1) {
                int toPut = (stacks[i].stackSize+items<capacity?stacks[i].stackSize:capacity-items);
                inventory.get(index).stackSize += toPut;
                stacks[i].stackSize -= toPut;
                items+=toPut;
            }
        }


        return stacks;
    }

    /**@return the amount of items of the given type*/
    public int getStackSize(ItemStack stack) {
        return inventory.get(getIndex(stack)).stackSize;
    }

    /**@return true if this contains already this type.*/
    public boolean containsType(ItemStack stack) {
        return inventory.parallelStream().anyMatch(s -> s.isItemEqual(stack));
    }

    /**@return the index of this type, or -1 if not found.*/
    public int getIndex(ItemStack stack) {
        for (int i=0; i<inventory.size(); i++) {
            if (inventory.get(i).isItemEqual(stack))
                return i;
        }
        return -1;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        for(int i=0; i<inventory.size(); i++)
            nbt.setTag(String.valueOf(i), inventory.get(i).writeToNBT(new NBTTagCompound()));

        nbt.setInteger("capacity", capacity);
        nbt.setInteger("size", items);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        for(String key: nbt.getKeySet()) {
            if(key.contentEquals("capacity"))
                capacity = nbt.getInteger("capacity");
            else if(key.contentEquals("size"))
                items = nbt.getInteger("size");
            else {
                putStack(ItemStack.loadItemStackFromNBT((NBTTagCompound) nbt.getTag(key)));
            }
        }
    }


}
