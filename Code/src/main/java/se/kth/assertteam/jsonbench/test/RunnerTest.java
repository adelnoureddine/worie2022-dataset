package se.kth.assertteam.jsonbench.test;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import se.kth.assertteam.jsonbench.JSonEnergyEngine;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RunnerTest {

	@Test
	public void testMainDatasetAW() throws IOException {

		JSonEnergyEngine.main(new String[] { "./data/awjson/" });

	}

	@Test
	public void testMainDatasetJsonBench() throws IOException {

		JSonEnergyEngine.main(new String[] { "./data/bench/" });

	}

	@Test
	public void testMainDatasetJsonBenchLibs() throws IOException {

		JSonEnergyEngine.main(new String[] { "--libs", "json-simple", "./data/bench/" });

	}

	@Test
	public void testMainDatasetJsonBenchLibs2Libs() throws IOException {

		JSonEnergyEngine.main(new String[] { "--libs", "json-simple,gson", "./data/bench/" });

	}

	@Test(expected = Throwable.class)
	public void testMainDatasetJsonBenchLibsWrongName() {

		try {
			JSonEnergyEngine.main(new String[] { "--libs", "json-simple2222333444sss", "./data/bench/" });
			fail();
		} catch (IOException e) {

		}

	}
}
