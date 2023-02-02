package se.kth.assertteam.jsonbench;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import se.kth.assertteam.jsonbench.parser.Argo;
import se.kth.assertteam.jsonbench.parser.CookJson;
import se.kth.assertteam.jsonbench.parser.Corn;
import se.kth.assertteam.jsonbench.parser.FastJson;
import se.kth.assertteam.jsonbench.parser.FlexJson;
import se.kth.assertteam.jsonbench.parser.GensonP;
import se.kth.assertteam.jsonbench.parser.GsonParser;
import se.kth.assertteam.jsonbench.parser.JJson;
import se.kth.assertteam.jsonbench.parser.Jackson;
import se.kth.assertteam.jsonbench.parser.Johnzon;
import se.kth.assertteam.jsonbench.parser.JsonIJ;
import se.kth.assertteam.jsonbench.parser.JsonIO;
import se.kth.assertteam.jsonbench.parser.JsonLib;
import se.kth.assertteam.jsonbench.parser.JsonP;
import se.kth.assertteam.jsonbench.parser.JsonSimple;
import se.kth.assertteam.jsonbench.parser.JsonUtil;
import se.kth.assertteam.jsonbench.parser.MJson;
import se.kth.assertteam.jsonbench.parser.OrgJSON;
import se.kth.assertteam.jsonbench.parser.ProgsBaseJson;
import se.kth.assertteam.jsonbench.parser.SOJO;

@Command(name = "TestJSonEngine", mixinStandardHelpOptions = true, version = "1", description = "Energy measurement of JSON libraries")
public class JSonEnergyEngine implements Callable<Integer> {
	static boolean log = true;

	@Option(names = { "-t",
			"--timeout" }, description = "Timeout in second for executing the parsing of a JSON file", defaultValue = "15")
	int TIMEOUT = 15;

	@Option(names = { "-i", "--iterations" }, description = "Iterations done on each dataset", defaultValue = "1")
	int iterations;

	@Option(names = { "-n",
			"--nrThreads" }, description = "Number of threads to execute the parsing", defaultValue = "1")
	int nrThreads; // Runtime.getRuntime().availableProcessors();

	@Option(names = { "-d",
			"--delayImpl" }, description = "Delay between the testing of two JSON libraries, in seconds", defaultValue = "10")
	int delayBetweenImplementations;

	@Option(names = { "-c",
			"--delayCases" }, description = "Delay between the parsing of two json files, in seconds", defaultValue = "0.001")
	double delayBetweenCases;

	@Option(names = {
			"--out" }, description = "Directory when we write the CSV with the results. It creates it if the folder does not exist", defaultValue = "./out/")

	File out;

	@Parameters(description = "Path to the folders to analyze containing JSON files", defaultValue = "./data/bench/correct")
	private File[] foldersToAnalyze;

	@Option(names = { "--libs" }, description = "Names of the libs, separate by commas", split = ",")

	String[] libsToExecute = null;

	public static void main(String[] args) throws IOException {

		int exitCode = new CommandLine(new JSonEnergyEngine()).execute(args);
		System.exit(exitCode);

	}

	@Override
	public Integer call() throws Exception {

		System.out.println("delayBetweenCases " + delayBetweenCases + " delayBetweenImplementations "
				+ delayBetweenImplementations + " " + foldersToAnalyze + " " + iterations);

		for (File file : foldersToAnalyze) {

			List<File> filesToAnalyze = findFiles(file.getAbsolutePath(), ".json");

			System.out.println("Files to analyze " + filesToAnalyze.size());
			List<JP> listImplementations = getImplementations();

			this.runExperiment(filesToAnalyze, listImplementations, file);
		}
		return 1;
	}

	public void runExperiment(List<File> filesToAnalyze, List<JP> listImplementations, File dataset)
			throws IOException {
		long pid = ProcessHandle.current().pid();
		System.out.println("Process ID of Java program: " + pid);
		System.out.println("Waiting for a few seconds before experiment start...");
		try {
			Thread.currentThread().sleep(1000 * delayBetweenImplementations);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		int i = 1;

		List<ResultExecution> executionResults = new ArrayList<>();

		for (JP anImplementation : listImplementations) {
			long init = (new Date()).getTime();
			System.out.println(i + "/" + listImplementations.size() + " " + anImplementation.getName());
			List<ResultExecution> executionResultsImpl = runImplementation(anImplementation, filesToAnalyze, dataset);

			executionResults.addAll(executionResultsImpl);

			long end = (new Date()).getTime();

			System.out.println("End: " + i + "/" + listImplementations.size() + " " + anImplementation.getName() + " "
					+ ((double) (end - init)) / 1000d);

			System.out.println("Start delay between implementations");
			try {
				Thread.currentThread().sleep(1000 * delayBetweenImplementations);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;

		}
		System.out.println("Printing ");
		String result = ResultExecution.toCSVHead() + "\n";
		for (ResultExecution resultExecution : executionResults) {
			result += resultExecution.toCSV() + "\n";

		}

		if (!out.exists()) {
			out.mkdirs();
		}

		File output = new File(out.getAbsoluteFile() + "/results_" + (new Date()).getTime() + ".csv");
		Files.write(output.toPath(), result.getBytes(), StandardOpenOption.CREATE_NEW);
		System.out.println(result);
		System.out.println("Saved at " + output.getAbsolutePath());

	}

	public List<JP> getImplementations() {

		List<JP> listImplementations = new ArrayList<>();

		listImplementations.add(new OrgJSON());

		listImplementations.add(new GsonParser());

		listImplementations.add(new JsonSimple());

		listImplementations.add(new Jackson());

		listImplementations.add(new CookJson());

		listImplementations.add(new JsonIO());

		listImplementations.add(new JsonLib());

		listImplementations.add(new JsonUtil());

		listImplementations.add(new MJson());

		listImplementations.add(new FlexJson());

		listImplementations.add(new Corn());

		listImplementations.add(new Johnzon());

		listImplementations.add(new GensonP());
		listImplementations.add(new ProgsBaseJson());
		listImplementations.add(new JsonP());

		listImplementations.add(new SOJO());
		listImplementations.add(new Argo());
		listImplementations.add(new FastJson());
		listImplementations.add(new JJson());
		listImplementations.add(new JsonIJ());

		if (this.libsToExecute != null && this.libsToExecute.length > 0) {

			List<JP> filteredImplementations = new ArrayList<>();

			for (String targetLib : libsToExecute) {

				Optional<JP> op = listImplementations.stream().filter(e -> targetLib.equals(e.getName())).findFirst();

				if (op.isPresent()) {
					filteredImplementations.add(op.get());

				} else {
					throw new IllegalArgumentException("Parser name not recorgnized " + targetLib);
				}
			}
			return filteredImplementations;
		} else
			return listImplementations;
	}

	public List<ResultExecution> runImplementation(JP parser, List<File> filesToAnalyze, File dataset)
			throws IOException {

		List<ResultExecution> executionResults = new ArrayList<>();

		// In case we want to run multiples iterations on the dataset

		for (int i = 1; i <= iterations; i++) {
			ZonedDateTime startTime = ZonedDateTime.now();
			Map<String, ResultKind> resultIteration = runAllFilesOnImplementation(parser, filesToAnalyze);
			ZonedDateTime endTime = ZonedDateTime.now();

			ResultExecution re = new ResultExecution(dataset.getName(), parser.getName(), i, transformTime(startTime),
					transformTime(endTime), resultIteration);
			executionResults.add(re);

		}
		return executionResults;

	}

	public static List<File> findFiles(String dir, String suffix) throws IOException {
		List<File> files = new ArrayList<>();

		Files.walk(Paths.get(dir)).filter(Files::isRegularFile).forEach((f) -> {
			String file = f.toString();
			if (file.endsWith(suffix))
				files.add(new File(file));
		});

		return files;
	}

	public static String readFile(File f) throws IOException {
		try {

			// FileReader fr = new FileReader(f);
			Stream<String> lines = Files.lines(f.toPath(), Charset.forName("UTF-8"));
			String data = lines.collect(Collectors.joining("\n"));
			lines.close();
			return data;

		} catch (Exception e) {
			System.err.println("Failed with UTF-8");
		}
		return Files.lines(f.toPath(), Charset.forName("UTF-16")).collect(Collectors.joining("\n"));
	}

	public Map<String, ResultKind> runAllFilesOnImplementation(JP parser, List<File> filesToAnalyze)
			throws IOException {
		int i = 0;

		System.out.println("Starting running " + parser.getName());
		ExecutorService executor = Executors.newFixedThreadPool(nrThreads);

		Map<String, ResultKind> results = new HashMap<>();

		ZonedDateTime now = ZonedDateTime.now();
		System.out.println("Start " + transformTime(now));

		for (File f : filesToAnalyze) {
			if (log)
				System.out.println("[" + parser.getName() + "] " + (i++) + " " + f.getName());

			Callable<ResultKind> task = () -> testCorrectJson(f, parser);
			Future<ResultKind> future = executor.submit(task);

			if (this.delayBetweenCases > 0) {
				try {
					Thread.currentThread().sleep((long) (1000 * delayBetweenCases));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			ResultKind r = ResultKind.CRASH;
			try {
				r = future.get(TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				System.err.println("Timout for " + f.getName());
			}
			results.put(f.getName(), r);
		}
		executor.shutdown();
		return results;
	}

	public String transformTime(ZonedDateTime now) {

		return now.getHour() + ":" + now.getMinute() + ":" + now.getSecond() + ":"
				+ (now.toInstant().getNano() / 1000000);
	}

	public Map<String, ResultKind> testAllIncorrectJson(File inDir, JP parser) throws IOException {
		int i = 0;
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Map<String, ResultKind> results = new HashMap<>();
		for (File f : findFiles(inDir.getAbsolutePath(), ".json")) {
			if (log)
				System.out.println("[" + parser.getName() + "] " + (i++) + " " + f.getName());

			Callable<ResultKind> task = () -> testIncorrectJson(f, parser);
			Future<ResultKind> future = executor.submit(task);
			ResultKind r = ResultKind.CRASH;
			try {
				r = future.get(TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				System.err.println("Timout for " + f.getName());
			}
			results.put(f.getName(), r);
		}
		executor.shutdown();
		return results;
	}

	public static ResultKind testCorrectJson(File f, JP parser) {
		String jsonIn = null;
		try {
			jsonIn = readFile(f);
		} catch (Exception e) {
			return ResultKind.FILE_ERROR;
		}
		Object jsonObject = null;
		String jsonOut;
		try {
			try {
				jsonObject = parser.parseString(jsonIn);
				if (jsonObject == null && !jsonIn.equals("null"))
					return ResultKind.NULL_OBJECT;
			} catch (Exception e) {
				return ResultKind.PARSE_EXCEPTION;
			}
			try {
				jsonOut = parser.print(jsonObject);
				if (jsonOut.equalsIgnoreCase(jsonIn)) {
					return ResultKind.OK;
				}
				if (parser.equivalence(jsonObject, parser.parseString(jsonOut))) {
					return ResultKind.EQUIVALENT_OBJECT;
				} else {
					return ResultKind.NON_EQUIVALENT_OBJECT;
				}
			} catch (Exception e) {
				return ResultKind.PRINT_EXCEPTION;
			}
		} catch (Error e) {
			return ResultKind.CRASH;
		}
	}

	public static ResultKind testIncorrectJson(File f, JP parser) {
		String jsonIn;
		try {
			jsonIn = readFile(f);
		} catch (Exception e) {
			return ResultKind.FILE_ERROR;
		}
		try {
			try {
				try {
					Object jsonObject = parser.parseString(jsonIn);
					if (jsonObject != null)
						return ResultKind.UNEXPECTED_OBJECT;
					else
						return ResultKind.NULL_OBJECT;
				} catch (Exception e) {
					return ResultKind.PARSE_EXCEPTION;
				}
			} catch (Error e) {
				return ResultKind.CRASH;
			}
		} catch (Exception e) {
			return ResultKind.CRASH;
		}
	}

}
