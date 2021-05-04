import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Main {
	public static final double px = 0.0625;
	public static final double playerWidth = 0.6;

	static PrintWriter jumps;

	static double y;
	static double velX;
	static double velY;
	static int count = 0;
	static double drag;
	static double acceleration;
	static int pxDistPB;
	static double xPB = 1000;
	static int tierPB;
	static double yPB;

	static double potionMultiplier;
	static boolean jumpsFound = false;
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static boolean previouslyAirborne = false;         //If the tick before you jump you were in the air or not (true/false) (this won't matter if InitialSpeed=0)
	//static double initialSpeed = 0.3152845068;                   //Initial speed (when Y=0, not Y=0.42)
	static double initialSpeed = 0.280614;                   //Initial speed (when Y=0, not Y=0.42)
	static double yLimit = -255;                      //How far down you want to check for (in blocks)
	static int speed;                               //Speed Effect
	static int slowness;                            //Slowness effect
	static boolean strafe45 = false;                    //Use 45 strafe (true/false)
	static boolean halfAngle = false;                  //if 45strafe is on, multiply speed by 1.000048
	static double boundary = 0.003;                   // Every jump must be possible by less than what you put that variable to (more than 0.0625 is every tier)
	static double x = initialSpeed;
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static int pxDist = 1;

	public static void main(String[] args) {
		potionMultiplier = ((1 + 0.2 * speed) * (1 - 0.15 * slowness));
		velX = initialSpeed;
		File file = new File("Jumps.txt");
		try {
			file.createNewFile();
			jumps = new PrintWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*Jump pb = null;
		int pbRun = 0;
		for (int i = 0; i < 15; i++) {
			System.out.println(i + " Run Ticks");
			Movement movement = new Movement()
					                    .move(i, false, true, true)
					                    .move(1, true, false, true);
			Jump jump = new Jump(movement.velX, true, false, false, 0, 0);
			jump.movementMultiplier = Movement.MovementMultipliers.SPRINTING;
			jump.calculate();
			if (jump.jumpsFound && (pb == null || jump.xPB + Jump.px < pb.xPB + Jump.px)) {
				pb = jump;
				pbRun = i;
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
		System.out.println(pbRun);
		if (pb != null)
			pb.calculate();

		Movement movement = new Movement()
				                    .move(40, false, true, true)
				                    .resetPos()
				                    .move(1, true, false, true)
				                    .jump(true, false, false)
				                    .moveAir(11, true, false, true);
		System.out.println(movement.lastX+0.6F);*/


		/*Vec3 vel = new Vec3();
		TickList.Input.JumpMovementFactor factor = new TickList.Input.JumpMovementFactor();

		Vec3 pos = TickList.Input.fromInputString("WPJ", 0)
		                         .updatePosWithVel(new Vec3(), vel, factor);

		TickList.Input input = TickList.Input.fromInputString("WPA", 45);
		input.onGround = false;
		for (int i = 0; i < 10; i++) {
			input.updatePosWithVel(pos, vel, factor);
		}
		System.out.println(pos);*/

		Vec3 vel = TickList.findVel(
				new Vec3(0, 0, 1),
				(Vec3 velocity) -> {
					Vec3 pos = TickList.getFinalPos(
							velocity.mult(-1),
							TickList.fromInputString("SJ", 0, 1),
							TickList.fromInputString("S", 0, 11).setOnGround(false),
							TickList.fromInputString("S", 0, 1),
							TickList.fromInputString("WPJ", 0, 1),
							TickList.fromInputString("WPA", 45, 10).setOnGround(false)
					);
					double len = pos.z + 0.6F;
					return Double.compare(2.375 + 1.2, len);
				}
		);

		System.out.println(vel.z);
		List<TickList.TickListInput> inputs = TickList.findInputs(0.0846385499639463, 8, true);
		System.out.println(inputs);

		Vec3 veltest = new Vec3();
		TickList.getFinalPos(veltest, inputs.toArray(new TickList.TickListInput[0]));
		System.out.println(veltest);

		System.out.println(vel);

		Vec3 pos = TickList.getFinalPos(
				veltest.mult(-1),
				TickList.fromInputString("SJ", 0, 1),
				TickList.fromInputString("S", 0, 11).setOnGround(false),
				TickList.fromInputString("WP", 0, 1),
				TickList.fromInputString("WPJ", 0, 1),
				TickList.fromInputString("WPA", 45, 10).setOnGround(false)
		);
		System.out.println(pos);

		/*Vec3 vel2 = new Vec3();
		TickList.getFinalPos(
				vel2,
				TickList.fromInputString("WJ", 180, 1),
				TickList.fromInputString("W", 180, 6).setOnGround(false),
				TickList.fromInputString("WP", 180, 5).setOnGround(false)
		);
		System.out.println(vel2);

		Vec3 pos2 = TickList.getFinalPos(
				vel2,
				TickList.fromInputString("WP", 180, 1),
				TickList.fromInputString("WPJ", 0, 1),
				TickList.fromInputString("WPA", 45, 10).setOnGround(false)
		);
		System.out.println(pos2);*/

		/*Vec3 pos2 = TickList.getFinalPos(
				TickList.TickListInput.fromInputString("SJ", 0, 1),
				TickList.TickListInput.fromInputString("S", 0, 11).setOnGround(false),
				null,
				TickList.TickListInput.fromInputString("S", 0, 1),
				TickList.TickListInput.fromInputString("WPJA", 0, 1),
				TickList.TickListInput.fromInputString("WP", 0, 10).setOnGround(false)
		);
		System.out.println(pos2);*/
	}

	public static void calculate() {
		while (true) {
			while (x < (pxDist * px) - playerWidth) {
				if (count == 0) {
					if (previouslyAirborne) {
						drag = 0.91 * Movement.SlipperinessClass.AIR;
					} else {
						drag = 0.91 * Movement.SlipperinessClass.BLOCK;
					}
					acceleration = 0.1 * 1.3 * 0.98 * potionMultiplier;
					velX = velX * drag + acceleration + 0.2;
					velY = 0.42;
				} else if (count == 1) {
					drag = 0.91 * Movement.SlipperinessClass.BLOCK;

					if (strafe45 && halfAngle) {
						acceleration = 0.02 * 1.3 * 1.000048;
					}

					if (strafe45 && !halfAngle) {
						acceleration = 0.02 * 1.3 * 1;
					}
					if (!strafe45 && !halfAngle) {
						acceleration = 0.02 * 1.3 * 0.98;
					}

					velX = velX * drag + acceleration;
					velY = (velY - 0.08) * 0.98;
				} else {
					drag = 0.91 * Movement.SlipperinessClass.AIR;

					if (strafe45 && halfAngle) {
						acceleration = 0.02 * 1.3 * 1.000048;
					}

					if (strafe45 && !halfAngle) {
						acceleration = 0.02 * 1.3 * 1;
					}
					if (!strafe45 && !halfAngle) {
						acceleration = 0.02 * 1.3 * 0.98;
					}


					velX = velX * drag + acceleration;
					velY = (velY - 0.08) * 0.98;
				}
				x = x + velX;
				y = y + velY;
				count++;
			}
			pxDist++;

			double visualDist = pxDist * px;
			double realDist = visualDist - playerWidth;
			double offset = x - realDist;

			if (offset > 0 && offset < px && offset < boundary) {
				jumpsFound = true;
				System.out.println(" ");
				System.out.println("Poss by: " + offset);
				System.out.println(pxDist + "px (" + visualDist + "b) Tier " + -(count - 11) + "(" + y + "b)");
				jumps.println(" ");
				jumps.println("Poss by: " + offset);
				jumps.println(pxDist + "px " + "(" + visualDist + "b) Tier " + -(count - 11) + " (" + y + "b)");
			}
			if (offset < xPB) {
				xPB = offset;
				pxDistPB = pxDist;
				yPB = y;
				tierPB = count;
			}
			if (y <= yLimit) {
				System.out.println("-----------------Smallest distance:--------------------");
				if (!jumpsFound) {
					System.out.println("No jump poss by less than " + boundary + " found.");
				}
				System.out.println("Poss by: " + (xPB + px));
				System.out.println(
						pxDistPB - 1 + "px " +
								"(" + ((pxDistPB - 1) * px) + "b) Tier " +
								-(tierPB - 11) +
								" (" + yPB + "b)"
				);
				System.out.println("Speed: " + speed + "   Slowness: " + slowness + "   Initial speed: " + initialSpeed + "   45: " + strafe45 + "   Previously Airborne: " + previouslyAirborne + "   Half Angles: " + halfAngle);
				jumps.println("-----------------Smallest distance:--------------------");
				if (!jumpsFound) {
					jumps.println("No jumps poss by less than " + boundary + " found.");
				}
				jumps.println("Poss by: " + (xPB + px));
				jumps.println(pxDistPB - 1 + "px " + "(" + ((pxDistPB - 1) * px) + "b) Tier " + (-(tierPB - 11)) + " (" + yPB + "b)");
				jumps.println("Speed: " + speed + "   Slowness: " + slowness + "   Initial speed: " + initialSpeed + "   45: " + strafe45 + "   Previously Airborne: " + previouslyAirborne + "   Half Angles: " + halfAngle);
				jumps.close();
				break;
			}
			x = initialSpeed;
			y = 0;
			velX = initialSpeed;
			velY = 0;
			count = 0;
		}
	}
}
