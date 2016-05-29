package itba.edu.ar.simulation.data;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.ss.model.entity.Entity;

public class GranularParticle implements Entity<FloatPoint>{

	private Particle particle;
	private FloatPoint previousPosition;
	
	public GranularParticle(Particle particle,double deltaTime) {
		super();
		this.particle = particle;
		this.previousPosition = particle.getPosition().minus(particle.getVelocity().multiply(deltaTime));
	}
	
	
	public FloatPoint getPosition() {
		return particle.getPosition();
	}

	public double getMass() {
		return particle.getMass();
	}

	public FloatPoint getVelocity() {
		return particle.getVelocity();
	}

	public void setPosition(FloatPoint position) {
		previousPosition = getPosition();
		particle.setPosition(position);
	}

	public void setVelocity(FloatPoint velocity) {
		particle.setVelocity(velocity);
	}

	public FloatPoint getPreviousPosition() {
		return previousPosition;
	}

	
	
}