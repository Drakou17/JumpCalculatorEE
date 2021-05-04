import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TickList {
	public static ArrayList<TickListInput> inputs = new ArrayList<>();

	private static List<TickListInput> possibleInputs = Arrays.asList(
			new TickListInput(true, false, false, false, true, false, false, 0, 1),
			new TickListInput(true, false, false, false, false, false, false, 0, 1),
			new TickListInput(false, false, true, false, false, false, false, 0, 1),
			new TickListInput(true, false, false, false, false, true, false, 0, 1),
			new TickListInput(false, false, true, false, false, true, false, 0, 1)
	);

	public static Vec3 getFinalPos(TickListInput... inputs) {
		return getFinalPos(new Vec3(), inputs);
	}

	public static Vec3 getFinalPos(Vec3 vel, TickListInput... inputs) {
		return getFinalPos(vel, new Vec3(), inputs);
	}

	public static Vec3 getFinalPos(Vec3 vel, Vec3 pos, TickListInput... inputs) {
		Input.JumpMovementFactor factor = new Input.JumpMovementFactor();
		for (TickListInput input : inputs) {
			if (input == null) {
				pos = new Vec3();
				continue;
			}
			input.updatePosWithVel(pos, vel, factor);
		}
		return pos;
	}

	private static double getVelOfInputs(int[] inputs) {
		Input.JumpMovementFactor factor = new Input.JumpMovementFactor();
		Vec3 vel = new Vec3();
		Vec3 pos = new Vec3();
		for (int i : inputs) {
			if (i == -1) continue;
			possibleInputs.get(i).updatePosWithVel(pos, vel, factor);
		}
		return vel.z;
	}

	public static int[] incrementInputs(int[] inputs) {
		int[] inputsTry = incrementInputsTry(inputs);
		if (inputsTry == null) return null;
		while (!isValidInputs(inputsTry)) {
			inputsTry = incrementInputsTry(inputsTry);
			if (inputsTry == null) return null;
		}
		return inputsTry;
	}

	public static int[] incrementInputsTry(int[] inputs) {
		inputs[0]++;
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i] >= possibleInputs.size()) {
				if (i == inputs.length - 1)
					return null;
				inputs[i + 1]++;
				inputs[i] = 0;
			}
		}
		return inputs;
	}

	private static int[] findInputsBruteForce(double velocity, int ticks, boolean over) {
		double pb = over ? 10 : 0;
		int[] bestInputs = null;

		int[] inputs = new int[ticks];
		Arrays.fill(inputs, -1);

		while (inputs != null) {
			double vel = getVelOfInputs(inputs);
			if (!over && (vel > pb && vel <= velocity) || over && (vel < pb && vel >= velocity)) {
				pb = vel;
				bestInputs = Arrays.copyOf(inputs, inputs.length);
			}

			inputs = incrementInputs(inputs);
		}

		System.out.println(pb);
		return bestInputs;
	}

	public static List<TickListInput> findInputs(double velocity, int ticks, boolean over) {
		int[] inputs = findInputsBruteForce(Math.abs(velocity), ticks, over);
		System.out.println(Arrays.toString(inputs));
		List<TickListInput> tickListInputs = new ArrayList<>();
		for (int i : inputs) {
			if (i == -1) break;
			tickListInputs.add(possibleInputs.get(i));
		}
		return tickListInputs;
	}

	public static Vec3 findVel(Vec3 direction, Tester tester) {
		double vel = findVelRecursive(direction.copy(), -50, 50, tester, 0);
		return direction.copy().mult(vel);
	}

	public static double findVelRecursive(Vec3 direction, double minVel, double maxVel, Tester tester, int depth) {
		if (depth > 100) return maxVel;
		else if (minVel == maxVel) return maxVel;

		double midVel = (minVel + maxVel) / 2;
		int test = tester.test(direction.copy().mult(midVel));
		if (test < 0) {
			minVel = midVel;
		} else if (test > 0) {
			maxVel = midVel;
		} else {
			return midVel;
		}
		return findVelRecursive(direction, minVel, maxVel, tester, depth + 1);
	}

	public static TickListInput fromInputString(String inputString, float yaw, int count) {
		return TickListInput.fromInputString(inputString, yaw, count);
	}

	private static boolean isValidInputs(int[] inputs) {
		for (int i = 0; i < inputs.length - 1; i++) {
			if (inputs[i] == 0 && inputs[i + 1] == 1)
				return false;
		}
		return true;
	}

	public interface Tester {
		int test(Vec3 vec3);
	}

	public static class TickListInput extends Input {
		public int count;

		public TickListInput(boolean W, boolean A, boolean S, boolean D, boolean SPRINT, boolean SNEAK, boolean JUMP, float YAW, int count) {
			super(W, A, S, D, SPRINT, SNEAK, JUMP, YAW);
			this.count = count;
		}

		public static TickListInput fromInputString(String inputString, float yaw, int count) {
			List<Character> inputs = inputString.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
			return new TickListInput(
					inputs.contains('W'),
					inputs.contains('A'),
					inputs.contains('S'),
					inputs.contains('D'),
					inputs.contains('P'),
					inputs.contains('N'),
					inputs.contains('J'),
					yaw,
					count
			);
		}

		@Override
		public Vec3 updatePosWithVel(Vec3 pos, Vec3 vel, JumpMovementFactor jumpMovementFactor) {
			for (int i = 0; i < count; i++) {
				super.updatePosWithVel(pos, vel, jumpMovementFactor);
			}
			return pos;
		}

		@Override
		public TickListInput setOnGround(boolean onGround) {
			super.setOnGround(onGround);
			return this;
		}
	}

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

		public Input setOnGround(boolean onGround) {
			this.onGround = onGround;
			return this;
		}

		public Vec3 undoMomentum(Vec3 vel) {
			if (!inWater) {
				if (!inLava) {
					float acc = 0.91F;
					if (onGround) acc = slipperiness.value * 0.91F;

					vel.x /= acc;
					vel.z /= acc;
				} else {
					vel.x /= 0.5D;
					vel.z /= 0.5D;
				}
			} else {
				vel.x /= 0.8F;
				vel.z /= 0.8F;
			}
			return vel;
		}

		public Vec3 updatePosWithVel(Vec3 pos, Vec3 vel, JumpMovementFactor jumpMovementFactor) {
			if (Math.abs(vel.x) < 0.005D) vel.x = 0.0D;
			if (Math.abs(vel.y) < 0.005D) vel.y = 0.0D;
			if (Math.abs(vel.z) < 0.005D) vel.z = 0.0D;

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
						vel.y *= 0.05000000074505806D;
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
						vel.y *= 0.05000000074505806D;
						vel.z *= 0.25D;
					}

					pos.add(vel);

					if (inCobweb) {
						vel.x = 0;
						vel.y = 0;
						vel.z = 0;
					}

					vel.y *= 0.5D;
					vel.y -= 0.02D;

					vel.x *= 0.5D;
					vel.z *= 0.5D;
				}
			} else {
				moveFlying(vel, moveStrafe, moveForward, 0.02F, YAW);

				if (inCobweb) {
					vel.x *= 0.25D;
					vel.y *= 0.05000000074505806D;
					vel.z *= 0.25D;
				}

				pos.add(vel);

				if (inCobweb) {
					vel.x = 0;
					vel.y = 0;
					vel.z = 0;
				}

				vel.y *= 0.800000011920929D;
				vel.y -= 0.02D;

				vel.x *= 0.8F;
				vel.z *= 0.8F;
			}

			jumpMovementFactor.jumpMovementFactor = 0.02F;

			if (SPRINT) {
				jumpMovementFactor.jumpMovementFactor = (float) ((double) jumpMovementFactor.jumpMovementFactor + (double) 0.02F * 0.3D);
			}

			return pos;
		}

		@Override
		public String toString() {
			return "Input[" +
					       (W ? "W" : "") +
					       (A ? "A" : "") +
					       (S ? "S" : "") +
					       (D ? "D" : "") +
					       (SPRINT ? "P" : "") +
					       (SNEAK ? "N" : "") +
					       (JUMP ? "J" : "") +
					       ", " +
					       YAW +
					       "]";
		}

		public static class JumpMovementFactor {
			float jumpMovementFactor = 0.02F;
		}
	}
}
