package itba.edu.ar.simulation.output;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import itba.edu.ar.cellIndexMethod.data.particle.Particle;
import itba.edu.ar.simulation.SimulationObserver;

public class PedestrianFlowUsingWindow implements SimulationObserver {

	private int windowSize;
	private int stepWindow;
	private List<Integer> pedestriansInRoom = new ArrayList<Integer>();
	private int frame = 0;
	private double destinationY;
	private double deltaTime;
	private String path;
	private static String _SEPARATOR_ = ",";
	private static String _FILENAME_ = "PedestrianFlowUsingWindow_";
	private String tag;

	public PedestrianFlowUsingWindow(int windowSize, int stepWindow, double destinationY, double deltaTime,
			String path, String tag) {
		super();
		this.windowSize = windowSize;
		this.stepWindow = stepWindow;
		this.destinationY = destinationY;
		this.deltaTime = deltaTime;
		this.path = path;
		this.tag = tag;
	}

	public void stepEnded(List<Particle> particles, double time) throws IOException {
		if (frame++ % stepWindow != 0)
			return;

		pedestriansInRoom.add(personsInsideRoom(particles));

	}

	public void simulationEnded() throws IOException {

		List<String> fileContent = new ArrayList<String>();
		int offset = windowSize / stepWindow;
		for (int index = offset; index < pedestriansInRoom.size(); index++) {
			StringBuilder sb = new StringBuilder();
			
			double pedestriansThatLeftTheRoom = (pedestriansInRoom.get(index-offset) - pedestriansInRoom.get(index))/(windowSize*deltaTime);
			sb.append(index*stepWindow* deltaTime).append(_SEPARATOR_).append(pedestriansThatLeftTheRoom);
			fileContent.add(sb.toString());
		}

		Files.write(Paths.get(path + _FILENAME_ + tag + ".csv"), fileContent, Charset.forName("UTF-8"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
		fileContent.clear();

	}

	private int personsInsideRoom(List<Particle> particles) {
		int res = 0;
		for (Particle particle : particles) {
			if (isInsideRoom(particle)) {
				res++;
			}
		}
		return res;

	}

	private boolean isInsideRoom(Particle particle) {
		return particle.getPosition().getY() > destinationY;
	}

}
