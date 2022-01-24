package software.bernie.example.client.model.entity;

import net.minecraft.util.Identifier;
import software.bernie.example.entity.RocketProjectile;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RocketModel extends AnimatedGeoModel<RocketProjectile> {
	@Override
	public Identifier getModelResource(RocketProjectile object) {
		return new Identifier(GeckoLib.MOD_ID, "geo/rocket.geo.json");
	}

	@Override
	public Identifier getTextureResource(RocketProjectile object) {
		return new Identifier(GeckoLib.MOD_ID, "textures/entity/projectiles/rocket.png");
	}

	@Override
	public Identifier getAnimationResource(RocketProjectile animatable) {
		return new Identifier(GeckoLib.MOD_ID, "animations/rocket.animation.json");
	}

}
