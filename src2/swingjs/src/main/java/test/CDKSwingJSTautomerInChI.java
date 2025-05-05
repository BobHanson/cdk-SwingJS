package test;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.LoggingToolFactory;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import io.github.dan2097.jnainchi.InchiAPI;
import io.github.dan2097.jnainchi.inchi.InchiLibrary;
import swingjs.CDK;
import swingjs.SwingJSLogger;

public class CDKSwingJSTautomerInChI {

	private static boolean isJS = /** @j2sNative true || */
			false;

	private final static String outputDir = "C:/temp/tautomers";

	public static void main(String[] args) {
		
		Locale.setDefault(Locale.ROOT);
		
		
		try {
			if (isJS) {
				LoggingToolFactory.setLoggingToolClass(SwingJSLogger.class);
				InchiLibrary.class.getName();
			}
			InchiAPI.initAndRun(() -> {
				byte[][] fileBytes = new byte[2000][];
				test(false, fileBytes);
				test(true, fileBytes);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void test(boolean useApiDefaults, byte[][] fileBytes) {

		// from github.com/WahlOya/Tautobase/blob/master/Tautobase_SMIRKS.txt
		InputStream fis = CDKSwingJSTautomerInChI.class.getResourceAsStream("Tautobase_SMIRKS.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
		String folder = (useApiDefaults ? "fromInchi" : "fromInchi0");
		try {
			FileOutputStream fostab = new FileOutputStream(outputDir + "/smiles_inchi.tab");
			BufferedWriter writertab = new BufferedWriter(new OutputStreamWriter(fostab));
			FileOutputStream foshtml = new FileOutputStream(outputDir + "/" + folder + "/index.html");
			BufferedWriter writerhtml = new BufferedWriter(new OutputStreamWriter(foshtml));
			writerhtml.write("<html><head><style>p{page-break-inside:avoid}</style></head><body><table>\n");
			String line;
			int i = 0;
			int nok = 0;
			int nsame = 0;
			int nchanged = 0;
			while ((line = reader.readLine()) != null) {
				String[] smirks = line.split("\t")[0].split(">>");
				if (smirks.length != 2)
					continue; // header
				String smiles1 = getCanonicalSmiles(smirks[0]);
				String smiles2 = getCanonicalSmiles(smirks[1]);
				String inchi1 = CDK.getInChIFromSmiles(smiles1, "standard");
				String inchi2 = CDK.getInChIFromSmiles(smiles2, "standard");
				++i;
				if (inchi1 == null) {
					System.out.println("null " + i + " " + smirks[0]);
				} else {
					nok++;
				}
				boolean same = (inchi1 != null && inchi1.equals(inchi2));
				writertab.append(smirks[0]).append('\t').append(smirks[1]).append('\t').append(inchi1);
				boolean apiChanged = false;
				if (same) {
					if (nsame % 5 == 0) {
						if (nsame > 0)
							writerhtml.append("</tr></table></td></tr>");
						writerhtml.append("<tr><td><p><table><tr>");
					}
					String smiles = CDK.getSmilesFromInChI(inchi1);
					writertab.append("\t\t").append(smiles);
					String fname = formatInt(i,"0000") + "_" + inchi1.replace('/', '_') + ".png";
					IAtomContainer mol = CDK.getCDKMoleculeFromInChI(inchi1, useApiDefaults ? null : "");
					apiChanged = writeImageForCDKMolecule(mol, folder + "/" + fname, fileBytes, i);
					String inchiKey = CDK.getInchiKeyFromInchi(inchi1);
					writerhtml.append("<td valign=top style='overflow-wrap:anywhere;width:200px;overflow:wrap;"
							+(apiChanged ? "background-color:lightgray" : "")
							+"'>"+i+"<br><img style='width:75%;height:75%' src=" + fname + "/><br>"
							+inchi1
							+"<br><a target=_blank href=\"https://pubchem.ncbi.nlm.nih.gov/#query=" + inchiKey + "\">" + inchiKey + "</a>"
							+"</td>");
					nsame++;
					if (apiChanged)
						nchanged++;
					System.out.println("same " + nsame + ":" + i + " " + apiChanged);
					
				} else {
					writertab.append('\t').append(inchi2);
				}
				writertab.append('\n');
				writerhtml.append('\n');
			}
			writertab.close();
			writerhtml.append("</tr></table></p></td></tr></table></body></html>");
			writerhtml.close();
			System.out.println("n=" + i + " ok=" + nok + " same=" + nsame + " api-changed=" + nchanged);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static String formatInt(int i, String zeros) {
		String s = zeros + i;
		return s.substring(s.length() - zeros.length());
	}

	private static boolean writeImageForCDKMolecule(IAtomContainer mol, String fname, byte[][] fileBytesToCheck, int i) {
		try {
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.generateCoordinates(AtomContainerManipulator.suppressHydrogens(mol));
			Font f = new Font("SansSerif", Font.BOLD, 20);
			DepictionGenerator dg = new DepictionGenerator(f);// .withSize(600, 600);
			BufferedImage image = dg.depict(mol).toImg();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", bos);
			byte[] bytes = bos.toByteArray();
			bos.close();
			OutputStream fos = getOutputStream(outputDir + "/" + fname);
			fos.write(bytes);
			fos.close();
			byte[] fbytes = fileBytesToCheck[i];
			if (fbytes == null) {
				fileBytesToCheck[i] = bytes;
				return false;
			}
			return (fbytes != null && !Arrays.equals(bytes, fbytes));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static OutputStream getOutputStream(String path) throws FileNotFoundException {
		return new FileOutputStream(path);
	}

	private static String getCanonicalSmiles(String smiles) {
		IAtomContainer mol = CDK.getCDKMoleculeFromSmiles(smiles);
		try {
			return new SmilesGenerator(SmiFlavor.Canonical).create(mol);
		} catch (CDKException e) {
			return null;
		}
	}

//	private static String fixSmiles(String s) {
//		String[] a = s.split(":");
//		s = a[0];
//		for (int i = 1; i < a.length; i++) {
//			int pt = a[i].indexOf("]");
//			s += a[i].substring(pt);
//		}
//		return s;
//	}
}