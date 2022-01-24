package software.bernie.example.client.model.entity;

import net.minecraft.util.Identifier;
import software.bernie.example.entity.BikeEntity;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BikeModel extends AnimatedGeoModel<BikeEntity> {
	@Override
	public Identifier getAnimationResource(BikeEntity entity) {
		return new Identifier(GeckoLib.MOD_ID, "animations/bike.animation.json");
	}

	@Override
	public Identifier getModelResource(BikeEntity entity) {
		return new Identifier(GeckoLib.MOD_ID, "geo/bike.geo.json");
	}

	@Override
	public Identifier getTextureResource(BikeEntity entity) {
		return new Identifier(GeckoLib.MOD_ID, "textures/model/entity/bike.png");
	}
}