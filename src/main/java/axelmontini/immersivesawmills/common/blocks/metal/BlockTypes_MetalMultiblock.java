package axelmontini.immersivesawmills.common.blocks.metal;

import axelmontini.immersivesawmills.common.blocks.BlockISBase;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;


public enum BlockTypes_MetalMultiblock implements BlockISBase.IBlockEnum, IStringSerializable {
    SAWMILL(true),
    BIOMASS_GENERATOR(true);

    private boolean needsCustomState;
    BlockTypes_MetalMultiblock(boolean needsCustomState) {this.needsCustomState = needsCustomState;}

    @Override
    public int getMeta() {
        return ordinal();
    }

    @Override
    public boolean listForCreative() {
        return false;
    }

    @Override
    public String getName() {
        return this.toString().toLowerCase(Locale.ENGLISH);
    }

    public boolean needsCustomState()
    {
        return this.needsCustomState;
    }

    public String getCustomState()
    {
        String[] split = getName().split("_");
        String s = split[0].toLowerCase(Locale.ENGLISH);
        for(int i=1; i<split.length; i++)
            s+=split[i].substring(0,1).toUpperCase(Locale.ENGLISH)+split[i].substring(1).toLowerCase(Locale.ENGLISH);
        return s;
    }
}
