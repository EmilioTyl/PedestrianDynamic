package itba.edu.ar.simulation.test;

import java.io.IOException;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.simulation.Simulation;
import itba.edu.ar.simulation.output.GranularSimulationPositions;
import itba.edu.ar.simulation.output.PedestrianEvacuation;
import itba.edu.ar.simulation.output.PedestrianFlow;
import itba.edu.ar.simulation.output.GranularSimulationEnergy;
import itba.edu.ar.simulation.output.GranularSimulationFlow;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.algorithm.impl.Verlet;

public class Test {

	private static final int printAfterNFrames = 200;
	private static final double length = 25 ;
	private static final double[] desiredVelocity = { 2 };
	private static final double width = 20;
	private static final double height = 5;
	private static final double diameter = 1.2;
	private static final double mass = 80;
	private static final String path = System.getProperty("user.dir") + "/simulation";
	private static final double deltaTime = Math.pow(10, -3);
	private static final double finalTime = 300;
	private static FloatPoint destination = new FloatPoint(10, 5);
	private static final int totalParticles = 100;
	private static final int evaluationFrame = 1000;

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {

		for (int i = 0; i < desiredVelocity.length; i++) {

			double velocity = desiredVelocity[i];

			Simulation simulation = new Simulation(length, width, height, diameter, mass, path, velocity, destination, totalParticles);

			GranularSimulationPositions gsp = new GranularSimulationPositions(path, length,width, printAfterNFrames,"lenght-"+length);
			PedestrianEvacuation pe = new PedestrianEvacuation(path, "desired vel-"+velocity, totalParticles, evaluationFrame, destination);
			PedestrianFlow pf = new PedestrianFlow(path, "desired vel-"+velocity, totalParticles, evaluationFrame, destination);

			simulation.subscribe(gsp);
			simulation.subscribe(pe);
			simulation.subscribe(pf);
			

			Algorithm<FloatPoint> algorithm = new Verlet(deltaTime);

			simulation.simulate(algorithm, deltaTime, finalTime);

		}
	}

}
