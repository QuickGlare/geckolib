/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package software.bernie.geckolib3.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import software.bernie.geckolib3.model.provider.AbstractGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class AnimationUtils {
	public static double convertTicksToSeconds(double ticks) {
		return ticks / 20;
	}

	public static double convertSecondsToTicks(double seconds) {
		return seconds * 20;
	}

	/**
	 * Gets the renderer for an entity
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Entity> EntityRenderer<T> getRenderer(T entity) {
		EntityRenderDispatcher renderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
		return (EntityRenderer<T>) renderManager.getRenderer(entity);
	}

	@SuppressWarnings("rawtypes")
	public static <T extends Entity> AbstractGeoModel getGeoModelForEntity(T entity) {
		EntityRenderer<T> entityRenderer = getRenderer(entity);

		if (entityRenderer instanceof IGeoRenderer) {
			return ((IGeoRenderer<?>) entityRenderer).getModel();
		}
		return null;
	}
}
