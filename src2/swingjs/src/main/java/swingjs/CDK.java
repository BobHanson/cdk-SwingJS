package swingjs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
	 * @j2sAlias getDataURIFromInChI
	 * 
	 * @param inchi
	 * @return
	 */
	public static String getDataURIFromInChI(String inchi) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
	    	IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
			IAtomContainer mol = InChIGeneratorFactory.getInstance()
					.getInChIToStructure(inchi, builder, "")
					.getAtomContainer();
			mol = AtomContainerManipulator.suppressHydrogens(mol);
			new StructureDiagramGenerator().generateCoordinates(mol);
			DepictionGenerator dg = new DepictionGenerator();
			BufferedImage image = dg.depict(mol).toImg();
			ImageIO.write(image, "PNG", os);
			byte[] bytes = Base64.getEncoder().encode(os.toByteArray());
			return "data:image/png;base64," + new String(bytes);
		} catch (Throwable e) {
			return null;
		}
	} 
	
	/**
	 * @j2sAlias get2DMolFromInChI
	 * 
	 * @param inchi
	 * @return
	 */
	public static String get2DMolFromInChI(String inchi) {
		try {
	    	IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
			IAtomContainer mol = InChIGeneratorFactory.getInstance()
					.getInChIToStructure(inchi, builder, "")
					.getAtomContainer();
			mol = AtomContainerManipulator.suppressHydrogens(mol);
			new StructureDiagramGenerator().generateCoordinates(mol);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new MDLV2000Writer(os).write(mol);
			return new String(os.toByteArray());
		} catch (Throwable e) {
			return null;
		}
	} 
	
	
	
    public final static void main(String[] args) {
		LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
		InchiLibrary.class.getName();
    }
	
}