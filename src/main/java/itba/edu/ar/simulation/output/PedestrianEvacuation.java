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

public class PedestrianEvacuation implements SimulationObserver{
	private List<String> fileContent = new LinkedList<String>();
	private static String _SEPARATOR_ = ",";
	private static String _FILENAME_ ="EvacuationVsTime_";
	private String path;
	private String tag;
	private int frame = 0;
	private int previusAmount = 0; 
	private int evaluationFrame = 0;
	private double destinationY;

	
	public PedestrianEvacuation(String path, String tag, int totalParticles,int evaluationFrame, double destinationY) throws IOException {
		this.path = path;
		this.tag=tag;
		this.evaluationFrame = evaluationFrame;
		this.previusAmount = totalParticles;
		this.destinationY=destinationY;
		
		Files.write(Paths.get(path + _FILENAME_+tag+".csv"), new LinkedList<String>(),
				Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}
	
	public void simulationEnded() throws IOException {
		
		
		
		
	}
	
	public void stepEnded(List<Particle> particles, double time) throws IOException {
		frame++;
		if (frame % evaluationFrame != 0)
			return;

		StringBuilder sb = new StringBuilder();
		int personsInside = personsInsideRoom(particles);
		sb.append(time).append(_SEPARATOR_).append(previusAmount - personsInside);
		previusAmount = personsInside;
		
		fileContent.add(sb.toString());
		Files.write(Paths.get(path + _FILENAME_+tag+".csv"), fileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		fileContent.clear();
		
	}

	private int personsInsideRoom(List<Particle> particles){
		int res = 0;
		for(Particle particle: particles){
			if(isInsideRoom(particle)){
				res ++;
			}
		}
		return res;
		
	}
	
	private boolean isInsideRoom(Particle particle){
		return particle.getPosition().getY() > destinationY;
	}
}
