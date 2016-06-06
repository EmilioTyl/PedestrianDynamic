package itba.edu.ar.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.management.DescriptorRead;

import itba.edu.ar.cellIndexMethod.IndexMatrix;
import itba.edu.ar.cellIndexMethod.IndexMatrixBuilder;
import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.input.file.CellIndexMethodFileGenerator;
import itba.edu.ar.input.file.data.Data;
import itba.edu.ar.simulation.data.GranularParticle;
import itba.edu.ar.simulation.data.ParticleForce;
import itba.edu.ar.simulation.data.PedestrianData;
import itba.edu.ar.simulation.data.SiloData;
import itba.edu.ar.simulation.model.Wall;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.model.entity.Entity;
import itba.edu.ar.ss.model.force.Force;

public class PedestrianSimulation {

	private List<Wall> walls = new ArrayList<Wall>();
	private List<Data> datas = new LinkedList<Data>();
	private String path;
	private List<SimulationObserver> subscribers = new LinkedList<SimulationObserver>();
	private double desiredVelocity = 0;
	private static final double NORMAL_CONSTANT = 1.2 * Math.pow(10, 5);
	private static final double A_CONSTANT = 2000;
	private static final double B_CONSTANT = 0.08;
	private static final double T_CONSTANT = 0.5;

	private double destinationLowerLimitX;
	private double destinationUpperLimitX;
	private double destinationY;
	private PedestrianData pedestrianData;
	
	public PedestrianSimulation(double length, double width, double destinationY, double diameter, double mass, String path,
			double desiredVelocity, int totalParticles,double lowerRadio,double upperRadio) {

		this.path = path;
		this.desiredVelocity = desiredVelocity;

		double bottomLength = (width - diameter) / 2;

		walls.add(new Wall(new FloatPoint(0, destinationY), new FloatPoint(0, destinationY + length)));
		walls.add(new Wall(new FloatPoint(0, length), new FloatPoint(width, length)));
		walls.add(new Wall(new FloatPoint(width, destinationY), new FloatPoint(width, length)));

		walls.add(new Wall(new FloatPoint(0, destinationY), new FloatPoint(bottomLength, destinationY)));
		walls.add(new Wall(new FloatPoint(width - bottomLength, destinationY), new FloatPoint(width, destinationY)));

		this.destinationLowerLimitX = bottomLength;
		this.destinationUpperLimitX = bottomLength + diameter;
		this.destinationY = destinationY;
		this.pedestrianData = new PedestrianData(totalParticles, mass, length, destinationY, width,lowerRadio,upperRadio);
		datas.add(pedestrianData);

	}

	public void simulate(Algorithm<FloatPoint> algorithm, double deltaTime, double finalTime)
			throws InstantiationException, IllegalAccessException, IOException {

		List<String> staticPath = new ArrayList<String>();
		List<String> dynamicPath = new ArrayList<String>();

		CellIndexMethodFileGenerator.generate(staticPath, dynamicPath, datas, path,
				((SiloData) datas.get(0)).getLength());

		IndexMatrix indexMatrix = IndexMatrixBuilder.getIndexMatrix(staticPath.get(0), dynamicPath.get(0), 1,
				deltaTime);

		List<Particle> particles = indexMatrix.getParticles();
		
		double time = 0;

		while (particles.size() > 0) {
			time += deltaTime;

			List<Entity<FloatPoint>> granularParticles = getGranularParticles(particles, deltaTime);

			algorithm.evolveSystem(granularParticles, getForces(indexMatrix.getParticles()), deltaTime);

			removeOutsiders(particles);

			notifyStepEnded(particles, time);
		}

		notifySimulationEnded(time);

	}

	private void notifySimulationEnded(double time) throws IOException {
		for (SimulationObserver so : subscribers)
			so.simulationEnded();
	}

	private void notifyStepEnded(List<Particle> particles, double time) throws IOException {
		for (SimulationObserver so : subscribers)
			so.stepEnded(particles, time);
	}

	private List<Entity<FloatPoint>> getGranularParticles(List<Particle> particles, double deltaTime) {
		List<Entity<FloatPoint>> granularParticles = new LinkedList<Entity<FloatPoint>>();
		for (Particle particle : particles) {
			granularParticles.add(new GranularParticle(particle, deltaTime));
		}
		return granularParticles;
	}

	private void removeOutsiders(List<Particle> particles) {
		Iterator<Particle> iter = particles.iterator();
		while (iter.hasNext()) {
			Particle particle = iter.next();
			FloatPoint position = particle.getPosition();

			if (outOfBorders(position) || evacuated(position)) {
				iter.remove();
			}
		}
	}

	private boolean evacuated(FloatPoint position) {
		return position.getY() <= destinationY;
	}

	private boolean outOfBorders(FloatPoint position) {
		return position.getY() < 0 || position.getX() < 0 || position.getX() > ((SiloData) datas.get(0)).getWidth()
				|| position.getY() > ((SiloData) datas.get(0)).getLength();
	}

	private List<Force<FloatPoint>> getForces(List<Particle> particles) {
		List<Force<FloatPoint>> forces = new LinkedList<Force<FloatPoint>>();

		for (Particle particle : particles) {
			FloatPoint totalForce = getDesiredForce(particle);
			
			for (Particle neighbour : particles) {
				if (!neighbour.equals(particle)) {
					
					Double overlap = getOverlap(particle, neighbour);
					

					FloatPoint normalVersor = getNormalVersor(particle, neighbour);
					FloatPoint tangencialVersor = normalVersor.rotateRadiants(Math.PI / 2);

					Double relativeVelocity = particle.getVelocity().minus(neighbour.getVelocity())
							.multiply(tangencialVersor);

					FloatPoint force = getForce(normalVersor, tangencialVersor, relativeVelocity, overlap);
					totalForce = totalForce.plus(force);
				}
			}
			totalForce = wallCollision(particle, totalForce);
			forces.add(new ParticleForce(totalForce));
		}
		return forces;
	}

	private FloatPoint getDesiredForce(Particle particle) {
		FloatPoint destination = new FloatPoint(getDestinationX(particle), destinationY);	
		FloatPoint desiredVersor = destination.minus(particle.getPosition());
		desiredVersor = desiredVersor.divide(desiredVersor.abs());
		FloatPoint desiredForce = desiredVersor.multiply(desiredVelocity).minus(particle.getVelocity())
				.multiply(particle.getMass() / T_CONSTANT);
		return desiredForce;
	}
	
	private double getDestinationX(Particle particle) {
		Double x = particle.getPosition().getX();

		if (x <= destinationLowerLimitX + 2*pedestrianData.getMaxRadio() ) {
			return destinationLowerLimitX + 2*pedestrianData.getMaxRadio();
		} else if (x >= destinationUpperLimitX - 2*pedestrianData.getMaxRadio()) {
			return destinationUpperLimitX- 2*pedestrianData.getMaxRadio();
		} else {
			return x;
		}
	}

	public FloatPoint getForce(FloatPoint normalVersor, FloatPoint tangencialVersor, double relativeVelocity,
			double overlap) {

		FloatPoint socialRepulsion = normalVersor.multiply((-1) * A_CONSTANT * Math.exp(overlap / B_CONSTANT));
		FloatPoint force = socialRepulsion;
		
		if (overlap >= 0) {
			FloatPoint normalForce = normalVersor.multiply(-1 * getConstantNormal() * overlap);
			FloatPoint tangencialForce = tangencialVersor
					.multiply(-1 * getConstantTangencial() * overlap * relativeVelocity);
			force = force.plus(normalForce.plus(tangencialForce));
		}

		return force;

	}

	private FloatPoint wallCollision(Particle particle, FloatPoint totalForce) {
		for (Wall wall : walls) {
			FloatPoint tangencialVersor = wall.getTangencialVersor();
			FloatPoint normalVersor = wall.getNormalVersor();

			double overlap = particle.getRadio() - Math.abs(Math.abs(particle.getPosition().multiply(normalVersor))
					- Math.abs(wall.getPositionOne().multiply(normalVersor)));
			if (wall.isCollision(particle, overlap)) {
				FloatPoint force = getForce(normalVersor.multiply(-1), tangencialVersor,
						particle.getVelocity().multiply(tangencialVersor), overlap);
				totalForce = totalForce.plus(force);
			}

		}

		return totalForce;
	}

	private double getConstantTangencial() {
		return 2 * getConstantNormal();
	}

	private double getConstantNormal() {
		return NORMAL_CONSTANT;
	}

	private double getOverlap(Particle particle, Particle neighbour) {

		return particle.getRadio() + neighbour.getRadio() - neighbour.getPosition().minus(particle.getPosition()).abs();
	}

	private FloatPoint getNormalVersor(Particle particle, Particle neighbour) {
		FloatPoint vector = neighbour.getPosition().minus(particle.getPosition());
		return vector.divide(vector.abs());
	}

	public void subscribe(SimulationObserver gsp) {
		subscribers.add(gsp);
	}
}
