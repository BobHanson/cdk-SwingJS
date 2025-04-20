package swingjs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.MDLV3000Reader;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.sun.jna.Pointer;

import io.github.dan2097.jnainchi.InchiAPI;
import io.github.dan2097.jnainchi.InchiInput;
import io.github.dan2097.jnainchi.InchiOptions;
import io.github.dan2097.jnainchi.JnaInchi;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;

/**
 * A class for CDK matching a similar class for OCL allowing 
 * streamlined JavaScript (or, in principle, Java) access to
 * various useful methods.  
 * 
 * Easily expandable as needs arise; this initial implementation
 * is primarily for working with InChIs.
 * 
 * 
 * 
 * @j2sExport
 * 
 * @author Bob Hanson
 *
 */
public class CDK {

	private static boolean isJS = /** @j2sNative true || */
			false;
	
	public static int notApplicable;

	public static boolean useInchiAPI = true;

	private final static String DEFAULT_OUTPUT_OPTIONS = "fixamide fixacid";

	static {
		/**
		 * @j2sNative C$.useInchiAPI = true;
		 */
	}

	@SuppressWarnings("resource")
	public static String get2DMolFromCDKMolecule(IAtomContainer mol) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new MDLV2000Writer(os).write(mol);
			String molfile = new String(os.toByteArray());
			os.close();
			return molfile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String get2DMolFromInChI(String inchi) {
		return get2DMolFromInChIOpt(inchi, DEFAULT_OUTPUT_OPTIONS);
	}

	public static String get2DMolFromInChIOpt(String inchi, String outputOptions) {
		try {
			return get2DMolFromCDKMolecule(getCDKMoleculeFromInChI(inchi, outputOptions));
		} catch (Throwable e) {
			return null;
		}
	}

	public static String get2DMolFromSmiles(String smiles) {
		return get2DMolFromCDKMolecule(getCDKMoleculeFromSmiles(smiles));
	}

	public static IAtomContainer getCDKMoleculeFromInChI(String inchi, String outputOptions) {
		try {
			return getCDKMoleculeFromInChIImpl(inchi, outputOptions); 
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IAtomContainer getCDKMoleculeFromInchiInput(InchiInput input) {
		try {
			return new InChIToStructure(input, getBuilder()) {
			}.getAtomContainer();
		} catch (CDKException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("resource")
	public static IAtomContainer getCDKMoleculeFromMOL(String molData) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(molData.getBytes());
			return (molData.indexOf("V3000") >= 0 ? new MDLV3000Reader(is) : new MDLV2000Reader(is))
					.read(newAtomContainer());
		} catch (CDKException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IAtomContainer getCDKMoleculeFromSmiles(String smiles) {
		try {
			return new SmilesParser(getBuilder()).parseSmiles(smiles);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getDataURIFromCDKMolecule(IAtomContainer mol, boolean withAtomNumbers) {		
		BufferedImage image = getImageFromCDKMolecule(mol, withAtomNumbers);
		if (image == null)
			return null;
		return getDataURIForImage(image);
	}

	public static String getDataURIFromInChI(String inchi) {
		return getDataURIFromInChIOpts(inchi, DEFAULT_OUTPUT_OPTIONS, false);
	}

	public static String getDataURIFromInChIOpts(String inchi, String outputOptions, boolean withAtomNumbers) {
		return getDataURIFromCDKMolecule(getCDKMoleculeFromInChI(inchi, outputOptions), withAtomNumbers);
	}

	public static BufferedImage getImageFromCDKMolecule(IAtomContainer mol, boolean withAtomNumbers) {
		try {
			DepictionGenerator dg = null;
			dg = new DepictionGenerator();
			if (withAtomNumbers) {
				dg = dg.withAnnotationScale(0.8);
				dg = dg.withAtomNumbers();
			}
			return dg.depict(mol).toImg();
		} catch (CDKException e) {
			return null;
		}
	}

	private static String getDataURIForImage(BufferedImage image) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", os);
			byte[] bytes = Base64.getEncoder().encode(os.toByteArray());
			return "data:image/png;base64," + new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInChIFromCDKMolecule(IAtomContainer mol, String inputOptions) {
		try {
			return getInChIFromCDKMoleculeImpl(mol, inputOptions);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInChIFromInChI(String inchi, String inputOptions) {
		return getInChIFromCDKMolecule(getCDKMoleculeFromInChI(inchi, null), fixInputOptions(inputOptions));
	}

	public static String getInChIFromInchiInput(InchiInput input, String inputOptions) {
		return getInChIFromInchiInputImpl(input, inputOptions);
	}

	public static String getInChIFromMOL(String molData, String inputOptions) {
		return getInChIFromCDKMolecule(getCDKMoleculeFromMOL(molData), fixInputOptions(inputOptions));
	}

	public static String getInChIFromSmiles(String smiles, String inputOptions) {
		try {
			return getInChIFromCDKMolecule(new SmilesParser(getBuilder()).parseSmiles(smiles), fixInputOptions(inputOptions));
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InchiInput getInchiInputFromCDKMolecule(IAtomContainer mol) {
		try {
			return InChIGenerator.getInchiInputFromCDKAtomContainer(mol);
		} catch (CDKException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InchiInput getInchiInputFromInChI(String inchi, String outputOptions) {
		return getInchiInputFromInChIImpl(inchi, outputOptions);
	}

	public static InchiInput getInchiInputFromMoleculeHandle(Pointer hStatus, Pointer hMolecule, String outputOptions) {
		return getInchiInputFromMoleculeHandleImpl(hStatus, hMolecule, outputOptions);
	}

	public static String getInChIKey(IAtomContainer mol, String inputOptions) {
		String inchi = getInChIFromCDKMolecule(mol, inputOptions);
		return getInchiKeyFromInChIImpl(inchi);
	}

	public static String getInchiModelJSON(String inchi) {	
		return getInchiModelJSONOpts(inchi, DEFAULT_OUTPUT_OPTIONS);
	}

	public static String getInchiModelJSONOpts(String inchi, String outputOptions) {	
		return getInchiModelJSONFromInchiInput(getInchiInputFromInChI(inchi, outputOptions));
	}

	public static String getInchiModelJSONFromInchiInput(InchiInput input) {
		return getJSONFromInchiInputImpl(input);
	}

	public static String getInChIVersion(boolean fullDescription) {
		return getInChIVersionImpl(false);
	}

	public static String getSmilesFromCDKMolecule(IAtomContainer mol) {
		try {
			return new SmilesGenerator(SmiFlavor.Isomeric).create(mol);
		} catch (CDKException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getSmilesFromInChI(String inchi) {
		return getSmilesFromInChIOpt(inchi, DEFAULT_OUTPUT_OPTIONS);
	}

	public static String getSmilesFromInChIOpt(String inchi, String outputOptions) {
		return getSmilesFromInChIImpl(inchi, outputOptions);
	}

	public static void initInchi(Runnable r) {
		if (isJS) {
			LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
			InchiLibrary.class.getName();
		}
		if (useInchiAPI)
		  InchiAPI.initAndRun(r);
		else // Java only
			r.run();
	}

	public final static void main(String[] args) {
		LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
		System.out.println(InchiLibrary.class.getName());
		Locale.setDefault(Locale.ROOT);
	}

	public static IAtomContainer newAtomContainer() {
		return getBuilder().newInstance(IAtomContainer.class);
	}

	public static IAtomContainer suppressHydrogens(IAtomContainer mol) {
		return AtomContainerManipulator.suppressHydrogens(mol);
	}

	private static String fixInputOptions(String inputOptions) {
		if (inputOptions == null || "standard".equals(inputOptions))
			inputOptions = "";
		return inputOptions;
	}

	private static IChemObjectBuilder getBuilder() {
		return DefaultChemObjectBuilder.getInstance();
	}

	private static InchiInput getInchiInputFromInChIImpl(String inchi, String outputOptions) {
		if (useInchiAPI) {
			return InchiAPI.getInchiInputFromInChI(inchi, outputOptions);
		} else {
			if (outputOptions == null || outputOptions.length() == 0) {
				notApplicable++;
				System.err.println("Output options are not implemented in JnaInchi");
			}
			return JnaInchi.getInchiInputFromInchi(inchi).getInchiInput();
		}
	}

	private static String getInChIFromInchiInputImpl(InchiInput input, String inputOptions) {
		if (useInchiAPI) {
			return InchiAPI.getInChIFromInchiInput(input, inputOptions);
		} else {
			notApplicable++;
			return null; // not implemented
		}
	}

	private static InchiInput getInchiInputFromMoleculeHandleImpl(Pointer hStatus, Pointer hMolecule,
			String outputOptions) {
		if (useInchiAPI) {
			if (outputOptions == null)
				outputOptions = DEFAULT_OUTPUT_OPTIONS;
			return InchiAPI.getInchiInputFromMoleculeHandle(hStatus, hMolecule, outputOptions);
		} else {
			notApplicable++;
			return null;
		}
	}

	private static String getInchiKeyFromInChIImpl(String inchi) {
		if (useInchiAPI) {
			return InchiAPI.getInChIKeyFromInChI(inchi);
		} else {
			return JnaInchi.inchiToInchiKey(inchi).getInchiKey();
		}
	}

	private static String getJSONFromInchiInputImpl(InchiInput input) {
		if (useInchiAPI) {
			return InchiAPI.getJSONFromInchiInput(input);
		} else {
			notApplicable++;
			return "";
		}
	}

	private static String getInChIVersionImpl(boolean fullDescription) {
		if (useInchiAPI) {
			return InchiAPI.getInChIVersion(fullDescription);
		} else {
			return JnaInchi.getInchiLibraryVersion();
		}
	}

	private static String getSmilesFromInChIImpl(String inchi, String outputOptions) {
		IAtomContainer mol;
		if (useInchiAPI) {	
			InchiInput input = InchiAPI.getInchiInputFromInChI(inchi, outputOptions);
			mol = getCDKMoleculeFromInchiInput(input);
		} else {
			if (outputOptions != null && outputOptions.length() > 0)
				System.err.println("output options are not available for JNA-InChI");
			mol = getCDKMoleculeFromInChI(inchi, null);		
		}
		mol = AtomContainerManipulator.suppressHydrogens(mol);
		return getSmilesFromCDKMolecule(mol);
	}

	private static String getInChIFromCDKMoleculeImpl(IAtomContainer mol, String inputOptions) throws CDKException {
		if (useInchiAPI) {
			return getInChIFromInchiInput(getInchiInputFromCDKMolecule(mol), inputOptions);
		} else {
			return InChIGeneratorFactory.getInstance().getInChIGenerator(mol, inputOptions).getInchi();
		}
	}

	private static IAtomContainer getCDKMoleculeFromInChIImpl(String inchi, String outputOptions) throws CDKException {
		if (useInchiAPI) {
			InchiInput input = InchiAPI.getInchiInputFromInChI(inchi, outputOptions);
			IAtomContainer mol = getCDKMoleculeFromInchiInput(input);
			return AtomContainerManipulator.suppressHydrogens(mol);
		} else {
			if (outputOptions == null || outputOptions.length() == 0) {
				notApplicable++;
				System.err.println("Output options are not implemented in JnaInchi");
			}
			return InChIGeneratorFactory.getInstance().getInChIToStructure(inchi, getBuilder()).getAtomContainer();
		}
	}

	public static String getCallCount() {
		if (useInchiAPI) {
			return "" + InchiAPI.getCallCount();
		} else {			
			return "unknown";
		}
	}

	public static InchiOptions getInchiOptions(String options) {
		return InchiAPI.parseOptions(options);
		
	}
	
}
