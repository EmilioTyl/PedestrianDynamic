package itba.edu.ar.simulation.output;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.simulation.SimulationObserver;
import itba.edu.ar.ss.model.entity.impl.AstronomicalObject;
import itba.edu.ar.ss.model.force.impl.AstronomicalObjectForce;
import itba.edu.ar.ss.simulation.solarSystem.SolarSystemSimulationObserver;
import itba.edu.ar.ss.system.data.SolarSystemData;

public class GranularSimulationEnergy implements SimulationObserver {

	private List<String> fileContent = new LinkedList<String>();
	private static String _SEPARATOR_ = ",";
	private String path;
	private int printAfterNFrames;
	private String tag;
	
	public GranularSimulationEnergy(String path, int printAfterNFrames, String tag) throws IOException {
		this.path = path;
		this.printAfterNFrames = printAfterNFrames;
		this.tag=tag;
		Files.write(Paths.get(path + "GranularSimulationEnergy_"+tag+".csv"), new LinkedList<String>(),
				Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}

	int frame = 0;

	public void stepEnded(List<Particle> particles, double time) throws IOException {
		if (frame++ % printAfterNFrames != 0)
			return;

		StringBuilder sb = new StringBuilder();
		double totalKineticEnergy = 0;

		for (Particle particle : particles) {
			totalKineticEnergy += getKineticEnergy(particle);
		}

		sb.append(time).append(_SEPARATOR_).append(totalKineticEnergy);
		fileContent.add(sb.toString());
		Files.write(Paths.get(path + "GranularSimulationEnergy_"+tag+".csv"), fileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		fileContent.clear();
	}

	public double getKineticEnergy(Particle particle) {
		return 0.5 * particle.getMass() * Math.pow(particle.getVelocityAbs(), 2);
	}

	public void simulationEnded(double time) throws IOException {

	}

}
