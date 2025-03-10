package test;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import io.github.dan2097.jnainchi.InchiAPI;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;
import swingjs.SwingJSLogger;

public class CDKSwingJSTest {
	private static boolean isJS = /** @j2sNative true || */
			false;

	public static void main(String[] args) {
		try {
			if (isJS) {
				LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
				InchiLibrary.class.getName();
			}
			InchiAPI.initAndRun(() -> {
				test0();
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static IChemObjectBuilder getBuilder() {
		return DefaultChemObjectBuilder.getInstance();
	}

	private static void test0() {
		// N variant
		long t0 = System.currentTimeMillis();

		String inchi = "InChI=1S/C41H45NO21/c43-13-27-32(51)34(53)37(56)40(61-27)59-25-11-19(45)10-21-20(25)12-26(30(42-21)17-4-7-22(46)23(47)9-17)60-41-38(63-39-36(55)31(50)24(48)14-58-39)35(54)33(52)28(62-41)15-57-29(49)8-3-16-1-5-18(44)6-2-16/h1-12,24,27-28,31-41,43-48,50-56H,13-15H2"
				+ "/b8-3+/t24-,27-,28-,31+,32-,33-,34+,35+,36-,37-,38-,39+,40-,41-/m1/s1";

		// ene inchi = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3/b4-3+";

		// allene inchi = "InChI=1S/C6H10/c1-3-5-6-4-2/h3,6H,4H2,1-2H3/t5-/m0/s1";
		try {
			IAtomContainer mol = InChIGeneratorFactory.getInstance().getInChIToStructure(inchi, getBuilder(), "").getAtomContainer();
			mol = AtomContainerManipulator.suppressHydrogens(mol);
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.generateCoordinates(mol);
			String inchi2 = InChIGeneratorFactory.getInstance().getInChIGenerator(mol).getInchi();
			System.out.println(inchi);
			System.out.println(inchi2);
			System.out.println("inchi->mol->inchi " + inchi.equals(inchi2));

			// mol to MOL file
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new MDLV2000Writer(os).write(mol);
			String inchiOut = new String(os.toByteArray());
			
			// inchi to mol
			String smi = new SmilesGenerator(SmiFlavor.Isomeric).create(mol);
			String smilesExpected = "C=1C=C(C=CC1/C=C/C(=O)OC[C@@H]2[C@H]([C@@H]([C@H]([C@H](OC=3C=C4C(C=C(C=C4O[C@H]5[C@@H]([C@H]([C@@H]([C@@H](CO)O5)O)O)O)O)=NC3C=6C=CC(=C(C6)O)O)O2)O[C@H]7[C@@H]([C@H]([C@@H](CO7)O)O)O)O)O)O";
			System.out.println(smilesExpected);
			System.out.println(smi);
			System.out.println("inchi->mol->smiles " + smi.equals(smilesExpected));

			
			mol = new SmilesParser(getBuilder()).parseSmiles(smi);
			// smiles to mol to inchi
			String inchi3 = InChIGeneratorFactory.getInstance().getInChIGenerator(mol).getInchi();
			System.out.println(inchi3);
			System.out.println("inchi->mol->smiles->mol->inchi " + inchi3.equals(inchi));
//			getImagesForInChI(inchi2);
			
			String s = getDataURIFromInChI(inchi2);
			
			/**
			 * @j2sNative 
			 * 
			 * $("body").append("<img src='" + s + "'>");
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println((System.currentTimeMillis() - t0) + " ms");
	}

	public static String getImagesForInChI(String inchi) {
		// inchi = "InChI=1S/C2H6O/c1-3-2/h1-2H3";
		// inchi = "InChI=1S/H2O/h1H2";
		try {
			IAtomContainer mol = AtomContainerManipulator.suppressHydrogens(InChIGeneratorFactory.getInstance()
					.getInChIToStructure(inchi, getBuilder(), "").getAtomContainer());
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.generateCoordinates(AtomContainerManipulator.suppressHydrogens(mol));

			Font f = new Font("SansSerif", Font.BOLD, 20);
			DepictionGenerator dg = new DepictionGenerator(f).withSize(600, 600);
			BufferedImage image = dg.depict(mol).toImg();

			// ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileOutputStream bos = new FileOutputStream("c:/temp/testcdk.png");
			ImageIO.write(image, "PNG", bos);
			bos.close();
			System.out.println("textcdk.png created");
			bos = new FileOutputStream("c:/temp/testcdk.svg");
			String svg = dg.depict(mol).toSvgStr(Depiction.UNITS_PX);
			bos.write(svg.getBytes());
			bos.close();
			System.out.println("textcdk.svg created");
//            bos = new FileOutputStream("c:/temp/testcdk.pdf");
//            dg.depict(mol).writeTo(Depiction.PDF_FMT, bos);
//            bos.close();
//            System.out.println("textcdk.pdf created");

//            String s = new BASE64Encoder().encode(bos.toByteArray());
			return "";// "data:image/png;base64," + s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

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
	

//    public static void testFonts() {
//        // used for testing font business
//        String[] s = new String[] { "H", "j", "Hj", "HjHjHj" };
//        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
//        for (int i = 0; i < s.length; i++) {
//            TextOutline t = new TextOutline(s[i], f);
//        }
//    }
}