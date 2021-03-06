package com.LukeTheDuke9801.progressivetechnicalities.objects.fluids;

import java.util.Random;

import javax.annotation.Nullable;

import com.LukeTheDuke9801.progressivetechnicalities.ProgressiveTechnicalities;
import com.LukeTheDuke9801.progressivetechnicalities.init.BlockInit;
import com.LukeTheDuke9801.progressivetechnicalities.init.FluidInit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;

public abstract class NymphariumFluid extends FlowingFluid {
   public Fluid getFlowingFluid() {
      return FluidInit.NYMPHARIUM_FLOWING.get();
   }

   public Fluid getStillFluid() {
      return FluidInit.NYMPHARIUM_FLUID.get();
   }

   public Item getFilledBucket() {
      return FluidInit.NYMPHARIUM_FLUID_BUCKET.get();
   }
   
   @Override
	protected FluidAttributes createAttributes() {
	   return net.minecraftforge.fluids.FluidAttributes.builder(
               FluidInit.NYMPHARIUM_STILL_RL,
               FluidInit.NYMPHARIUM_FLOWING_RL)
               .luminosity(1000).density(3000).viscosity(6000).temperature(0).overlay(FluidInit.NYMPHARIUM_OVERLAY_RL).build(this);
	}

    public static void solidifyNearby(LivingEntity living, World worldIn, BlockPos pos, int level) {
	      if (living.onGround || true) {
	         BlockState blockstate = FluidInit.NYMPHARIUM_ICE.get().getDefaultState();
	         float f = (float)Math.min(16, 2 + level);
	         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

	         for(BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add((double)(-f), -1.0D, (double)(-f)), pos.add((double)f, -1.0D, (double)f))) {
	            if (blockpos.withinDistance(living.getPositionVec(), (double)f)) {
	               blockpos$mutable.setPos(blockpos.getX(), blockpos.getY() + 1, blockpos.getZ());
	               BlockState blockstate1 = worldIn.getBlockState(blockpos$mutable);
	               if (blockstate1.isAir(worldIn, blockpos$mutable)) {
	                  BlockState blockstate2 = worldIn.getBlockState(blockpos);
	                  boolean isFull = blockstate2.getBlock() == FluidInit.NYMPHARIUM_FLUID_BLOCK.get() && blockstate2.get(FlowingFluidBlock.LEVEL) == 0; 
	                  boolean isNympharium = blockstate2.getFluidState().getFluid().isEquivalentTo(FluidInit.NYMPHARIUM_FLUID.get());
	                  if (isNympharium && isFull && blockstate.isValidPosition(worldIn, blockpos) && worldIn.func_226663_a_(blockstate, blockpos, ISelectionContext.dummy()) && !net.minecraftforge.event.ForgeEventFactory.onBlockPlace(living, new net.minecraftforge.common.util.BlockSnapshot(worldIn, blockpos, blockstate2), net.minecraft.util.Direction.UP)) {
	                     worldIn.setBlockState(blockpos, blockstate);
	                     worldIn.getPendingBlockTicks().scheduleTick(blockpos,FluidInit.NYMPHARIUM_ICE.get(), MathHelper.nextInt(living.getRNG(), 60, 120));
	                  }
	               }
	            }
	         }

	      }
	   }
   
   @OnlyIn(Dist.CLIENT)
   public void animateTick(World worldIn, BlockPos pos, IFluidState state, Random random) {
      if (!state.isSource() && !state.get(FALLING)) {
         if (random.nextInt(64) == 0) {
            worldIn.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.BLOCKS, random.nextFloat() * 0.25F + 0.75F, random.nextFloat() + 0.5F, false);
         }
      } else if (random.nextInt(10) == 0) {
         worldIn.addParticle(ParticleTypes.UNDERWATER, (double)pos.getX() + (double)random.nextFloat(), (double)pos.getY() + (double)random.nextFloat(), (double)pos.getZ() + (double)random.nextFloat(), 0.0D, 0.0D, 0.0D);
      }

   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public IParticleData getDripParticleData() {
      return ParticleTypes.DRIPPING_WATER;
   }

   protected boolean canSourcesMultiply() {
      return false;
   }

   protected void beforeReplacingBlock(IWorld worldIn, BlockPos pos, BlockState state) {
      TileEntity tileentity = state.getBlock().hasTileEntity() ? worldIn.getTileEntity(pos) : null;
      Block.spawnDrops(state, worldIn.getWorld(), pos, tileentity);
   }

   public int getSlopeFindDistance(IWorldReader worldIn) {
      return 4;
   }

   public BlockState getBlockState(IFluidState state) {
      return FluidInit.NYMPHARIUM_FLUID_BLOCK.get().getDefaultState().with(FlowingFluidBlock.LEVEL, Integer.valueOf(getLevelFromState(state)));
   }

   public boolean isEquivalentTo(Fluid fluidIn) {
      return fluidIn == getStillFluid() || fluidIn == getFlowingFluid();
   }

   public int getLevelDecreasePerBlock(IWorldReader worldIn) {
      return 1;
   }

   public int getTickRate(IWorldReader p_205569_1_) {
      return 5;
   }

   public boolean canDisplace(IFluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
       return p_215665_5_ == Direction.DOWN && !p_215665_4_.isIn(FluidTags.WATER);
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public static class Flowing extends NymphariumFluid {
      protected void fillStateContainer(StateContainer.Builder<Fluid, IFluidState> builder) {
         super.fillStateContainer(builder);
         builder.add(LEVEL_1_8);
      }

      public int getLevel(IFluidState p_207192_1_) {
         return p_207192_1_.get(LEVEL_1_8);
      }

      public boolean isSource(IFluidState state) {
         return false;
      }
   }

   public static class Source extends NymphariumFluid {
      public int getLevel(IFluidState p_207192_1_) {
         return 8;
      }

      public boolean isSource(IFluidState state) {
         return true;
      }
   }
}