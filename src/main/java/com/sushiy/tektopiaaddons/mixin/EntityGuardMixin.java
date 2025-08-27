package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.tangotek.tektopia.ItemTagType;
import net.tangotek.tektopia.ModItems;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.VillagerRole;
import net.tangotek.tektopia.entities.EntityGuard;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

@Mixin(value = EntityGuard.class)
public abstract class EntityGuardMixin extends EntityVillagerTek
{
    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> EQUIP_AUTOCHANGE_ARMOR;
    @Unique
    @Mutable
    private static @Final DataParameter<Boolean> EQUIP_AUTOCHANGE_WEAPON;

    public EntityGuardMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @Inject(method = "entityInit", at = @At("TAIL"))
    protected void entityInit(CallbackInfo ci) {
        this.registerAIFilter("equip_autochange.armor", EQUIP_AUTOCHANGE_ARMOR);
        this.registerAIFilter("equip_autochange.weapon", EQUIP_AUTOCHANGE_WEAPON);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticBlock(CallbackInfo ci) {
        EQUIP_AUTOCHANGE_ARMOR = EntityDataManager.createKey(EntityGuard.class, DataSerializers.BOOLEAN);
        EQUIP_AUTOCHANGE_WEAPON = EntityDataManager.createKey(EntityGuard.class, DataSerializers.BOOLEAN);

    }

    /**
     * @author Sushiy
     * @reason needs to take into account the autoequip filters
     */
    @Overwrite(remap = false)
    public void equipBestGear() {

        EntityGuard guard = (EntityGuard) (Object)this;
        if(isAIFilterEnabled("equip_autochange.armor"))
        {
            this.equipBestGear(EntityEquipmentSlot.CHEST, getBestArmor(guard, EntityEquipmentSlot.CHEST));
            this.equipBestGear(EntityEquipmentSlot.LEGS, getBestArmor(guard, EntityEquipmentSlot.LEGS));
            this.equipBestGear(EntityEquipmentSlot.FEET, getBestArmor(guard, EntityEquipmentSlot.FEET));
            this.equipBestGear(EntityEquipmentSlot.HEAD, getBestArmor(guard, EntityEquipmentSlot.HEAD));
            this.equipBestGear(EntityEquipmentSlot.OFFHAND, tektopiaAddons$getBestShield(guard));
        }
        if(isAIFilterEnabled("equip_autochange.weapon")) {
            this.equipBestGear(EntityEquipmentSlot.MAINHAND, getBestWeapon(guard));
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        float beforeHealth = this.getHealth();

        if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, source, amount)) return false;
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (this.world.isRemote)
        {
            return false;
        }
        else
        {
            this.idleTime = 0;

            if (this.getHealth() <= 0.0F)
            {
                return false;
            }
            else if (source.isFireDamage() && this.isPotionActive(MobEffects.FIRE_RESISTANCE))
            {
                return false;
            }
            else
            {
                float f = amount;

                if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
                {
                    this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).damageItem((int)(amount * 4.0F + this.rand.nextFloat() * amount * 2.0F), this);
                    amount *= 0.75F;
                }

                boolean flag = false;

                if (amount > 0.0F && tektopiaAddons$canBlockDamageSource(source))
                {
                    this.damageShield(amount);
                    amount = 0.0F;

                    if (!source.isProjectile())
                    {
                        Entity entity = source.getImmediateSource();

                        if (entity instanceof EntityLivingBase)
                        {
                            this.blockUsingShield((EntityLivingBase)entity);
                            this.playSound(SoundEvents.ITEM_SHIELD_BLOCK,this.getSoundVolume(), this.getSoundPitch());
                        }
                        this.world.setEntityState(this, (byte)29);
                    }
                    return false;
                }
            }

        }
        if (super.attackEntityFrom(source, amount))
        {
            float afterHealth = this.getHealth();
            float actualDamage = beforeHealth - afterHealth;
            if (actualDamage > 0.0F) {
                if (!this.isRole(VillagerRole.DEFENDER)) {
                    this.modifyHappy(-8);
                }

                if (this.hasVillage() && actualDamage > 0.0F) {
                    this.getVillage().reportVillagerDamage(this, source, actualDamage);
                }

                if (this.isSleeping()) {
                    this.onStopSleep();
                }
            }

            return true;
        } else {
            return false;
        }
    }


    @Unique
    private boolean tektopiaAddons$canBlockDamageSource(DamageSource damageSourceIn)
    {
        if (!damageSourceIn.isUnblockable() && tektopiaAddons$GetRandomShieldChance(damageSourceIn))
        {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vec3d vec3d1 = this.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Unique
    private boolean tektopiaAddons$GetRandomShieldChance(DamageSource damageSourceIn) {
        EntityGuard guard = (EntityGuard) (Object)this;
        if(guard.getHeldItemOffhand().getItem() instanceof ItemShield)
        {
            Random rnd = new Random();
            if(damageSourceIn.isProjectile())
            {
                return rnd.nextInt(4) > 0;
            }
            else
            {
                return rnd.nextBoolean();
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        EntityGuard guard = (EntityGuard) (Object)this;
        if(itemStack.getItem() instanceof ItemArmor)
        {
            ItemArmor armor = (ItemArmor) itemStack.getItem();
            ItemStack equippedArmor = guard.getItemStackFromSlot(armor.getEquipmentSlot());
            player.setHeldItem(hand, ItemStack.EMPTY);
            setItemStackToSlot(armor.getEquipmentSlot(), itemStack);
            if(!equippedArmor.isEmpty())
            {
                player.setHeldItem(hand, equippedArmor);
            }
            setAIFilter("equip_autochange.armor", false);
            return true;
        }
        if(itemStack.getItem() instanceof ItemSword)
        {
            ItemSword newWeapon = (ItemSword) itemStack.getItem();
            ItemStack oldWeapon = guard.getHeldItemMainhand();
            player.setHeldItem(hand, ItemStack.EMPTY);
            setHeldItem(EnumHand.MAIN_HAND, itemStack);
            if(!oldWeapon.isEmpty())
            {
                player.setHeldItem(hand, oldWeapon);
            }
            setAIFilter("equip_autochange.weapon", false);
            return true;
        }
        if(itemStack.getItem() instanceof ItemShield)
        {
            ItemShield newShield = (ItemShield) itemStack.getItem();
            ItemStack oldShield = guard.getHeldItemOffhand();
            player.setHeldItem(hand, ItemStack.EMPTY);
            setHeldItem(EnumHand.OFF_HAND, itemStack);
            if(!oldShield.isEmpty())
            {
                player.setHeldItem(hand, oldShield);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    /**
     * @author Sushiy
     * @reason always prefer current item if autochange equipment is blocked
     */
    @Overwrite(remap = false)
    public static Function<ItemStack, Integer> getBestWeapon(EntityGuard guard) {
        return (p) -> {
            if(!guard.isAIFilterEnabled("equip_autochange.weapon"))
                return p == guard.getHeldItemMainhand() ? 100 : -1;

            if (p.getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword)p.getItem();
                if (p.isItemEnchanted() && !guard.isAIFilterEnabled("equip_enchanted_sword")) {
                    return -1;
                } else if (sword.getToolMaterialName().equals(Item.ToolMaterial.DIAMOND.name()) && !guard.isAIFilterEnabled("equip_diamond_sword")) {
                    return -1;
                } else if (sword.getToolMaterialName().equals(Item.ToolMaterial.IRON.name()) && !guard.isAIFilterEnabled("equip_iron_sword")) {
                    return -1;
                } else {
                    int score = (int)sword.getAttackDamage();
                    score = (int)((float)score + EnchantmentHelper.getModifierForCreature(p, EnumCreatureAttribute.UNDEFINED));
                    ++score;
                    score *= 10;
                    if (ModItems.isTaggedItem(p, ItemTagType.VILLAGER)) {
                        ++score;
                    }

                    return score;
                }
            } else {
                return -1;
            }
        };
    }

    @Unique
    private static Function<ItemStack, Integer> tektopiaAddons$getBestShield(EntityGuard guard)
    {
        return(p) -> {
            if(p.getItem() instanceof ItemShield)
            {
                if(!guard.isAIFilterEnabled("equip_autochange.armor"))
                    return p == guard.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND) ? 100 : -1;
                ItemShield shield = (ItemShield) p.getItem();
                if (p.isItemEnchanted() && !guard.isAIFilterEnabled("equip_enchanted_armor")) {
                    return -1;
                }
                int score = 30;
                score += EnchantmentHelper.getEnchantmentModifierDamage(Arrays.asList(p), DamageSource.GENERIC);
                return score;
            }
            return -1;
        };
    }

    /**
     * @author Sushiy
     * @reason always prefer current item if autochange equipment is blocked
     */
    @Overwrite(remap = false)
    public static Function<ItemStack, Integer> getBestArmor(EntityGuard guard, EntityEquipmentSlot slot) {
        return (p) -> {
            if(!guard.isAIFilterEnabled("equip_autochange.armor"))
                return p == guard.getItemStackFromSlot(slot) ? 100 : -1;
            if (p.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor)p.getItem();
                if (armor.armorType == slot) {
                    if (p.isItemEnchanted() && !guard.isAIFilterEnabled("equip_enchanted_armor")) {
                        return -1;
                    }

                    if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.DIAMOND && !guard.isAIFilterEnabled("equip_diamond_armor")) {
                        return -1;
                    }

                    if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.IRON && !guard.isAIFilterEnabled("equip_iron_armor")) {
                        return -1;
                    }

                    if (armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && !guard.isAIFilterEnabled("equip_leather_armor")) {
                        return -1;
                    }

                    int score = armor.getArmorMaterial().getDamageReductionAmount(armor.armorType);
                    score += EnchantmentHelper.getEnchantmentModifierDamage(Arrays.asList(p), DamageSource.GENERIC);
                    return score;
                }
            }

            return -1;
        };
    }
}
