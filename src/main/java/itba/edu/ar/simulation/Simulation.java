package itba.edu.ar.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import itba.edu.ar.cellIndexMethod.CellIndexMethod;
import itba.edu.ar.cellIndexMethod.IndexMatrix;
import itba.edu.ar.cellIndexMethod.IndexMatrixBuilder;
import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.cellIndexMethod.route.Route;
import itba.edu.ar.cellIndexMethod.route.routeImpl.OptimizedRoute;
import itba.edu.ar.input.file.CellIndexMethodFileGenerator;
import itba.edu.ar.input.file.data.Data;
import itba.edu.ar.simulation.data.GranularParticle;
import itba.edu.ar.simulation.data.ParticleForce;
import itba.edu.ar.simulation.data.SiloData;
import itba.edu.ar.simulation.model.Wall;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.model.entity.Entity;
import itba.edu.ar.ss.model.force.Force;

public class Simulation {


	private List<Wall> walls = new ArrayList<Wall>();
	private List<Data> datas = new LinkedList<Data>();
	private String path;
	private List<SimulationObserver> subscribers = new LinkedList<SimulationObserver>();
	private FloatPoint destination = null;
	private double desiredVelocity = 0;
	private static final double NORMAL_CONSTANT = 1.2 *  Math.pow(10, 5);
	private static final double A_CONSTANT =  2 * Math.pow(10, 3);
	private static final double B_CONSTANT =  0.08;
	private static final double T_CONSTANT =  0.5;



	public Simulation(double length, double width, double height, double diameter, double mass, String path, double desiredVelocity, FloatPoint destination, int totalParticles) {

		this.path = path;
		this.desiredVelocity = desiredVelocity;
		this.destination = destination;

		double bottomLength = (width - diameter) / 2;

		walls.add(new Wall(new FloatPoint(0, height), new FloatPoint(0, height + length)));
		walls.add(new Wall(new FloatPoint(0, length), new FloatPoint(width, length)));
		walls.add(new Wall(new FloatPoint(width, height), new FloatPoint(width, length)));

		walls.add(new Wall(new FloatPoint(0, height), new FloatPoint(bottomLength, height)));
		walls.add(new Wall(new FloatPoint(width - bottomLength, height), new FloatPoint(width, height)));

		double particleRadio = 0.35;   // Change it for a random radio between [0.25, 0.35]
		int particleQuantity = totalParticles; 
		
		datas.add(new SiloData(particleQuantity, mass, particleRadio, length, height, width));

	}

	public void simulate(Algorithm<FloatPoint> algorithm, double deltaTime, double finalTime)
			throws InstantiationException, IllegalAccessException, IOException {

		List<String> staticPath = new ArrayList<String>();
		List<String> dynamicPath = new ArrayList<String>();

		CellIndexMethodFileGenerator.generate(staticPath, dynamicPath, datas, path,
				((SiloData) datas.get(0)).getLength());

		IndexMatrix indexMatrix = IndexMatrixBuilder.getIndexMatrix(staticPath.get(0), dynamicPath.get(0),
				getCellQuantity((SiloData) datas.get(0)), deltaTime);

		List<Particle> particles = indexMatrix.getParticles();
		System.out.println("Particle quantity: " + particles.size());
		double time = 0;			
		while(particles.size() > 0){
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
			so.simulationEnded(time);
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
			
			if (outOfBorders(position) || evacuated(position))
			{
				iter.remove();
			}
		}
	}

	private boolean evacuated(FloatPoint position){
		return position.getY() <= destination.getY();
	}
	
	private boolean outOfBorders(FloatPoint position) {
		return position.getY() < 0 || position.getX() < 0 || position.getX() > ((SiloData) datas.get(0)).getWidth() || position.getY() > ((SiloData) datas.get(0)).getLength() ;
	}

	private List<Force<FloatPoint>> getForces(List<Particle> particles) {
		List<Force<FloatPoint>> forces = new LinkedList<Force<FloatPoint>>();

		for (Particle particle : particles) {		
			FloatPoint totalForce = getDesiredForce(particle); 
			for (Particle neighbour : particles) {
				
				if(!neighbour.equals(particle)){
					Double overlap = getOverlap(particle, neighbour);
					
					FloatPoint normalVersor = getNormalVersor(particle, neighbour);
					FloatPoint tangencialVersor = normalVersor.rotateRadiants(Math.PI/2);

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
	
	private FloatPoint getDesiredForce(Particle particle){
		FloatPoint desiredVersor = destination.minus(particle.getPosition());
		FloatPoint desiredForce =  desiredVersor.multiply(desiredVelocity).minus(particle.getVelocity()).multiply(particle.getMass()/T_CONSTANT);
		return desiredForce;
	}

	public FloatPoint getForce(FloatPoint normalVersor, FloatPoint tangencialVersor, double relativeVelocity,
			double overlap) {
		
		FloatPoint socialRepulsion = normalVersor.multiply((-1)*A_CONSTANT*Math.exp(overlap/B_CONSTANT));
		FloatPoint force = socialRepulsion;
		if(overlap >= 0){
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
			
			double overlap = particle.getRadio() - Math.abs(Math.abs(particle.getPosition().multiply(normalVersor)) - Math.abs(wall.getPositionOne().multiply(normalVersor)));
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

	private Route getRoute(SiloData data) {
		return new OptimizedRoute(getCellQuantity(data), false, ((SiloData) datas.get(0)).getLength());
	}

	private int getCellQuantity(SiloData data) {
		return (int) Math.ceil((data.getLength() - data.getHeigth()) / (data.getRadio() * 2)) - 1;
	}

	public void subscribe(SimulationObserver gsp) {
		subscribers.add(gsp);
	}
}
