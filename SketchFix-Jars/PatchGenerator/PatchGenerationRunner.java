
import edSketch.repair.config.ConfigReader.ConfigType;
import edSketch.repair.staticAnalyzer.model.StaticAnalyzer;

/**
 * @author Lisa May 28, 2018 PatchGenerationRunner.java
 */

public class PatchGenerationRunner {

	public static void main(String[] args) {
		StaticAnalyzer analyzer = new StaticAnalyzer();
		analyzer.setConfigFile(ConfigType.SIMPLE, "PatchGenerationConfig.txt");
		// The path of the class where the defect is.
		String path = "/main/java/CLASSPATH";
		// The first line of the defect.
		String defect_line_start = "1";
		// The last line of the defect.
		String defect_line_end = "2";
		String faultLoc = path + ":" + defect_line_start + ":" + defect_line_end;
		analyzer.setFaultLocation(faultLoc, 0);
	}

}
