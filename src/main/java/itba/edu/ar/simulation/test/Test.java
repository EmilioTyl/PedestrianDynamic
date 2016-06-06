package itba.edu.ar.simulation.test;

import java.io.IOException;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.simulation.PedestrianSimulation;
import itba.edu.ar.simulation.output.GranularSimulationPositions;
import itba.edu.ar.simulation.output.PedestrianEvacuation;
import itba.edu.ar.simulation.output.PedestrianFlow;
import itba.edu.ar.simulation.output.PedestrianFlowUsingWindow;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.algorithm.impl.Verlet;

public class Test {

	private static final int printAfterNFrames = 200;
	private static final double length = 25;
	private static final double[] desiredVelocities = {1};
	private static final double width = 20;
	private static final double destinationY = 5;
	private static final double destinationDiameter = 1.2;
	private static final double mass = 80;
	private static final String path = System.getProperty("user.dir") + "/simulation/";
	private static final double deltaTime = Math.pow(10, -3);
	private static final double finalTime = 300;

	private static final int totalParticles[] = {50};
	private static final int evaluationFrame = 1000;
	private static int windowSize = 5000;
	private static int stepWindow = 500;
	private static int testQuantity = 1;
	private static double lowerRadio = 0.25;
	private static double upperRadio = 0.35;

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		for(int qty: totalParticles){
			for (double desiredVelocity : desiredVelocities) {
				for (int testNumber = 0; testNumber < testQuantity; testNumber++) {
					String tag = "_part_"+qty+"_test_number_" + testNumber + "_desired vel_" + desiredVelocity;
	
					PedestrianSimulation simulation = new PedestrianSimulation(length, width, destinationY,
							destinationDiameter, mass, path, desiredVelocity, qty, lowerRadio, upperRadio);
	
					GranularSimulationPositions gsp = new GranularSimulationPositions(path, length, width,
							printAfterNFrames, tag);
					//PedestrianEvacuation pe = new PedestrianEvacuation(path, tag, qty, evaluationFrame,
					//		destinationY,deltaTime);
					//PedestrianFlow pf = new PedestrianFlow(path, tag, qty, windowSize, destinationY);
					//PedestrianFlowUsingWindow pfuw = new PedestrianFlowUsingWindow(windowSize, stepWindow, destinationY,
					//		deltaTime, path, tag);
	
					simulation.subscribe(gsp);
					//simulation.subscribe(pe);
					//simulation.subscribe(pf);
					//simulation.subscribe(pfuw);
	
					Algorithm<FloatPoint> algorithm = new Verlet(deltaTime);
	
					simulation.simulate(algorithm, deltaTime, finalTime);
	
				}
			}
		}
	}

}
