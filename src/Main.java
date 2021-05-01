import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
	public static double px = 0.0625;
	public static double playerWidth = 0.6;
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
	static boolean previouslyAirborne = true;         //If the tick before you jump you were in the air or not (true/false) (this won't matter if InitialSpeed=0)
	static double initialSpeed = 0.3152845068;                   //Initial speed (when Y=0, not Y=0.42)
	static double yLimit = -255;                      //How far down you want to check for (in blocks)
	static int speed;                               //Speed Effect
	static int slowness;                            //Slowness effect
	static boolean strafe45 = true;                    //Use 45 strafe (true/false)
	static boolean halfAngle = true;                  //if 45strafe is on, multiply speed by 1.000048
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
		calculate();
	}

	public static void calculate() {
		while (true) {
			while (x < (pxDist * px) - playerWidth) {
				if (count == 0) {
					if (previouslyAirborne) {
						drag = 0.91 * Slipperiness.AIR;
					} else {
						drag = 0.91 * Slipperiness.BLOCK;
					}
					acceleration = 0.1 * 1.3 * 0.98 * potionMultiplier;
					velX = velX * drag + acceleration + 0.2;
					velY = 0.42;
				} else if (count == 1) {
					drag = 0.91 * Slipperiness.BLOCK;

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
					drag = 0.91 * Slipperiness.AIR;

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
			if (x - (((pxDist) * px) - playerWidth) > 0 && x - (((pxDist) * px) - playerWidth) < px && x - (((pxDist) * px) - playerWidth) < boundary) {
				jumpsFound = true;
				System.out.println(" ");
				System.out.println("Poss by: " + (x - (((pxDist) * px) - playerWidth)));
				System.out.println(pxDist + "px (" + (pxDist * px) + "b) Tier " + -(count - 11) + "(" + y + "b)");
				jumps.println(" ");
				jumps.println("Poss by: " + (x - (((pxDist) * px) - playerWidth)));
				jumps.println(pxDist + "px " + "(" + (pxDist * px) + "b) Tier " + -(count - 11) + " (" + y + "b)");
			}
			if (x - (((pxDist) * px) - playerWidth) < xPB) {
				xPB = x - (((pxDist) * px) - playerWidth);
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

	public static class Slipperiness {
		public static final double AIR = 1;
		public static final double BLOCK = 0.6;
	}
}
