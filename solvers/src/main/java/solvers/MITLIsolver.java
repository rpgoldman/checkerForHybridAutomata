package solvers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import formulae.cltloc.CLTLocFormula;
import formulae.cltloc.converters.CLTLoc2Zot;
import formulae.cltloc.visitor.NicelyIndentToString;
import formulae.mitli.MITLIFormula;
import formulae.mitli.converters.MITLI2CLTLoc;
import formulae.mitli.parser.MITLILexer;
import formulae.mitli.parser.MITLIParser;

public class MITLIsolver {

	private final MITLIFormula formula;
	private final PrintStream out;
	private final int bound;

	private CLTLocFormula cltlocFormula;
	private Map<Integer, MITLIFormula> vocabulary;
	private String zotEncoding;

	public MITLIsolver(MITLIFormula formula, PrintStream out, int bound) {

		this.formula = formula;
		this.out = out;
		this.bound = bound;

	}

	public boolean solve() throws IOException {

		out.println("Transforming the MITLI formula in CLTLoc");
		MITLI2CLTLoc converted = new MITLI2CLTLoc(formula, bound);
		cltlocFormula = converted.apply();

		this.vocabulary = converted.getVocabulary();

		out.println("CLTLoc formula converted ");

		zotEncoding = new CLTLoc2Zot(bound).apply(cltlocFormula);

		out.println("CLTLoc formula encoded in ZOT");

		String lispFile = "tmp.lisp";

		out.println("Running zot");
		//FileUtils.writeStringToFile(new File(lispFile), zotEncoding);

		//Process p = Runtime.getRuntime().exec("sh ./run_zot.sh " + lispFile);

		//boolean sat = false;

	/*	BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String lineer =stdErr.readLine();
		String linein =  stdIn.readLine();
		while (((linein) != null) || ((lineer) != null)) {

			System.out.println("ci passo");
			if (linein.contains("---SAT---")) {
				sat = true;
			}
			if (lineer != null) {
				out.println(lineer);
			}
			if (linein != null) {
				out.println(linein);
			}
			lineer = stdErr.readLine();
			linein =  stdIn.readLine();
		}*/

		// FileUtils.forceDelete(new File(lispFile));

		return true;
	}

	public String getZotEncoding() {
		return zotEncoding;
	}

	public static void main(String[] args) throws Exception {
		PrintStream out = System.out;

		out.println(args[0]);
		out.println(args[1]);

		String fileOutName = args[0];
		Preconditions.checkArgument(args.length > 1,
				"you must specify the file that contains the MITLI formula and the bound to be used");
		Preconditions.checkArgument(Files.exists(Paths.get(args[0])), "The file: " + args[0] + " does not exist");
		// Preconditions.checkArgument(args[1]!=null, "The second parameter mus
		// be the bound to be used");
		// Preconditions.checkArgument( Integer.getInteger(args[1])!=null,
		// "Parameter "+args[1]+ " not valid");
		out.println("Quantitative - Metric Temporal Logic Solver");
		out.println("v. 2.0 - 19/4/2013\n");
		out.println("Analizing the file: " + args[0]);

		if (new File(args[0]).isFile()) {
			ANTLRInputStream input = new ANTLRInputStream(new FileInputStream(args[0]));
			MITLILexer lexer = new MITLILexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			MITLIParser parser = new MITLIParser(tokens);
			parser.setBuildParseTree(true);

			MITLIFormula formula = parser.mitli().formula;

			MITLIsolver solver = new MITLIsolver(formula, System.out, Integer.parseInt(args[1]));
			solver.solve();

			out.print("************************************************************************************\n");
			// Writing the CLTLoc formula
			String cltlocFile;
			if (fileOutName.contains(".mitli")) {
				cltlocFile = fileOutName.replaceAll(".mitli", ".cltloc");
			} else {
				cltlocFile = fileOutName.concat(".cltloc");
			}

			FileUtils.writeStringToFile(new File(cltlocFile),
					solver.getCltlocFormula().accept(new NicelyIndentToString()));
			out.println("CLTLoc formula written in the file " + cltlocFile);

			// Writing the vocabulary
			StringBuilder vocabularyBuilder = new StringBuilder();
			for (Entry<Integer, MITLIFormula> entry : solver.getVocabulary().entrySet()) {
				vocabularyBuilder.append(entry.getKey() + ": " + entry.getValue() + "\n");
			}
			String vocabulary;
			if (fileOutName.contains(".mitli")) {
				vocabulary = fileOutName.replaceAll(".mitli", ".vocabulary");
			} else {
				vocabulary = fileOutName.concat(".vocabulary");
			}
			FileUtils.writeStringToFile(new File(vocabulary), vocabularyBuilder.toString());
			out.println("Vocabulary written in the file " + vocabulary);

			// Writing the zot encoding

			String zotEncoding = solver.getZotEncoding();
			String lispFile;
			if (fileOutName.contains(".mitli")) {
				lispFile = fileOutName.replaceAll(".mitli", ".lisp");
			} else {
				lispFile = fileOutName.concat(".lisp");
			}
			FileUtils.writeStringToFile(new File(lispFile), zotEncoding);
			out.println("Zot encoding written in the file " + lispFile);

		} else {
			System.out.println(
					"File " + (new File("").getAbsolutePath()) + File.pathSeparator + args[0] + " does not exist");
		}
	}

	public Map<Integer, MITLIFormula> getVocabulary() {
		return vocabulary;
	}

	public CLTLocFormula getCltlocFormula() {
		return cltlocFormula;
	}

	public void setCltlocFormula(CLTLocFormula cltlocFormula) {
		this.cltlocFormula = cltlocFormula;
	}
}