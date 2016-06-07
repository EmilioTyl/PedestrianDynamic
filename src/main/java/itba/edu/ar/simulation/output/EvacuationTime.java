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

public class EvacuationTime implements SimulationObserver {
	private List<String> fileContent = new LinkedList<String>();
	private static String _SEPARATOR_ = ",";

	private String path;
	private double desiredVelocity;
	private String filename = null;
	private int frame = 0;
	private double deltaTime = 0;

	public EvacuationTime(String path, double desiredVelocity, String filename, double deltaTime) {
		this.desiredVelocity = desiredVelocity;
		this.filename = filename;
		this.deltaTime = deltaTime;
	}

	public void simulationEnded() throws IOException {
		
		StringBuilder sb = new StringBuilder();
		sb.append(desiredVelocity).append(_SEPARATOR_).append(frame*deltaTime);
		fileContent.add(sb.toString());
		Files.write(Paths.get(path + filename + ".csv"), fileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		fileContent.clear();
	}

	public void stepEnded(List<Particle> particles, double time) throws IOException {
		frame ++;
		
	}

}
