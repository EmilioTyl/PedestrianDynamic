package itba.edu.ar.simulation.data;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.ss.model.force.Force;

public class ParticleForce implements Force<FloatPoint>{

	private FloatPoint force;
	
	public ParticleForce(FloatPoint force) {
		super();
		this.force = force;
	}

	public FloatPoint getCurrentForce() {
		return force;
	}

	public FloatPoint getPreviousForce() {
		throw new IllegalAccessError();
	}

}
