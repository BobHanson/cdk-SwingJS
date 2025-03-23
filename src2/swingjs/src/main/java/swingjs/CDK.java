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

	public static boolean useInchiAPI = false;
	static {
		/**
		 * @j2sNative C$.useInchiAPI = true;
		 */
	}

	/**
	 * 
	 * @param mol
	 * @return
	 */
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

	/**
	 * 
	 * 
	 * @param inchi
	 * @return
	 */
	public static String get2DMolFromInChI(String inchi) {
		try {
			return get2DMolFromCDKMolecule(getCDKMoleculeFromInChI(inchi, "fixamide"));
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param inchi
	 * @param moreOptions "fixamide"
	 * @return
	 * 
	 */
	public static IAtomContainer getCDKMoleculeFromInChI(String inchi, String moreOptions) {
		try {
			return getCDKMoleculeFromInChIImpl(inchi, moreOptions); 
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Particularly for JavaScript, this method allows passing to
	 * 
	 * 
	 * 
	 * @param input an InchiInput object
	 * @return a CDK molecule as in IAtomContainer
	 * 
	 */
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

	/**
	 * 
	 * @param smiles
	 * @return
	 */
	public static IAtomContainer getCDKMoleculeFromSmiles(String smiles) {
		try {
			return new SmilesParser(getBuilder()).parseSmiles(smiles);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param mol
	 * @param os
	 * @return
	 */
	public static String getDataURIFromCDKMolecule(IAtomContainer mol) {
		DepictionGenerator dg = null;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			dg = new DepictionGenerator();
			BufferedImage image = dg.depict(mol).toImg();
			ImageIO.write(image, "PNG", os);
			byte[] bytes = Base64.getEncoder().encode(os.toByteArray());
			return "data:image/png;base64," + new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 
	 * @param inchi
	 * @return
	 * 
	 */
	public static String getDataURIFromInChI(String inchi) {
		return getDataURIFromCDKMolecule(getCDKMoleculeFromInChI(inchi, "fixamide"));
	}

	public static String getInChIFromCDKMolecule(IAtomContainer mol, String options) {
		try {
			return getInChIFromCDKMoleculeImpl(mol, options);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInChIFromInChI(String inchi, String options) {
		return getInChIFromCDKMolecule(getCDKMoleculeFromInChI(inchi, null), fixOptions(options));
	}

	public static String getInChIFromInchiInput(InchiInput input, String options) {
		return getInChIFromInchiInputImpl(input, options);
	}

	/**
	 * 
	 */
	public static String getInChIFromMOL(String molData, String options) {
		return getInChIFromCDKMolecule(getCDKMoleculeFromMOL(molData), fixOptions(options));
	}

	/**
	 * 
	 */
	public static String getInChIFromSmiles(String smiles, String options) {
		try {
			return getInChIFromCDKMolecule(new SmilesParser(getBuilder()).parseSmiles(smiles), fixOptions(options));
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 */
	public static InchiInput getInchiInputFromCDKMolecule(IAtomContainer mol) {
		try {
			return InChIGenerator.getInchiInputFromCDKAtomContainer(mol);
		} catch (CDKException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param hStatus
	 * @param hMolecule
	 * @param moreOptions may be "fixamide"
	 * @return
	 */
	public static InchiInput getInchiInputFromMoleculeHandle(Pointer hStatus, Pointer hMolecule, String moreOptions) {
		
		return getInchiInputFromMoleculeHandleImpl(hStatus, hMolecule, moreOptions);
	}

	/**
	 * 
	 */
	public static String getInChIKey(IAtomContainer mol, String options) {
		
		String inchi = getInChIFromCDKMolecule(mol, options);
		return getInchiKeyFromInChIImpl(inchi);
	}

	/**
	 * 
	 */
	public static String getInChIModelJSON(String inchi) {	
		return getJSONFromInchiInputImpl(getInchiInputFromInChIImpl(inchi, "fixamide"));
	}

	/**
	 * 
	 */
	public static String getInChIVersion(boolean fullDescription) {
		return getInChIVersionImpl(false);
	}

	/**
	 * 
	 */
	public static String getSmilesFromCDKMolecule(IAtomContainer mol) {
		try {
			return new SmilesGenerator(SmiFlavor.Isomeric).create(mol);
		} catch (CDKException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 */
	public static String getSmilesFromInChI(String inchi) {
		return getSmilesFromInChIImpl(inchi);
	}

	public static void init(Runnable r) {
		if (isJS) {
			LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
			InchiLibrary.class.getName();
		}
		if (useInchiAPI)
		  InchiAPI.initAndRun(r);
		else // Java only
			r.run();
	}

	/**
	 * 
	 */
	public final static void main(String[] args) {
		LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
		System.out.println(InchiLibrary.class.getName());
		Locale.setDefault(Locale.ROOT);
	}

	/**
	 * 
	 */
	public static IAtomContainer newAtomContainer() {
		return getBuilder().newInstance(IAtomContainer.class);
	}

	/**
	 * 
	 * 
	 * @param mol
	 * @return mol
	 */
	public static IAtomContainer suppressHydrogens(IAtomContainer mol) {
		return AtomContainerManipulator.suppressHydrogens(mol);
	}

	private static String fixOptions(String options) {
		if (options == "" || options == "standard")
			options = null;
		return options;
	}

	private static IChemObjectBuilder getBuilder() {
		return DefaultChemObjectBuilder.getInstance();
	}

	private static InchiInput getInchiInputFromInChIImpl(String inchi, String moreOptions) {
		if (useInchiAPI) {
			return InchiAPI.getInchiInputFromInChI(inchi, moreOptions);
		} else {
			if ("fixamide".equals(moreOptions)) {
				notApplicable++;
				System.err.println(moreOptions + "Not Implemented in JnaInchi");
			}
			return JnaInchi.getInchiInputFromInchi(inchi).getInchiInput();
		}
	}

	private static String getInChIFromInchiInputImpl(InchiInput input, String options) {
		if (useInchiAPI) {
			return InchiAPI.getInChIFromInchiInput(input, options);
		} else {
			notApplicable++;
			return null; // not implemented
		}
	}

	private static InchiInput getInchiInputFromMoleculeHandleImpl(Pointer hStatus, Pointer hMolecule,
			String moreOptions) {
		if (useInchiAPI) {		
			return InchiAPI.getInchiInputFromMoleculeHandle(hStatus, hMolecule,moreOptions);
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

	private static String getSmilesFromInChIImpl(String inchi) {
		IAtomContainer mol;
		if (useInchiAPI) {	
			InchiInput input = InchiAPI.getInchiInputFromInChI(inchi, "fixamide");
			mol = getCDKMoleculeFromInchiInput(input);
		} else {
			mol = getCDKMoleculeFromInChI(inchi, null);		
		}
		mol = AtomContainerManipulator.suppressHydrogens(mol);
		return getSmilesFromCDKMolecule(mol);
	}

	private static String getInChIFromCDKMoleculeImpl(IAtomContainer mol, String options) throws CDKException {
		if (useInchiAPI) {
			return getInChIFromInchiInput(getInchiInputFromCDKMolecule(mol), options);
		} else {
			return InChIGeneratorFactory.getInstance().getInChIGenerator(mol, options).getInchi();
		}
	}

	private static IAtomContainer getCDKMoleculeFromInChIImpl(String inchi, String moreOptions) throws CDKException {
		if (useInchiAPI) {
			InchiInput input = InchiAPI.getInchiInputFromInChI(inchi, moreOptions);
			IAtomContainer mol = getCDKMoleculeFromInchiInput(input);
			return AtomContainerManipulator.suppressHydrogens(mol);
		} else {
			if ("fixamide".equals(moreOptions)) {
				notApplicable++;
				System.err.println(moreOptions + "Not Implemented in JnaInchi");
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

}
