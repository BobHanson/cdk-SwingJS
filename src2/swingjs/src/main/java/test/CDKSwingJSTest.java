package test;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.generators.standard.TextOutline;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import io.github.dan2097.jnainchi.InchiAPI;
import io.github.dan2097.jnainchi.InchiInput;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;
import swingjs.SwingJSLogger;

public class CDKSwingJSTest {
	private static final boolean writeFiles = false;
	
	private static boolean isJS = /** @j2sNative true || */
			false;

	public static void main(String[] args) {
		
		Locale.setDefault(Locale.ROOT);
		
		
		try {
			if (isJS) {
				LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
				InchiLibrary.class.getName();
			}
			InchiAPI.initAndRun(() -> {
				//testFonts(); //
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

		
		
		
		long t0 = System.currentTimeMillis();

		
		
		
		
		String auxinfo = "AuxInfo=1/0/N:1,3,5,2,6,4/E:(1,2,3,4,5,6)/rA:6nCCCCCC/rB:;d1s2;d2;s1;s4d5;/rC:7.8848,-4.7251,0;9.6152,-4.7246,0;8.7516,-4.225,0;9.6152,-5.7255,0;7.8848,-5.73,0;8.7538,-6.225,0;";

		String molFile = "https://cactus.nci.nih.gov/chemical/structure/C/file?format=sdf&get3d=true\n" + 
				"__Jmol-16.07212415433D 1   1.00000     0.00000     0\n" + 
				"Jmol version 16.2.20  2023-01-10 13:20 EXTRACT: ({0:4})\n" + 
				"  5  4  0  0  0  0            999 V2000\n" + 
				"    0.0000   -0.0000    0.0000 C   0  0  0  0  0  0\n" + 
				"    0.0000   -0.8900   -0.6293 H   0  0  0  0  0  0\n" + 
				"    0.0000    0.8900   -0.6293 H   0  0  0  0  0  0\n" + 
				"   -0.8900   -0.0000    0.6293 H   0  0  0  0  0  0\n" + 
				"    0.8900   -0.0000    0.6293 H   0  0  0  0  0  0\n" + 
				"  1  2  1\n" + 
				"  1  3  1\n" + 
				"  1  4  1\n" + 
				"  1  5  1\n" + 
				"M  END\n" + 
				"";
		
		molFile = propanamide_mol;

		String inchi = "InChI=1S/C41H45NO21/c43-13-27-32(51)34(53)37(56)40(61-27)59-25-11-19(45)10-21-20(25)12-26(30(42-21)17-4-7-22(46)23(47)9-17)60-41-38(63-39-36(55)31(50)24(48)14-58-39)35(54)33(52)28(62-41)15-57-29(49)8-3-16-1-5-18(44)6-2-16/h1-12,24,27-28,31-41,43-48,50-56H,13-15H2"
				+ "/b8-3+/t24-,27-,28-,31+,32-,33-,34+,35+,36-,37-,38-,39+,40-,41-/m1/s1";

		String smiles = "O=C1-NC=C1";
		
		
		String taxol = "InChI=1S/C47H51NO14/c1-25-31(60-43(56)36(52)35(28-16-10-7-11-17-28)48-41(54)29-18-12-8-13-19-29)23-47(57)40(61-42(55)30-20-14-9-15-21-30)38-45(6,32(51)22-33-46(38,24-58-33)62-27(3)50)39(53)37(59-26(2)49)34(25)44(47,4)5/h7-21,31-33,35-38,40,51-52,57H,22-24H2,1-6H3,(H,48,54)/t31-,32-,33+,35-,36+,37+,38-,40-,45+,46-,47+/m0/s1";

		
		// tautomer test
		
		String allopurinol = "InChI=1S/C5H4N4O/c10-5-3-1-8-9-4(3)6-2-7-5/h1-2H,(H2,6,7,8,9,10)";

		String molfileap = "C5H4N4O\r\n" + 
				"APtclcactv05012509183D 0   0.00000     0.00000\r\n" + 
				" \r\n" + 
				" 14 15  0  0  0  0  0  0  0  0999 V2000\r\n" + 
				"   -3.3374   -0.8489    0.0013 H   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -2.5976    1.5753    0.0010 H   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"    1.6928   -1.9041   -0.0012 O   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"    1.2426   -0.7691   -0.0011 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -1.3304   -1.3277    0.0003 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"    1.5607    1.5217   -0.0016 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -0.2047   -0.5227   -0.0006 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -0.6419    0.8291   -0.0007 C   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -2.4181   -0.5394    0.0009 N   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -2.0119    0.8021    0.0003 N   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"    0.2680    1.7906   -0.0011 N   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"    2.0527    0.3035    0.0041 N   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"   -1.3338   -2.4077    0.0006 H   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"    2.2532    2.3504    0.0027 H   0  0  0  0  0  0  0  0  0  0  0  0\r\n" + 
				"  3  4  2  0  0  0  0\r\n" + 
				"  9 10  1  0  0  0  0\r\n" + 
				"  5  9  1  0  0  0  0\r\n" + 
				"  1  9  1  0  0  0  0\r\n" + 
				"  8 10  1  0  0  0  0\r\n" + 
				"  2 10  1  0  0  0  0\r\n" + 
				"  4 12  1  0  0  0  0\r\n" + 
				"  6 12  2  0  0  0  0\r\n" + 
				"  8 11  2  0  0  0  0\r\n" + 
				"  6 11  1  0  0  0  0\r\n" + 
				"  5  7  2  0  0  0  0\r\n" + 
				"  5 13  1  0  0  0  0\r\n" + 
				"  4  7  1  0  0  0  0\r\n" + 
				"  7  8  1  0  0  0  0\r\n" + 
				"  6 14  1  0  0  0  0\r\n" + 
				"M  END\r\n" + 
				"$$$$\r\n" + 
				"";

		// phenol-carboxylate test

		String inchipc = "InChI=1S/C9H8O3/c10-8-4-1-7(2-5-8)3-6-9(11)12/h1-6,10H,(H,11,12)/p-1/b6-3+";
		
		// ene inchi = "InChI=1S/C4H8/c1-3-4-2/h3-4H,1-2H3/b4-3+";
		// morphine 
		//inchi = "InChI=1S/C17H19NO3/c1-18-7-6-17-10-3-5-13(20)16(17)21-15-12(19)4-2-9(14(15)17)8-11(10)18/h2-5,10-11,13,16,19-20H,6-8H2,1H3/t10-,11+,13-,16-,17-/m0/s1";

		// allene inchi = "InChI=1S/C6H10/c1-3-5-6-4-2/h3,6H,4H2,1-2H3/t5-/m0/s1";
		// 2-propanol inchi = "InChI=1S/C4H10O/c1-3-4(2)5/h4-5H,3H2,1-2H3/t4-/m0/s1";
		try {
			
			// this first method DOES NOT WORK 
			// because inchi C does not produce the same ordering of atoms
			// from a MOL file. 
			InchiInput inputap = InchiAPI.getInchiInputFromInChI(allopurinol, null);
			IAtomContainer molap = new InChIToStructure(inputap, DefaultChemObjectBuilder.getInstance()) {
			}.getAtomContainer();
			// we must first generate the InChI, and then use that.
			InchiInput inputapFromMol = InchiAPI.getInchiInputFromMolFile(molfileap);
			String inchiapFromMol = InchiAPI.getInChIFromInchiInput(inputapFromMol, null);
			InchiInput inputapFromMolViaInchi = InchiAPI.getInchiInputFromInChI(inchiapFromMol, null);
			IAtomContainer molapViaInchi = new InChIToStructure(inputapFromMolViaInchi, DefaultChemObjectBuilder.getInstance()) {
			}.getAtomContainer();
			
		
			InchiInput inputpc = InchiAPI.getInchiInputFromInChI(inchipc, "fixacid");
			IAtomContainer molpc = new InChIToStructure(inputpc, DefaultChemObjectBuilder.getInstance()) {
			}.getAtomContainer();
		    getImagesForCDKMolecule(molpc);
			

			// taxol test
			getImagesForInChI(taxol);
			
			
			// amide test
			getImagesForInChI("InChI=1S/C3H7NO/c1-2-3(4)5/h2H2,1H3,(H2,4,5)");

			getImagesForSMILES(smiles);

			if(true)
				return;
//			String abc ="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
//			checkABC(abc, "lt");
//			checkABC(abc, "tr");
//			checkABC(abc, "az");
//
			IAtomContainer mol;
		
			// molfile -> image, amide fix
			
			InchiInput input = InchiAPI.getInchiInputFromMolFile(molFile, "fixamide");
			mol = new InChIToStructure(input, DefaultChemObjectBuilder.getInstance()) {
			}.getAtomContainer();
			System.out.println(mol.getAtomCount());
//			mol = AtomContainerManipulator.suppressHydrogens(mol);
		    getDataURIFromCDKMolecule(mol);
		    getImagesForCDKMolecule(mol);

		    
			String ikey = InchiAPI.inchiToInchiKey(inchi).getInchiKey();
			System.out.println(ikey);
			System.out.println("MSKVTYYOOJREKU-STJZCQMKSA-N".equals(ikey));

			getDataURIForCDKMolecule(mol);

			
		    
		    // inchi->mol
			
			
			
			mol = new InChIToStructure(inchi, getBuilder(), "fixamide") {}.getAtomContainer();
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
			
			// smiles to mol to inchi
			mol = new SmilesParser(getBuilder()).parseSmiles(smi);
			String inchi3 = InChIGeneratorFactory.getInstance().getInChIGenerator(mol).getInchi();
			System.out.println(inchi3);
			System.out.println("inchi->mol->smiles->mol->inchi " + inchi3.equals(inchi));
			
			getImagesForInChI(inchi2);

			// to image 

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println((System.currentTimeMillis() - t0) + " ms");
	}

	private static void checkABC(String abc, String la) {
		String abclc = abc.toLowerCase(Locale.ROOT);
		String abcuc = abc.toUpperCase(Locale.ROOT);
		String abclalc = abc.toLowerCase(Locale.forLanguageTag(la));
		String abclauc = abc.toUpperCase(Locale.forLanguageTag(la));
		for (int i = 0;i < abc.length(); i++) {
			if (abclc.codePointAt(i) != abclalc.codePointAt(i))
				System.out.println(la + " " + i + " " + abc.charAt(i) + " " + abclalc.charAt(i));
			if (abcuc.codePointAt(i) != abclauc.codePointAt(i))
				System.out.println(la + " " + i + " " + abc.charAt(i) + " " + abclauc.charAt(i));
		}
		
	}

	public static String getImagesForInChI(String inchi) {
		// inchi = "InChI=1S/C2H6O/c1-3-2/h1-2H3";
		// inchi = "InChI=1S/H2O/h1H2";
		try {
			IAtomContainer mol = getMolFixed(inchi, true, true);
			String s = getImagesForCDKMolecule(mol);
			getDataURIForCDKMolecule(mol);
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static String getImagesForSMILES(String smiles) {
		try {
	        SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
	        IAtomContainer mol = sp.parseSmiles(smiles);
			String s = getImagesForCDKMolecule(mol);
			getDataURIForCDKMolecule(mol);
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	private static IAtomContainer getMolFixed(String inchi, boolean fixAmides, boolean suppressHydrogens) throws CDKException {
		InchiInput input = InchiAPI.getInchiInputFromInChI(inchi, fixAmides ? "fixamide" : "");
		IAtomContainer mol = new InChIToStructure(input, DefaultChemObjectBuilder.getInstance()) {
		}.getAtomContainer();
		if (suppressHydrogens)
			mol = AtomContainerManipulator.suppressHydrogens(mol);
		return mol;
	}

	private static String getImagesForCDKMolecule(IAtomContainer mol) throws Exception {
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.generateCoordinates(AtomContainerManipulator.suppressHydrogens(mol));

		Font f = new Font("SansSerif", Font.BOLD, 20);
		DepictionGenerator dg = new DepictionGenerator(f).withSize(600, 600);
		BufferedImage image = dg.depict(mol).toImg();

		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStream bos = getOutputStream("c:/temp/testcdk.png");
		ImageIO.write(image, "PNG", bos);
		bos.close();
		bos = getOutputStream("c:/temp/testcdk.svg");
		String svg = dg.depict(mol).toSvgStr(Depiction.UNITS_PX);
		bos.write(svg.getBytes());
		bos.close();
//        bos = new FileOutputStream("c:/temp/testcdk.pdf");
//        dg.depict(mol).writeTo(Depiction.PDF_FMT, bos);
//        bos.close();
//        System.out.println("textcdk.pdf created");

//        String s = new BASE64Encoder().encode(bos.toByteArray());
		return "";// "data:image/png;base64," + s;
	}

	private static OutputStream getOutputStream(String path) throws FileNotFoundException {
		if (writeFiles)
			System.out.println(path + " created");
		return (writeFiles ? new FileOutputStream(path) : new ByteArrayOutputStream());
	}

	public static String getDataURIForCDKMolecule(IAtomContainer mol) {
		return getDataURIFromMolOrInChI(mol, null);
	}

	public static String getDataURIForInChI(String inchi) {		
		return getDataURIFromMolOrInChI(null, inchi);
	}

	/**
	 * @j2sAlias getDataURIFromInChI
	 * 
	 * @param inchi
	 * @return
	 */
	public static String getDataURIFromMolOrInChI(IAtomContainer mol, String inchi) {
		try {
			if (mol == null)
				mol = getMolFixed(inchi, true, true);
			String s = getDataURIFromCDKMolecule(mol);
			if (!writeFiles)
				return s;
			/**
			 * @j2sNative 
			 * 
			 * $("body").append("<img src='" + s + "'>");
			 */
			return s;
		} catch (Throwable e) {
			return null;
		}
	} 
	
    final static String propanamide_mol = "C3H7NO\n" + 
    		"APtclcactv03172517403D 0   0.00000     0.00000\n" + 
    		" \n" + 
    		" 12 11  0  0  0  0  0  0  0  0999 V2000\n" + 
    		"    0.5021    0.0389   -0.0019 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -0.7264   -0.8339   -0.0014 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -1.9776    0.0468    0.0014 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"    0.3882    1.2463   -0.0002 O   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"    1.7275   -0.5224    0.0013 N   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -0.7263   -1.4620   -0.8922 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -0.7239   -1.4644    0.8877 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -1.9778    0.6749    0.8923 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -1.9802    0.6772   -0.8877 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"   -2.8662   -0.5845    0.0018 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"    1.8186   -1.4881    0.0039 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"    2.5182    0.0394    0.0010 H   0  0  0  0  0  0  0  0  0  0  0  0\n" + 
    		"  1  2  1  0  0  0  0\n" + 
    		"  2  3  1  0  0  0  0\n" + 
    		"  1  4  2  0  0  0  0\n" + 
    		"  1  5  1  0  0  0  0\n" + 
    		"  2  6  1  0  0  0  0\n" + 
    		"  2  7  1  0  0  0  0\n" + 
    		"  3  8  1  0  0  0  0\n" + 
    		"  3  9  1  0  0  0  0\n" + 
    		"  3 10  1  0  0  0  0\n" + 
    		"  5 11  1  0  0  0  0\n" + 
    		"  5 12  1  0  0  0  0\n" + 
    		"M  END\n" + 
    		"$$$$\n" + 
    		"";

	private static String getDataURIFromCDKMolecule(IAtomContainer mol) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
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
	
    public static void testFonts() {
        // used for testing font business
        String[] s = new String[] { "|","H", "j", "Hj", "HjHjHj" };
        Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        System.out.println(f);
        for (int i = 0; i < s.length; i++) {
            TextOutline t = new TextOutline(s[i], f);
        }
    }
}