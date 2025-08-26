package com.sushiy.tektopiaaddons;

import com.craftstudio.common.animation.AnimationHandler;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.tangotek.tektopia.*;
import net.tangotek.tektopia.entities.EntityCaptainAura;
import net.tangotek.tektopia.entities.EntityGuard;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.*;
import net.tangotek.tektopia.storage.ItemDesire;
import net.tangotek.tektopia.storage.UpgradeEquipment;
import net.tangotek.tektopia.structures.VillageStructure;
import net.tangotek.tektopia.structures.VillageStructureBarracks;
import net.tangotek.tektopia.structures.VillageStructureType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EntityArcher extends EntityVillagerTek implements IRangedAttackMob
{
    protected static AnimationHandler animHandler = TekVillager.getNewAnimationHandler(EntityArcher.class);

    //AI Filters
    private static final DataParameter<Boolean> SALUTE;
    private static final DataParameter<Boolean> EAT_GOLDEN_APPLE;
    private static final DataParameter<Boolean> PICKUP_EMERALDS;
    private static final DataParameter<Boolean> EQUIP_LEATHER_ARMOR;

    private static final DataParameter<Boolean> PRACTICE_BOW;

    protected int courageChance = 1;
    protected int wantsPractice = 0;


    public EntityArcher(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, ProfessionType.GUARD, VillagerRole.VILLAGER.value | VillagerRole.DEFENDER.value);
    }

    protected int getBowSkillChance() {
        return this.getAttackTarget() instanceof EntityArmorStand ? 4 : 1;
    }

    protected void initEntityAI() {
        super.initEntityAI();
        Runnable onHit = () -> {
            this.tryAddSkill(ProfessionType.GUARD, this.getBowSkillChance());
            if (this.isHostile().test(this.getAttackTarget())) {
                if (this.courageChance > 0 && this.getRNG().nextInt(this.courageChance) == 0) {
                    EntityCaptainAura aura = new EntityCaptainAura(this.getEntityWorld(), this.getX(), this.getY(), this.getZ());
                    aura.setRadius(3.0F);
                    aura.setWaitTime(10);
                    aura.setDuration(40);
                    aura.setRadiusPerTick((float)this.getSkillLerp(ProfessionType.GUARD, 1, 6) / 10.0F);
                    this.world.spawnEntity(aura);
                    this.playSound(ModSoundEvents.courageAura);
                    this.courageChance += 6;
                } else {
                    this.courageChance = Math.max(this.courageChance - 1, 1);
                }
            }

        };
        this.getDesireSet().addItemDesire(new ItemDesire(Items.GOLDEN_APPLE, 1, 1, 1, (Predicate)null));
        this.getDesireSet().addItemDesire(new UpgradeEquipment("Weapon", getBestWeapon(this), EntityEquipmentSlot.MAINHAND, (Predicate)null));
        this.addTask(49, new EntityAIEatGoldenApple(this));
        this.addTask(49, new EntityAIMeleeTarget(this, (p) -> getWeapon(this), VillagerThought.SWORD, (p) -> !p.isSleepingTime(), onHit, ProfessionType.GUARD));
        this.addTask(49, new EntityAIAttackRangedBow2<EntityArcher>(this, 1.0D, 20, 15.0F));
        List<EntityAIPickUpItem.PickUpData> pickUpCounts = new ArrayList();
        pickUpCounts.add(new EntityAIPickUpItem.PickUpData(new ItemStack(Items.EMERALD, 1, 0), 60, "pickup_emeralds"));
        this.addTask(50, new EntityAIPickUpItem(this, pickUpCounts, 1));
        this.addTask(50, new EntityAIPatrolGuardPost(this, (p) -> this.hasVillage() && p.isWorkTime() && hasWeapon(this), 8, 60));
        this.addTask(50, new EntityAIPatrolVillage(this, (p) -> hasWeapon(this)));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(2, new EntityAIProtectVillage(this, (p) -> !p.isSleeping()));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityZombie.class, true));
    }

    public static Function<ItemStack, Integer> getBestWeapon(EntityArcher archer) {
        return (p) -> {
            if (p.getItem() instanceof ItemBow) {
                ItemBow bow = (ItemBow)p.getItem();
                if (p.isItemEnchanted() && !archer.isAIFilterEnabled("equip_enchanted_sword")) {
                    return -1;
                }else {
                    int score = 1;
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

    public static boolean hasWeapon(EntityVillagerTek villager) {
        return !getWeapon(villager).isEmpty();
    }

    protected static ItemStack getWeapon(EntityVillagerTek villager) {
        return villager.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
    }

    @Override
    public void setRevengeTarget(@Nullable EntityLivingBase livingBase) {
        super.setRevengeTarget(livingBase);
        if (livingBase != null && this.getAttackTarget() != null && this.getAttackTarget() != livingBase && !(livingBase instanceof EntityPlayer)) {
            double distTarget = this.getDistanceSq(this.getAttackTarget());
            double distRevenge = this.getDistanceSq(livingBase);
            if (distTarget > distRevenge * (double)2.0F) {
                this.setAttackTarget(livingBase);
            }
        }

    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase target) {
        super.setAttackTarget(target);
        if (this.hasVillage() && !(target instanceof EntityArmorStand)) {
            this.getVillage().addActiveDefender(this);
        }

    }


    protected void randomizeGoals() {
        super.randomizeGoals();
        this.wantsPractice = 0;
        if (this.getRNG().nextInt(2) == 0) {
            this.wantsPractice = this.getRNG().nextInt(10) + 15;
        }

    }

    public void attachToVillage(Village v) {
        super.attachToVillage(v);
        this.sleepOffset = v.getNextGuardSleepOffset();
    }

    public int wantsPractice() {
        return this.getSkill(ProfessionType.GUARD) < this.getIntelligence() ? this.wantsPractice : 0;
    }

    protected void bedCheck() {
        if (this.hasVillage() && this.homeFrame != null) {
            VillageStructure struct = this.village.getStructureFromFrame(this.homeFrame);
            if (!(struct instanceof VillageStructureBarracks)) {
                List<VillageStructure> barracks = this.village.getStructures(VillageStructureType.BARRACKS);
                Stream var10000 = barracks.stream();
                VillageStructureBarracks.class.getClass();
                //VillageStructureBarracks availBarracks = (VillageStructureBarracks)var10000.map(VillageStructureBarracks.class::cast).filter((b) -> !b.isFull()).findAny().orElse((Object)null);
                //if (availBarracks != null) {
               //   this.clearHome();
                //}
            }
        }

        super.bedCheck();
    }

    public void onStopSleep() {
        super.onStopSleep();
        this.equipBestGear();
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();
    }

    protected boolean canVillagerPickupItem(Item itemIn) {
        return false;
    }

    public com.google.common.base.Predicate<Entity> isSuitableTarget() {
        return (e) -> super.isHostile().test(e) || e instanceof EntityArmorStand;
    }

    public boolean isFleeFrom(Entity e) {
        ItemStack equipped = this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        return equipped == ItemStack.EMPTY ? super.isFleeFrom(e) : false;
    }

    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
    }

    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        EntityArrow entityarrow = this.getArrow(distanceFactor);
        if (this.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
            entityarrow = ((net.minecraft.item.ItemBow) this.getHeldItemMainhand().getItem()).customizeArrow(entityarrow);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - entityarrow.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);
        entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(14 - this.world.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(float p_190726_1_)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
        entitytippedarrow.setEnchantmentEffectsFromEntity(this, p_190726_1_);
        return entitytippedarrow;
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {

    }

    @Override
    public AnimationHandler getAnimationHandler() {
        return animHandler;
    }

    static {
        SALUTE = EntityDataManager.createKey(EntityArcher.class, DataSerializers.BOOLEAN);
        EAT_GOLDEN_APPLE = EntityDataManager.createKey(EntityArcher.class, DataSerializers.BOOLEAN);
        PICKUP_EMERALDS = EntityDataManager.createKey(EntityArcher.class, DataSerializers.BOOLEAN);
        PRACTICE_BOW = EntityDataManager.createKey(EntityArcher.class, DataSerializers.BOOLEAN);
        EQUIP_LEATHER_ARMOR = EntityDataManager.createKey(EntityArcher.class, DataSerializers.BOOLEAN);

        int[] var10000 = new int[4];
        Block var10003 = Blocks.DIRT;
        var10000[0] = Block.getStateId(Blocks.DIRT.getDefaultState());
        var10003 = Blocks.DIRT;
        var10000[1] = Block.getStateId(Blocks.DIRT.getDefaultState());
        var10003 = Blocks.STONE;
        var10000[2] = Block.getStateId(Blocks.STONE.getDefaultState());
        var10003 = Blocks.COBBLESTONE;
        var10000[3] = Block.getStateId(Blocks.COBBLESTONE.getDefaultState());

        animHandler.addAnim("tektopia", "villager_chop", "guard_m", true);
        animHandler.addAnim("tektopia", "villager_salute", "guard_m", true);
        animHandler.addAnim("tektopia", "villager_craft", "guard_m", false);
        EntityVillagerTek.setupAnimations(animHandler, "guard_m");
    }
}
