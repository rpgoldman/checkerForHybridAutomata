package zotrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

public class ZotRunner {

	private final String zotEncoding;
	private final PrintStream out;

	/**
	 * 
	 * @param zotEncoding
	 *            the zotEncoding to be verified
	 */
	public ZotRunner(String zotEncoding, PrintStream out) {
		Preconditions.checkNotNull(zotEncoding, "the zotEncoding cannot be null");
		this.zotEncoding = zotEncoding;
		this.out = out;
	}

	public boolean run() throws IOException {
		String lispFile = "tmp.zot";

		out.println("Running zot");
		FileUtils.writeStringToFile(new File(lispFile), zotEncoding);

		out.println("Considering the file " + new File(lispFile).getAbsolutePath());
		 String[] command = {"/bin/bash", "run_zot.sh", lispFile};
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		Process p = builder.start();

		// Process p = Runtime.getRuntime().exec("sh ./run_zot.sh " + lispFile);

		InputStream stdout = p.getInputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));

		boolean sat = false;

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("---UNSAT---")) {
				sat = true;
			}
			out.println("Stdout: " + line);
		}

		out.print("Zot ends");
		// FileUtils.forceDelete(new File(lispFile));
		return sat;
	}

}