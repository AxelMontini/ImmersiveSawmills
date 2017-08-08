package axelmontini.immersivesawmills.common.gui;

import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by Axel Montini on 15/07/2017.
 */
public class ContainerSawmill extends ContainerIEBase<TileEntitySawmill> {
    public ContainerSawmill(InventoryPlayer inventoryPlayer, TileEntitySawmill tile) {
        super(inventoryPlayer, tile);

         this.addSlotToContainer(new IESlot(this, this.inv, 0, 30, 30) {
             @Override  //valid if recipe is defined.
             public boolean isItemValid(ItemStack itemStack) {
                 return SawmillRecipe.findRecipe(itemStack) != null;
             }
         });

        this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 60, 30));
        this.addSlotToContainer(new IESlot.Output(this, this.inv, 2, 60, 30));
    }
}
