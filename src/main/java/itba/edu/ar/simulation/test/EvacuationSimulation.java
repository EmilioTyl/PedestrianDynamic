package itba.edu.ar.simulation.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.simulation.PedestrianSimulation;
import itba.edu.ar.simulation.output.EvacuationTime;
import itba.edu.ar.simulation.output.GranularSimulationPositions;
import itba.edu.ar.ss.algorithm.Algorithm;
import itba.edu.ar.ss.algorithm.impl.Verlet;

public class EvacuationSimulation {
	private static final int printAfterNFrames = 200;
	private static final double length = 25 ;
	private static final double initialDesiredVelocity = 0.5;
	private static final double deltaVelocity = 0.5;
	private static final double finalDesiredVelocity = 10;
	private static final double width = 20;
	private static final double destinationDiameter = 1.2;
	private static final double mass = 80;
	private static final String path = System.getProperty("user.dir") + "/simulation";
	private static final double deltaTime = Math.pow(10, -3);
	private static final double finalTime = 300;
	private static final double destinationY = 5;
	private static final int totalParticles = 100;
	private static final String _FILENAME_ = "EvacuarionTime";
	private static double lowerRadio = 0.25;
	private static double upperRadio = 0.35;
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		
		Files.write(Paths.get(path + _FILENAME_+".csv"), new LinkedList<String>(),
				Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		for (double velocity = initialDesiredVelocity; velocity < finalDesiredVelocity; velocity += deltaVelocity) {
			
			

			PedestrianSimulation simulation = new PedestrianSimulation(length, width, destinationY,
					destinationDiameter, mass, path, velocity, totalParticles, lowerRadio, upperRadio);

			GranularSimulationPositions gsp = new GranularSimulationPositions(path, length,width, printAfterNFrames,"lenght-"+length);
			EvacuationTime evacuation = new EvacuationTime(path, velocity, _FILENAME_);
			
			simulation.subscribe(gsp);
			simulation.subscribe(evacuation);
			
			

			Algorithm<FloatPoint> algorithm = new Verlet(deltaTime);

			simulation.simulate(algorithm, deltaTime, finalTime);

		}
	}


}
