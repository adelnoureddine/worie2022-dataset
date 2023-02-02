package se.kth.assertteam.jsonbench;

import java.util.Map;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResultExecution {
	String dataset;
	String name;
	int iteration;
	String startTime;
	String endTime;
	Map<String, ResultKind> resultIteration;

	public ResultExecution(String dataset, String name, int iteration, String startTime, String endTime,
			Map<String, ResultKind> resultIteration) {
		super();
		this.dataset = dataset;
		this.name = name;
		this.iteration = iteration;
		this.startTime = startTime;
		this.endTime = endTime;
		this.resultIteration = resultIteration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Map<String, ResultKind> getResultIteration() {
		return resultIteration;
	}

	public void setResultIteration(Map<String, ResultKind> resultIteration) {
		this.resultIteration = resultIteration;
	}

	public String toCSV() {

		String r = "";
		r += this.dataset + ",";
		r += this.name + ",";
		r += this.iteration + ",";
		r += this.startTime + ",";
		r += this.endTime + ",";

		for (ResultKind element : ResultKind.values()) {

			long total = this.resultIteration.values().stream().filter(e -> e.equals(element)).count();
			r += total + ",";
		}

		return r;

	}

	public static String toCSVHead() {

		String r = "";
		r += "dataset,";
		r += "name,";
		r += "iteration,";
		r += "startTime,";
		r += "endTime,";

		for (ResultKind element : ResultKind.values()) {

			r += element.name() + ",";
		}

		return r;

	}

}
