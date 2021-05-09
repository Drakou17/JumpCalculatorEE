import java.util.Locale;

public class Jump {
	public static final double px = 0.0625;
	public static final double playerWidth = 0.6;
	public static final double yLimit = -255;
	public static final boolean debug = true;
	public double x, y;
	public int pxDist;
	public double xPB = Double.MAX_VALUE, yPB;
	public int pxDistPB;
	public int tierPB;
	public double initialSpeed;
	public boolean strafe45;
	public boolean halfAngle;
	public boolean previouslyAirborne;
	public int slowness, speed;
	public double potionMultiplier;
	public double velX, velY;
	public double drag;
	public double minOffset = 0.003;
	public boolean jumpsFound = false;
	public double movementMultiplier = Movement.MovementMultipliers.SPRINTING;

	public Jump(double initialSpeed, boolean strafe45, boolean halfAngle, boolean previouslyAirborne, int slowness, int speed) {
		this.strafe45 = strafe45;
		this.halfAngle = halfAngle;
		this.previouslyAirborne = previouslyAirborne;

		this.slowness = slowness;
		this.speed = speed;
		this.potionMultiplier = ((1 + 0.2 * speed) * (1 - 0.15 * slowness));

		this.initialSpeed = initialSpeed;
		resetSimulation();
	}

	public void resetSimulation() {
		x = initialSpeed;
		y = 0;
		velX = initialSpeed;
		velY = 0;
	}

	public int findMinAirtimeForDist(int pxDist) {
		double acceleration = 0;
		int airtime = 0;
		for (; x < (pxDist * px) - playerWidth; airtime++) {
			drag = 0.91 * Movement.SlipperinessClass.BLOCK;
			if (airtime > 1 || airtime == 0 && previouslyAirborne) drag = 0.91 * Movement.SlipperinessClass.AIR;

			if (airtime == 0) {
				acceleration = 0.1 * movementMultiplier * potionMultiplier;
				if (movementMultiplier == Movement.MovementMultipliers.SPRINTING)
					acceleration *= Movement.StrafingMultipliers.DEFAULT;
				velX = velX * drag + acceleration + 0.2;
				velY = 0.42;
			} else {
				if (strafe45 && halfAngle) {
					acceleration = 0.02 * movementMultiplier * 1.000048;
				}

				if (strafe45 && !halfAngle) {
					acceleration = 0.02 * movementMultiplier * Movement.StrafingMultipliers.STRAFE;
				}
				if (!strafe45 && !halfAngle) {
					acceleration = 0.02 * movementMultiplier * Movement.StrafingMultipliers.DEFAULT;
				}

				velX = velX * drag + acceleration;
				velY = (velY - 0.08) * 0.98;
				if (Math.abs(velY) < 0.005)
					velY = 0;
			}
			x = x + velX;
			y = y + velY;
		}
		return airtime;
	}


	public void calculate() {
		while (y > yLimit) {
			resetSimulation();
			int airtime = findMinAirtimeForDist(pxDist);
			pxDist++;

			double visualDist = pxDist * px;
			double realDist = visualDist - playerWidth;
			double offset = x - realDist;

			if (offset > 0 && offset < px && offset < minOffset) {
				jumpsFound = true;
				double vel = velY;
				vel = (vel - 0.08) * 0.98;
				if (Math.abs(vel) < 0.005) vel = 0;

				if (debug) {
					System.out.println(" ");
					System.out.println("Poss by: " + String.format(Locale.US, "%,.16f", offset));
					System.out.println(pxDist + "px (" + visualDist + "b) Tier " + -(airtime - 11) + " (" + y + " - " + (y + vel) + ")");
					System.out.println(Math.floor(y));
				}
			}
			if (offset < xPB) {
				xPB = offset;
				pxDistPB = pxDist;
				yPB = y;
				tierPB = airtime;
			}
		}

		System.out.println("-----------------Smallest distance:--------------------");
		if (!jumpsFound) {
			System.out.println("No jump poss by less than " + minOffset + " found.");
		} else {
			System.out.println("Poss by: " + String.format(Locale.US, "%,.16f", xPB + px));
			System.out.println(
					pxDistPB - 1 + "px " +
							"(" + ((pxDistPB - 1) * px) + "b) " +
							"Tier " + -(tierPB - 11) +
							" Airtime " + tierPB +
							" (" + yPB + "b)"
			);
			System.out.println("Speed: " + speed + "   Slowness: " + slowness + "   Initial speed: " + initialSpeed + "   45: " + strafe45 + "   Previously Airborne: " + previouslyAirborne + "   Half Angles: " + halfAngle);
		}
	}
}
