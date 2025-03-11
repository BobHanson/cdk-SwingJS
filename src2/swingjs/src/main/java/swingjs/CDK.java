package swingjs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import io.github.dan2097.jnainchi.inchi.InchiLibrary;

public class CDK {

	/**
	 * @j2sAlias get2DMolFromInChI
	 * 
	 * @param inchi
	 * @return
	 */
	public static String get2DMolFromInChI(String inchi) {
		try {
			IAtomContainer mol = getCDKMoleculeFromInChI(inchi);
			return get2DMolFromCDKMolecule(mol);
		} catch (Throwable e) {
			return null;
		}
	} 
	
	/**
	 * @j2sAlias getDataURIFromInChI
	 * 
	 * @param inchi
	 * @return
	 * @throws IOException 
	 * @throws CDKException 
	 */
	public static String getDataURIFromInChI(String inchi) throws CDKException, IOException {
		return getDataURIFromCDKMolecule(getCDKMoleculeFromInChI(inchi));
	} 

	
	/**
	 * @j2sAlias getCDKMoleculeFromInChI
	 * 
	 * @param inchi
	 * @return
	 * @throws CDKException
	 */
	public static IAtomContainer getCDKMoleculeFromInChI(String inchi) throws CDKException {
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		IAtomContainer mol = InChIGeneratorFactory.getInstance().getInChIToStructure(inchi, builder, "")
				.getAtomContainer();
		AtomContainerManipulator.suppressHydrogens(mol);
		new StructureDiagramGenerator().generateCoordinates(mol);
		return mol;
	}

	/**
	 * @j2sAlias get2DMolFromCDKMolecule
	 * @param mol
	 * @return
	 * @throws CDKException
	 * @throws IOException
	 */
    public static String get2DMolFromCDKMolecule(IAtomContainer mol) throws CDKException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		new MDLV2000Writer(os).write(mol);
		String molfile = new String(os.toByteArray());
		os.close();
		return molfile;
	}

	/**
	 * @j2sAlias getDataURIFromCDKMolecule
	 * 
	 * @param mol
	 * @param os
	 * @return
	 * @throws CDKException
	 * @throws IOException
	 */
	public static String getDataURIFromCDKMolecule(IAtomContainer mol) throws CDKException, IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			DepictionGenerator dg = new DepictionGenerator();
			BufferedImage image = dg.depict(mol).toImg();
			ImageIO.write(image, "PNG", os);
			byte[] bytes = Base64.getEncoder().encode(os.toByteArray());
			return "data:image/png;base64," + new String(bytes);
		}
	}	

	public final static void main(String[] args) {
		LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
		InchiLibrary.class.getName();
    }
	
}