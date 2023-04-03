package com.gildedgames.aether.entity.projectile.crystal;

import com.gildedgames.aether.client.AetherSoundEvents;
import com.gildedgames.aether.client.particle.AetherParticleTypes;
import com.gildedgames.aether.data.resources.AetherDamageTypes;
import com.gildedgames.aether.entity.AetherEntityTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

/**
 * Projectile occasionally generated by the sun spirit. It can be hit back at the sun spirit to deal damage.
 */
public class IceCrystal extends AbstractCrystal {
    public double xPower;
    public double zPower;
    public boolean attacked = false;

    /**
     * Used for registering the entity. Use the other constructor to provide more context.
     */
    public IceCrystal(EntityType<? extends IceCrystal> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @param shooter - The entity that created this projectile
     */
    public IceCrystal(Level level, Entity shooter) {
        this(AetherEntityTypes.ICE_CRYSTAL.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getY(), shooter.getZ());
        float rotation = this.random.nextFloat() * 360;
        this.xPower = Mth.sin(rotation) * 0.20;
        this.zPower = -Mth.cos(rotation) * 0.20;
        this.setDeltaMovement(this.xPower, 0, this.zPower);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.doDamage(result.getEntity());
    }

    public void doDamage(Entity entity) {
        if (this.getOwner() != entity) {
            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.hurt(AetherDamageTypes.indirectEntityDamageSource(this.level, AetherDamageTypes.ICE_CRYSTAL, this, this.getOwner()), 7.0F)) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10));
                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(), this.getImpactExplosionSoundEvent(), SoundSource.HOSTILE, 2.0F, this.random.nextFloat() - this.random.nextFloat() * 0.2F + 1.2F);
                    this.discard();
                }
            }
        }
    }

    /**
     * Until the crystal is hit by a player, it will bounce off the walls. If it has already been attacked once,
     * it will break on the next block it touches.
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.attacked) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), this.getImpactExplosionSoundEvent(), SoundSource.HOSTILE, 2.0F, this.random.nextFloat() - this.random.nextFloat() * 0.2F + 1.2F);
            this.discard();
            return;
        }
        this.markHurt();
        switch (result.getDirection()) {
            case NORTH, SOUTH -> this.zPower = -this.zPower;
            case WEST, EAST -> this.xPower = -this.xPower;
        }
        this.setDeltaMovement(this.xPower, 0, this.zPower);
    }

    /** [VANILLA COPY] - AbstractHurtingProjectile.hurt(DamageSource, float)
     * The ice crystal needs to move only horizontally when attacked, so yPower isn't copied over.
     */
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            this.markHurt();
            Entity entity = source.getEntity();
            if (entity != null) {
                if (!this.level.isClientSide) {
                    Vec3 vec3 = entity.getLookAngle();
                    this.xPower = vec3.x * 2.5;
                    this.zPower = vec3.z * 2.5;
                    this.setDeltaMovement(xPower, 0, zPower);
                    this.setOwner(entity);
                    this.attacked = true;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * This is needed to make the crystal vulnerable to player attacks.
     */
    @Override
    public boolean isPickable() {
        return true;
    }

    public SoundEvent getImpactExplosionSoundEvent() {
        return AetherSoundEvents.ENTITY_ICE_CRYSTAL_EXPLODE.get();
    }

    @Override
    protected ParticleOptions getExplosionParticle() {
        return AetherParticleTypes.FROZEN.get();
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("XSpeed", this.xPower);
        tag.putDouble("ZSpeed", this.zPower);
        tag.putBoolean("Attacked", this.attacked);
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.xPower = tag.getDouble("XSpeed");
        this.zPower = tag.getDouble("ZSpeed");
        this.attacked = tag.getBoolean("Attacked");
    }
}
