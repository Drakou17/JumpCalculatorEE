import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TickList {
	public static ArrayList<Input> inputs = new ArrayList<>();

	public static class Input {
		public boolean W, A, S, D;
		public boolean SPRINT, SNEAK, JUMP;
		public float YAW;

		public boolean inWater = false, inLava = false, onLadder = false, inCobweb = false;
		public int jumpBoost = 0;

		public boolean onGround = true;
		public Movement.Slipperiness slipperiness = Movement.Slipperiness.BLOCK;

		public float moveStrafe, moveForward;

		public Input(boolean W, boolean A, boolean S, boolean D, boolean SPRINT, boolean SNEAK, boolean JUMP, float YAW) {
			this.W = W;
			this.A = A;
			this.S = S;
			this.D = D;
			this.SPRINT = SPRINT && W;
			this.SNEAK = SNEAK;
			this.JUMP = JUMP;
			this.YAW = YAW;

			this.moveStrafe = 0F;
			this.moveForward = 0F;

			if (W) moveForward++;
			if (S) moveForward--;
			if (A) moveStrafe++;
			if (D) moveStrafe--;

			if (SNEAK) {
				moveStrafe = (float) ((double) moveStrafe * 0.3D);
				moveForward = (float) ((double) moveForward * 0.3D);
			}

			if (moveForward < 0.8F) this.SPRINT = false;
		}

		public static Input fromInputString(String inputString, float yaw) {
			List<Character> inputs = inputString.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
			return new Input(
					inputs.contains('W'),
					inputs.contains('A'),
					inputs.contains('S'),
					inputs.contains('D'),
					inputs.contains('P'),
					inputs.contains('N'),
					inputs.contains('J'),
					yaw
			);
		}

		private static void moveFlying(Vec3 vel, float strafe, float forward, float friction, float YAW) {
			float speed = strafe * strafe + forward * forward;
			if (speed >= 1.0E-4F) {
				speed = MinecraftMathHelper.sqrt_float(speed);

				if (speed < 1.0F) {
					speed = 1.0F;
				}

				speed = friction / speed;
				strafe = strafe * speed;
				forward = forward * speed;
				float sin = MinecraftMathHelper.sin(YAW * (float) Math.PI / 180.0F);
				float cos = MinecraftMathHelper.cos(YAW * (float) Math.PI / 180.0F);
				vel.x += strafe * cos - forward * sin;
				vel.z += forward * cos + strafe * sin;
			}
		}

		public Vec3 updatePosWithVel(Vec3 pos, Vec3 vel, JumpMovementFactor jumpMovementFactor) {
			if (Math.abs(vel.x) < 0.005D) vel.x = 0.0D;
			if (Math.abs(vel.y) < 0.005D) vel.y = 0.0D;
			if (Math.abs(vel.y) < 0.005D) vel.x = 0.0D;

			if (this.JUMP) {
				if (this.inWater)
					vel.y += 0.03999999910593033D;
				else if (this.inLava)
					vel.y += 0.03999999910593033D;
				else {
					vel.y = 0.42F;
					if (jumpBoost != 0) vel.y += (float) (this.jumpBoost + 1) * 0.1F;

					if (this.SPRINT) {
						float f = YAW * 0.017453292F;
						vel.x -= MinecraftMathHelper.sin(f) * 0.2F;
						vel.z += MinecraftMathHelper.cos(f) * 0.2F;
					}
				}
			}

			float moveStrafe = this.moveStrafe * 0.98F;
			float moveForward = this.moveForward * 0.98F;

			if (!inWater) {
				if (!inLava) {
					float f = 0.91F;
					if (onGround) f = slipperiness.value * 0.91F;
					float f1 = 0.16277136F / (f * f * f);

					float friction;

					if (onGround) {
						friction = (SPRINT ? 0.130000010133F : 0.1F) * f1;
					} else {
						friction = jumpMovementFactor.jumpMovementFactor;
					}

					moveFlying(vel, moveStrafe, moveForward, friction, YAW);

					if (onLadder) {
						float maxLadderMoveSpeed = 0.15F;
						vel.x = MinecraftMathHelper.clamp_double(vel.x, -maxLadderMoveSpeed, maxLadderMoveSpeed);
						vel.z = MinecraftMathHelper.clamp_double(vel.z, -maxLadderMoveSpeed, maxLadderMoveSpeed);

						if (vel.y < -0.15D) {
							vel.y = -0.15D;
						}

						if (vel.y < 0.0D) {
							vel.y = 0.0D;
						}
					}

					if (inCobweb) {
						vel.x *= 0.25D;
						vel.x *= 0.05000000074505806D;
						vel.z *= 0.25D;
					}

					pos.add(vel);

					if (inCobweb) {
						vel.x = 0;
						vel.y = 0;
						vel.z = 0;
					}

					vel.y -= 0.08D;
					vel.y *= 0.9800000190734863D;

					float acc = 0.91F;
					if (onGround) acc = slipperiness.value * 0.91F;

					vel.x *= acc;
					vel.z *= acc;
				} else {
					moveFlying(vel, moveStrafe, moveForward, 0.02F, YAW);
					if (inCobweb) {
						vel.x *= 0.25D;
						vel.x *= 0.05000000074505806D;
						vel.z *= 0.25D;
					}

					pos.add(vel);

					if (inCobweb) {
						vel.x = 0;
						vel.y = 0;
						vel.z = 0;
					}

					vel.x *= 0.5D;
					vel.y *= 0.5D;
					vel.z *= 0.5D;
					vel.y -= 0.02D;
				}
			} else {
				moveFlying(vel, moveStrafe, moveForward, 0.02F, YAW);

				if (inCobweb) {
					vel.x *= 0.25D;
					vel.x *= 0.05000000074505806D;
					vel.z *= 0.25D;
				}

				pos.add(vel);

				if (inCobweb) {
					vel.x = 0;
					vel.y = 0;
					vel.z = 0;
				}

				vel.x *= 0.8F;
				vel.y *= 0.800000011920929D;
				vel.z *= 0.8F;
				vel.y -= 0.02D;
			}

			jumpMovementFactor.jumpMovementFactor = 0.02F;

			if (SPRINT) {
				jumpMovementFactor.jumpMovementFactor = (float) ((double) jumpMovementFactor.jumpMovementFactor + (double) 0.02F * 0.3D);
			}

			return pos;
		}

		public static class JumpMovementFactor {
			float jumpMovementFactor = 0.02F;
		}
	}
}
