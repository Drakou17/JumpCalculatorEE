public class Movement {
	private static final double SQRT_2 = Math.sqrt(2);
	public double velX, velY, velZ;
	public double x, y, z;
	public double yaw = 0;
	public double lastX, lastY, lastZ;

	public Slipperiness currentSlipperiness = Slipperiness.BLOCK;
	private Slipperiness lastSlipperiness = Slipperiness.BLOCK;
	private int speed = 0;
	private int slowness = 0;
	private double potionMultiplier = 1;

	public Movement setPotions(int speed, int slowness) {
		this.speed = speed;
		this.slowness = slowness;
		this.potionMultiplier = ((1 + 0.2 * speed) * (1 - 0.15 * slowness));
		return this;
	}

	private Movement prepareNext() {
		this.lastSlipperiness = currentSlipperiness;
		return this;
	}

	private void beforeNext() {
		this.lastX = this.x;
		this.lastY = this.y;
		this.lastZ = this.z;
	}

	public Movement move(int ticks, boolean sprint, boolean sneak, boolean strafe) {
		double movementMultiplier = 1;
		if (sprint) movementMultiplier *= MovementMultipliers.SPRINTING;
		if (sneak) movementMultiplier *= MovementMultipliers.SNEAKING;

		if (!strafe) movementMultiplier *= StrafingMultipliers.DEFAULT;
		else if (sneak) movementMultiplier *= StrafingMultipliers.SNEAK_STRAFE;

		for (int i = 0; i < ticks; i++) {
			beforeNext();

			double currSlip = 0.6 / currentSlipperiness.value;
			double acceleration = 0.1 * movementMultiplier * potionMultiplier * currSlip * currSlip * currSlip;

			velX = getMomentumX() + acceleration;
			x += velX;

			prepareNext();
		}
		return this;
	}

	public Movement moveAir(int ticks, boolean sprint, boolean sneak, boolean strafe) {
		if (currentSlipperiness != Slipperiness.AIR)
			currentSlipperiness = Slipperiness.AIR;

		double movementMultiplier = 1;
		if (sprint) movementMultiplier *= MovementMultipliers.SPRINTING;
		if (sneak) movementMultiplier *= MovementMultipliers.SNEAKING;

		if (!strafe) movementMultiplier *= StrafingMultipliers.DEFAULT;
		else if (sneak) movementMultiplier *= StrafingMultipliers.SNEAK_STRAFE;


		for (int i = 0; i < ticks; i++) {
			beforeNext();

			double acceleration = 0.02 * movementMultiplier;

			velX = getMomentumX() + acceleration;
			velY = (velY - 0.08) * 0.98;
			if (Math.abs(velY) < 0.005)
				velY = 0;

			x += velX;
			y += velY;

			prepareNext();
		}
		return this;
	}

	public Movement jump(boolean sprint, boolean sneak, boolean strafe) {
		beforeNext();

		double movementMultiplier = 1;
		if (sprint) movementMultiplier *= MovementMultipliers.SPRINTING;
		if (sneak) movementMultiplier *= MovementMultipliers.SNEAKING;

		if (!strafe) movementMultiplier *= StrafingMultipliers.DEFAULT;
		else if (sneak) movementMultiplier *= StrafingMultipliers.SNEAK_STRAFE;

		double currSlip = 0.6 / currentSlipperiness.value;
		double acceleration = 0.1 * movementMultiplier * potionMultiplier * currSlip * currSlip * currSlip;
		if (sprint) acceleration += 0.2;

		velX = getMomentumX() + acceleration;
		velY = 0.42;
		x += velX;
		y += velY;

		return prepareNext();
	}

	@Override
	public String toString() {
		return "Movement [x: " + x + ", y: " + y + ", z: " + z + ", velX: " + velX + ", velY: " + velY + ", velZ: " + velZ;
	}

	public String toStringLast() {
		return "Movement [x: " + lastX + ", y: " + lastY + ", z: " + lastZ + ", velX: " + velX + ", velY: " + velY + ", velZ: " + velZ;
	}

	public Movement resetPos() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		return this;
	}

	private double getMomentumX() {
		double momentum = velX * getDrag();
		if (Math.abs(momentum) < 0.005) return 0;
		return momentum;
	}

	private double getMomentumZ() {
		double momentum = velZ * getDrag();
		if (Math.abs(momentum) < 0.005) return 0;
		return momentum;
	}

	private double getDrag() {
		return 0.91 * lastSlipperiness.value;
	}

	public enum Slipperiness {
		AIR(1F),
		BLOCK(0.6F),
		SLIME(0.8F),
		ICE(0.98F);

		public final float value;

		Slipperiness(float value) {
			this.value = value;
		}
	}

	public static class SlipperinessClass {
		public static final double AIR = 1;
		public static final double BLOCK = 0.6;
	}

	public static class MovementMultipliers {
		public static final double SPRINTING = 1.3;
		public static final double WALKING = 1.0;
		public static final double SNEAKING = 0.3;
		public static final double STOPPING = 0.0;
	}

	public static class StrafingMultipliers {
		public static final double DEFAULT = 0.98;
		public static final double STRAFE = 1;
		public static final double SNEAK_STRAFE = 0.98 * SQRT_2;
	}
}
