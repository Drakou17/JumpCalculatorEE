public class Vec3 {
	public double x, y, z;

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3() {
		this(0, 0, 0);
	}

	public Vec3 add(Vec3 v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
		return this;
	}

	public Vec3 copy() {
		return new Vec3(this.x, this.y, this.z);
	}

	@Override
	public String toString() {
		return "[x: " + this.x + ", y: " + this.y + ", z: " + this.z + "]";
	}
}
