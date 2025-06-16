package mjf.nmm.entities.ai.goals;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public class PhantomSwoopGoal extends Goal {
		private boolean catsNearby;
		private int nextCatCheckAge;
        private PhantomEntity phantomEntity;


		public PhantomSwoopGoal(PhantomEntity phantomEntity) {
			this.phantomEntity = phantomEntity;
			this.setControls(EnumSet.of(Goal.Control.MOVE));
		}

		protected boolean isNearTarget() {
			return phantomEntity.targetPosition.squaredDistanceTo(phantomEntity.getX(), phantomEntity.getY(), phantomEntity.getZ()) < 4.0;
		}

		@Override
		public boolean canStart() {
			return phantomEntity.getTarget() != null && phantomEntity.movementType == PhantomEntity.PhantomMovementType.SWOOP;
		}

		@Override
		public boolean shouldContinue() {
			LivingEntity livingEntity = phantomEntity.getTarget();
			if (livingEntity == null) {
				return false;
			} else if (!livingEntity.isAlive()) {
				return false;
			} else if (livingEntity instanceof PlayerEntity playerEntity && (livingEntity.isSpectator() || playerEntity.isCreative())) {
				return false;
			} else if (!this.canStart()) {
				return false;
			} else {
				if (phantomEntity.age > this.nextCatCheckAge) {
					this.nextCatCheckAge = phantomEntity.age + 20;
					List<CatEntity> list = phantomEntity.getWorld()
						.getEntitiesByClass(CatEntity.class, phantomEntity.getBoundingBox().expand(16.0), EntityPredicates.VALID_ENTITY);

					for (CatEntity catEntity : list) {
						catEntity.hiss();
					}

					this.catsNearby = !list.isEmpty();
				}

				return !this.catsNearby;
			}
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
			phantomEntity.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = phantomEntity.getTarget();
			if (livingEntity != null) {
				phantomEntity.targetPosition = new Vec3d(livingEntity.getX(), livingEntity.getBodyY(0.5), livingEntity.getZ());
				if (phantomEntity.getBoundingBox().expand(0.2F).intersects(livingEntity.getBoundingBox())) {
					phantomEntity.tryAttack(castToServerWorld(phantomEntity.getWorld()), livingEntity);
					phantomEntity.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
					if (!phantomEntity.isSilent()) {
						phantomEntity.getWorld().syncWorldEvent(WorldEvents.PHANTOM_BITES, phantomEntity.getBlockPos(), 0);
					}
				} else if (phantomEntity.horizontalCollision || phantomEntity.hurtTime > 0) {
					phantomEntity.movementType = PhantomEntity.PhantomMovementType.CIRCLE;
				}
			}
		}
	}
