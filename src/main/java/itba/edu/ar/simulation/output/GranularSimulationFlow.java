package itba.edu.ar.simulation.output;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import itba.edu.ar.cellIndexMethod.data.particle.FloatPoint;
import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.simulation.SimulationObserver;

public class GranularSimulationFlow implements SimulationObserver {

	private static final double epsilon = 0.01;
	private static final String _SEPARATOR_ = ",";
	private int printAfterNframes = 0;
	private String path;
	private double height;
	private double bottomLength;
	private double diameter;
	private double deltaTime;
	private double length;
	int frames = 0;
	int particleQuantity = 0;
	double accumulatedFlow = 0;

	public GranularSimulationFlow(int printAfterNframes, String path, double heigth, double length, double width,
			double diameter, double deltaTime) throws IOException {
		super();
		this.length = length;
		this.deltaTime = deltaTime;
		this.printAfterNframes = printAfterNframes;
		this.path = path;
		this.height = heigth;
		this.diameter = diameter;
		bottomLength = (width - diameter) / 2;
		Files.write(Paths.get(path + "GranularSimulationFlow_" + length + ".csv"), new LinkedList<String>(),
				Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}

	public void simulationEnded(double time) throws IOException {
		List<String> fileContent = new LinkedList<String>();
		int times = (frames / printAfterNframes);
		fileContent.add("Average" + _SEPARATOR_ + accumulatedFlow / times);
		Files.write(Paths.get(path + "GranularSimulationFlow_" + length + ".csv"), fileContent,
				Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
		fileContent.clear();
		particleQuantity = 0;
	}


	public void stepEnded(List<Particle> particles, double time) throws IOException {

		for (Particle particle : particles) {
			if (insideHole(particle)) {
				particleQuantity++;
			}
		}

		if (frames++ % printAfterNframes != 0)
			return;

		List<String> fileContent = new LinkedList<String>();
		double flow = particleQuantity / (deltaTime * printAfterNframes);
		accumulatedFlow += flow;
		fileContent.add(time + _SEPARATOR_ + flow);
		Files.write(Paths.get(path + "GranularSimulationFlow_" + length + ".csv"), fileContent,
				Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
		fileContent.clear();
		particleQuantity = 0;
	}

	private boolean insideHole(Particle particle) {
		return insideHoleX(particle.getPosition()) && insideHoleY(particle.getPosition());
	}

	private boolean insideHoleX(FloatPoint position) {
		return position.getX() >= bottomLength && position.getX() <= (bottomLength + diameter);
	}

	private boolean insideHoleY(FloatPoint position) {
		return position.getY() >= height - epsilon && position.getY() <= height + epsilon;
	}

}
