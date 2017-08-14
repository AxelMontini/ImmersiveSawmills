package axelmontini.immersivesawmills.common.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**Composed of multiple stacks
 * @deprecated f**king useless and doesn't even work, plus huge overhead.*/
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

    /**Extracts the specified amount of items from an itemstack of this type.
     * Doesn't copy the NBT tag of the itemstack, so be aware.
     * @param type an ItemStack representing the type to extract.
     * @param amount how many to extract, with -1 to retrieve all of them.
     * @return the extracted itemstack.*/
    public ItemStack pullStack(ItemStack type, int amount) {
        return pullStack(getIndex(type), amount);
    }

    public ItemStack pullStack(int index, int amount) {
        ItemStack stack = inventory.get(index);
        int amountExtracted = 0;
        if (stack.stackSize > amount) {
            amountExtracted = amount;
            stack.stackSize -= amount;//Remove "amount" of items from ItemStack
        } else {
            amountExtracted = amount - stack.stackSize;
            stack.stackSize = 0;
        }
        deleteEmpty();
        return new ItemStack(stack.getItem(), amountExtracted, stack.getMetadata());
    }

    public ItemStack pullStackRandom(int amount) {
        return pullStack(new Random().nextInt(inventory.size()), amount);
    }

    /**Extracts some ItemStacks based on the condition given.
     * @param amountCondition function which returns the number of items to extract from an itemstack.*/
    public ItemStack[] pullStacks(Function<ItemStack, Integer> amountCondition) {
        ItemStack[] stacks = new ItemStack[inventory.size()];

        for(ListIterator<ItemStack> i = inventory.listIterator(); i.hasNext();) {
            int index = i.nextIndex();

            ItemStack stack = i.next();
            int toExtract = amountCondition.apply(stack);
            if(toExtract > 0) {
                if (stack.stackSize > toExtract) {
                    stack.stackSize -= toExtract;//Remove "amount" of items from ItemStack
                } else {
                    toExtract = toExtract - stack.stackSize;
                    stack.stackSize = 0;
                }
                i.set(stack);
                stacks[index] = new ItemStack(stack.getItem(), toExtract, stack.getMetadata());
            }
        }
        deleteEmpty();
        return stacks;
    }

    /**Pulls all the ItemStacks in equal parts each (relative to tot size of the multistack) (rounded down to int).
     * @param maxAmount the max amount of items to pull (not necessarily the amount which will be extracted).
     * @return the extracted itemstacks*/
    public ItemStack[] pullStacksEqualPart(int maxAmount) {
        final int totSize = inventory.size();
        //All the itemstack sizes
        DoubleStream coeffStr = inventory.stream().mapToDouble(s -> s.stackSize/totSize);

        List<ItemStack> toExtract = new ArrayList<>();

        AtomicInteger index = new AtomicInteger(-1);
        for(double coeff : coeffStr.toArray()) {
            ItemStack stack = inventory.get(index.incrementAndGet()).copy();
             stack.stackSize = (int) Math.floor(coeff*maxAmount);

             toExtract.add(pullStack(stack, stack.stackSize));
        }

        deleteEmpty();
        return toExtract.toArray(new ItemStack[]{});
    }

    /**@return the free space*/
    public int getFree() {
        return capacity-items;
    }

    /**@return true if empty*/
    public boolean isEmpty() {
        return !isNotEmpty();
    }

    /**@return true if not empty*/
    public boolean isNotEmpty() {
        return inventory.stream().anyMatch(s->s.stackSize>0);
    }

    /**@return the items that couldn't be put in.*/
    public ItemStack[] putStacks(ItemStack... stacks) {
        if(items>=capacity)
            return stacks;

        for(int i=0; i<stacks.length; i++) {
            if(items == capacity)
                continue;

            int index = getIndex(stacks[i]);
            if(index!=-1) {
                int put = Math.min(capacity-items, stacks[0].stackSize);
                inventory.get(index).stackSize += put;
                stacks[i].stackSize -= put;
                items+=put;
            } else {
                int put = Math.min(capacity-items, stacks[0].stackSize);
                inventory.add(new ItemStack(stacks[i].getItem(), put, stacks[i].getMetadata()));
                stacks[i].stackSize -= put;
                items+=put;
            }
        }
        return stacks;
    }

    /**Remove empty itemstacks from the list using a ListIterator*/
    private void deleteEmpty() {
        for(ListIterator<ItemStack> liter = inventory.listIterator(); liter.hasNext();)
            if(liter.next().stackSize <=0)
                liter.remove(); //Remove if empty
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
        deleteEmpty();
        NBTTagList itemList = new NBTTagList();
        if(inventory.size()!=0)
            for(int i=0; i<inventory.size(); i++)
                itemList.appendTag(inventory.get(i).writeToNBT(new NBTTagCompound()));
        nbt.setTag("items", itemList);
        nbt.setInteger("capacity", capacity);
        nbt.setInteger("size", items);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        capacity = nbt.getInteger("capacity");
        NBTTagList itemList = nbt.getTagList("items", Constants.NBT.TAG_LIST);
        IntStream.range(0, itemList.tagCount()).forEach(index ->
                putStack(ItemStack.loadItemStackFromNBT(itemList.getCompoundTagAt(index)))
        );
        items = inventory.stream().collect(Collectors.summingInt(st -> st.stackSize));
    }


}
