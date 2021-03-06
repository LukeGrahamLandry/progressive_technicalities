package com.LukeTheDuke9801.progressivetechnicalities.entities.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;

public class ShulkerArrowEntity extends ArrowEntity{
	public ShulkerArrowEntity(EntityType<? extends ArrowEntity> type, World worldIn) {
	      super(type, worldIn);
    }

    public ShulkerArrowEntity(World worldIn, double x, double y, double z) {
       super(worldIn, x, y, z);
    }

    public ShulkerArrowEntity(World worldIn, LivingEntity shooter) {
       super(worldIn, shooter);
    }
    
    protected void onEntityHit(EntityRayTraceResult ray) {
    	super.onEntityHit(ray);
    	LivingEntity entity = (LivingEntity) ray.getEntity();
    	entity.addPotionEffect(new EffectInstance(Effects.LEVITATION, 40, 0));
    }
}
