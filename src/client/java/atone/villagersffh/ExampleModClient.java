package atone.villagersffh;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.Random;

public class ExampleModClient implements ClientModInitializer {

	public static final String MOD_ID = "villagersffh";
	public static final Identifier VILLAGER_PARTICLES_ID = new Identifier(MOD_ID, "villager_particle");

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		ClientPlayNetworking.registerGlobalReceiver(VILLAGER_PARTICLES_ID, (client, handler, buf, responseSender) -> {
			// Read packet data on the event loop
			Vector3f villagerPos = buf.readVector3f();
			Random random = new Random();

			client.execute(() -> {
				// Everything in this lambda is run on the render thread
				for (int i = 0; i < 1; ++i) {
					double j = (random.nextDouble() - 0.5);
					double k = (random.nextDouble() - 0.5);

					client.world.addParticle(ParticleTypes.SCRAPE, villagerPos.x + j, villagerPos.y + 1.5 + j, villagerPos.z + k, 0, 0, 0);
				}
			});
		});

	}
}