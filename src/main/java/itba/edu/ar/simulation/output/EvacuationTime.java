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

public class EvacuationTime implements SimulationObserver{
	private List<String> fileContent = new LinkedList<String>();
	private static String _SEPARATOR_ = ",";
	
	private String path;
	private double desiredVelocity;
	private String filename = null;


	
	public EvacuationTime(String path, double desiredVelocity, String filename) {
		this.desiredVelocity = desiredVelocity;
		this.filename = filename;
	}
	
	public void simulationEnded(double time) throws IOException {
		StringBuilder sb = new StringBuilder();	
		sb.append(desiredVelocity).append(_SEPARATOR_).append(time);
		fileContent.add(sb.toString());
		Files.write(Paths.get(path + filename+".csv"), fileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		fileContent.clear();
	}

	public void stepEnded(List<Particle> particles, double time) throws IOException {
		
	}

}
