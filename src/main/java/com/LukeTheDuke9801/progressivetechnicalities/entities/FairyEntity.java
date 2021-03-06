package com.LukeTheDuke9801.progressivetechnicalities.entities;

import com.LukeTheDuke9801.progressivetechnicalities.ProgressiveTechnicalities;
import com.LukeTheDuke9801.progressivetechnicalities.init.BiomeInit;
import com.LukeTheDuke9801.progressivetechnicalities.objects.fluids.NymphariumFluidBlock;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public class FairyEntity extends MonsterEntity {
    protected static final DataParameter<Byte> VEX_FLAGS = EntityDataManager.createKey(FairyEntity.class, DataSerializers.BYTE);
    private int angryTimer;

    public FairyEntity(EntityType<? extends FairyEntity> p_i50190_1_, World p_i50190_2_) {
        super(p_i50190_1_, p_i50190_2_);
        this.moveController = new FairyEntity.MoveHelperController(this);
        this.experienceValue = 3;
        this.angryTimer = 0;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        entityIn.setMotion(entityIn.getMotion().add(0.0D, 2.0D, 0.0D));
        return flag;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        this.angryTimer = 1*60*20;

        if (source == NymphariumFluidBlock.DAMAGE_SOURCE) this.heal(amount);
        return super.attackEntityFrom(source, amount);
    }

    public boolean isAngry(){
        return this.angryTimer > 0;
    }

    public boolean isInvulnerableTo(DamageSource source) {
        if (source == NymphariumFluidBlock.DAMAGE_SOURCE) return true;
        return super.isInvulnerableTo(source);
    }

    @Nullable
    @Override
    public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        updateTexture();
        this.setEquipmentBasedOnDifficulty(difficultyIn);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    public void move(MoverType typeIn, Vec3d pos) {
        super.move(typeIn, pos);
        this.doBlockCollisions();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);

        if (this.angryTimer > 0){
            this.angryTimer--;
        }

        // TODO: only do this when it first spawns
        updateTexture();
    }

    private void updateTexture(){
        Biome biome = this.world.getBiome(new BlockPos(this));
        if (biome == BiomeInit.FEY_PLAINS.get()) this.texture = Texture.GREEN;
        else if (biome == BiomeInit.FRUIT_FOREST.get()) this.texture = Texture.BLUE;
        else if (biome == BiomeInit.MUSHROOM_FOREST.get()) this.texture = Texture.BLACK;
        else if (biome == BiomeInit.OIL_SPIKES.get()) this.texture = Texture.YELLOW;
        else this.texture = Texture.ORIGINAL;
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(4, new FairyEntity.ChargeAttackGoal());
        this.goalSelector.addGoal(8, new FairyEntity.MoveRandomGoal());
        this.goalSelector.addGoal(9, new LookAtGoal(this, PlayerEntity.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_KNOCKBACK).setBaseValue(5.0D);
    }

    protected void registerData() {
        super.registerData();
        this.dataManager.register(VEX_FLAGS, (byte)0);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.angryTimer = compound.getInt("angryTimer");
        this.texture = Texture.getByName(compound.getString("texture"));
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("angryTimer", this.angryTimer);
        compound.putString("texture", this.texture.getName());
    }

    public Texture texture = Texture.ORIGINAL;
    public static enum Texture{
        ORIGINAL("original"),
        BLUE("blue"),
        GREEN("green"),
        BLACK("black"),
        YELLOW("yellow");

        private final String name;
        public final ResourceLocation resourceLocation;
        private static final Map<String, Texture> TEXTURES_BY_NAME =  Arrays.stream(values()).collect(Collectors.toMap(Texture::getName, (p_221081_0_) -> {
            return p_221081_0_;
        }));
        private Texture(String nameIn){
            this.name = nameIn;
            this.resourceLocation = new ResourceLocation(ProgressiveTechnicalities.MOD_ID, "textures/entity/" + nameIn + "_fairy.png");
        }

        public String getName(){
            return this.name;
        }

        public static Texture getByName(String name){
            return TEXTURES_BY_NAME.get(name);
        }
    }

    private boolean getVexFlag(int mask) {
        int i = this.dataManager.get(VEX_FLAGS);
        return (i & mask) != 0;
    }

    private void setVexFlag(int mask, boolean value) {
        int i = this.dataManager.get(VEX_FLAGS);
        if (value) {
            i = i | mask;
        } else {
            i = i & ~mask;
        }

        this.dataManager.set(VEX_FLAGS, (byte)(i & 255));
    }

    public boolean isCharging() {
        return this.getVexFlag(1);
    }

    public void setCharging(boolean charging) {
        this.setVexFlag(1, charging);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VEX_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness() {
        return 8.0F;
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        ItemStack sword = new ItemStack(Items.ALLIUM);
        sword.addEnchantment(Enchantments.KNOCKBACK, 2);
        this.setItemStackToSlot(EquipmentSlotType.MAINHAND, sword);
        this.setDropChance(EquipmentSlotType.MAINHAND, 0.0F);
    }

    class ChargeAttackGoal extends Goal {
        public ChargeAttackGoal() {
            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean shouldExecute() {
            if (FairyEntity.this.angryTimer > 0 && FairyEntity.this.getAttackTarget() != null && !FairyEntity.this.getMoveHelper().isUpdating()){ // && FairyEntity.this.rand.nextInt(7) == 0) {
                return FairyEntity.this.getDistanceSq(FairyEntity.this.getAttackTarget()) > 4.0D;
            } else {
                return false;
            }
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting() {
            return FairyEntity.this.getMoveHelper().isUpdating() && FairyEntity.this.isCharging() && FairyEntity.this.getAttackTarget() != null && FairyEntity.this.getAttackTarget().isAlive();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void startExecuting() {
            LivingEntity livingentity = FairyEntity.this.getAttackTarget();
            Vec3d vec3d = livingentity.getEyePosition(1.0F);
            FairyEntity.this.moveController.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 2.0D);
            FairyEntity.this.setCharging(true);
            FairyEntity.this.playSound(SoundEvents.ENTITY_VEX_CHARGE, 1.0F, 1.0F);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void resetTask() {
            FairyEntity.this.setCharging(false);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            LivingEntity livingentity = FairyEntity.this.getAttackTarget();
            if (FairyEntity.this.getBoundingBox().intersects(livingentity.getBoundingBox())) {
                FairyEntity.this.attackEntityAsMob(livingentity);
                FairyEntity.this.setCharging(false);
            } else {
                double d0 = FairyEntity.this.getDistanceSq(livingentity);
                if (d0 < 9.0D) {
                    Vec3d vec3d = livingentity.getEyePosition(1.0F);
                    FairyEntity.this.moveController.setMoveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
                }
            }

        }
    }

    class MoveHelperController extends MovementController {
        public MoveHelperController(FairyEntity vex) {
            super(vex);
        }

        public void tick() {
            if (this.action == MovementController.Action.MOVE_TO) {
                Vec3d vec3d = new Vec3d(this.posX - FairyEntity.this.getPosX(), this.posY - FairyEntity.this.getPosY(), this.posZ - FairyEntity.this.getPosZ());
                double d0 = vec3d.length();
                if (d0 < FairyEntity.this.getBoundingBox().getAverageEdgeLength()) {
                    this.action = MovementController.Action.WAIT;
                    FairyEntity.this.setMotion(FairyEntity.this.getMotion().scale(0.5D));
                } else {
                    FairyEntity.this.setMotion(FairyEntity.this.getMotion().add(vec3d.scale(this.speed * 0.05D / d0)));
                    if (FairyEntity.this.getAttackTarget() == null) {
                        Vec3d vec3d1 = FairyEntity.this.getMotion();
                        FairyEntity.this.rotationYaw = -((float)MathHelper.atan2(vec3d1.x, vec3d1.z)) * (180F / (float)Math.PI);
                        FairyEntity.this.renderYawOffset = FairyEntity.this.rotationYaw;
                    } else {
                        double d2 = FairyEntity.this.getAttackTarget().getPosX() - FairyEntity.this.getPosX();
                        double d1 = FairyEntity.this.getAttackTarget().getPosZ() - FairyEntity.this.getPosZ();
                        FairyEntity.this.rotationYaw = -((float)MathHelper.atan2(d2, d1)) * (180F / (float)Math.PI);
                        FairyEntity.this.renderYawOffset = FairyEntity.this.rotationYaw;
                    }
                }

            }
        }
    }
    
    class MoveRandomGoal extends Goal {
        public MoveRandomGoal() {
            this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean shouldExecute() {
            return !FairyEntity.this.getMoveHelper().isUpdating() && FairyEntity.this.rand.nextInt(7) == 0;
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean shouldContinueExecuting() {
            return false;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            BlockPos blockpos =  new BlockPos(FairyEntity.this);

            for(int i = 0; i < 3; ++i) {
                BlockPos blockpos1 = blockpos.add(FairyEntity.this.rand.nextInt(15) - 7, FairyEntity.this.rand.nextInt(11) - 5, FairyEntity.this.rand.nextInt(15) - 7);
                if (FairyEntity.this.world.isAirBlock(blockpos1)) {
                    FairyEntity.this.moveController.setMoveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 0.25D);
                    if (FairyEntity.this.getAttackTarget() == null) {
                        FairyEntity.this.getLookController().setLookPosition((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.5D, (double)blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
                    }
                    break;
                }
            }

        }
    }
}