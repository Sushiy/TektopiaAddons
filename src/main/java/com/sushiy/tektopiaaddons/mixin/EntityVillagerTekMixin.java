package com.sushiy.tektopiaaddons.mixin;
import com.leviathanstudio.craftstudio.common.animation.AnimationHandler;
import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.*;
import net.minecraft.world.World;
import net.tangotek.tektopia.entities.EntityNecromancer;
import net.tangotek.tektopia.entities.EntityVillageNavigator;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = EntityVillagerTek.class)
public abstract class EntityVillagerTekMixin extends EntityVillageNavigator
{
    public EntityVillagerTekMixin(World worldIn, int rolesMask) {
        super(worldIn, rolesMask);
    }

    /**
     * @author Sushiy
     * @reason Added all "monsters" to have a more generic way of finding enemies
     */
    @Overwrite(remap = false)
    public com.google.common.base.Predicate<Entity> isHostile() {
        return (e) ->
                (e.isCreatureType(EnumCreatureType.MONSTER, false)
                || e instanceof EntityZombie && !(e instanceof EntityPigZombie)
                || e instanceof EntityWitherSkeleton
                || e instanceof EntityEvoker
                || e instanceof EntityVex
                || e instanceof EntityVindicator
                || e instanceof EntityNecromancer)
                && !TektopiaAddons.ignoredMonsters.contains(e.getName());
    }
    /**
     * @author Sushiy
     * @reason Ignored Monsters should still be fleed from
     */
    @Overwrite(remap = false)
    public boolean isFleeFrom(Entity e) {
        return this.isHostile().test(e) || TektopiaAddons.ignoredMonsters.contains(e.getName());
    }
}
