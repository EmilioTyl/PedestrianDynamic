package itba.edu.ar.simulation.test;

import java.io.IOException;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.simulation.SimulationRelaxationTime;
import itba.edu.ar.simulation.output.GranularSimulationEnergy;
import itba.edu.ar.simulation.output.GranularSimulationPositions;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.algorithm.impl.Verlet;

public class RelaxationTime {

	private static final int printAfterNFrames = 100;
	private static final double length = 10;
	private static final double width = 5;
	private static final double height = 5;
	private static final double mass = 0.01;
	private static final String path = System.getProperty("user.dir") + "/";
	private static final double deltaTime = 0.5 * Math.pow(10, -4.5);
	private static final int[] particleQuantities = {100,1000};
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		
		for (int i = 0; i < particleQuantities.length; i++) {

			int particleQuanity = particleQuantities[i];

			SimulationRelaxationTime simulation = new SimulationRelaxationTime(length, width, height, mass, path,particleQuanity);

			GranularSimulationEnergy gse = new GranularSimulationEnergy(path, printAfterNFrames, "particleQuantity-"+particleQuanity);
			GranularSimulationPositions gsp = new GranularSimulationPositions(path, length,width, printAfterNFrames,"particleQuantity-"+particleQuanity);
			
			simulation.subscribe(gse);
			simulation.subscribe(gsp);

			Algorithm<FloatPoint> algorithm = new Verlet(deltaTime);

			simulation.simulate(algorithm, deltaTime);

		}
		
	}
	
	
}
