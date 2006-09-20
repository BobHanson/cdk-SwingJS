/*  $RCSfile: $
 *  $Author: egonw $
 *  $Date: 2006-04-20 17:59:04 -0400 (Thu, 20 Apr 2006) $
 *  $Revision: 6064 $
 *
 *  Copyright (C) 2002-2006  The Chemistry Development Kit (CDK) project
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All I ask is that proper credit is given for my work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.smiles;

import java.util.ArrayList;

import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.Ring;
import org.openscience.cdk.aromaticity.HueckelAromaticityDetector;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.tools.HydrogenAdder;
import org.openscience.cdk.tools.IValencyChecker;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.SmilesValencyChecker;

/**
 * Tool that tries to deduce bond orders based on connectivity and hybridization
 * for a number of common ring systems.
 *
 * @author Todd Martin
 * @cdk.module smiles
 * @cdk.keyword bond order
 */
public class DeduceBondSystemTool {

    private LoggingTool logger;
    private HydrogenAdder hAdder;
    private IValencyChecker valencyChecker;
	private int status = 0;
	
	private int counter = 0;

    /**
     * Constructor for the DeduceBondSystemTool object.
     */
    public DeduceBondSystemTool() {
        logger = new LoggingTool(this);
        valencyChecker = new SmilesValencyChecker();
        hAdder = new HydrogenAdder();
    }

    public IMolecule fixAromaticBondOrders(IMolecule molecule) {
        //System.out.println("here");

        IRingSet ringSet = null;

        // TODO remove rings with nonsp2 carbons(?) and rings larger than 7 atoms
        ringSet = removeExtraRings(molecule);

//		System.out.println(rs.getAtomContainerCount());

        ArrayList MasterList = new ArrayList();

        //this.counter=0;// counter which keeps track of all current possibilities for placing double bonds


        for (int i = 0; i <= ringSet.getAtomContainerCount() - 1; i++) {

            IRing ring = (IRing) ringSet.getAtomContainer(i);

            if (ring.getAtomCount() == 5) {
                fiveMemberedRingPossibilities(molecule, ring, MasterList);
            } else if (ring.getAtomCount() == 6) {
                sixMemberedRingPossibilities(molecule, ring, MasterList);
            } else {
                sevenMemberedRingPossibilities(molecule, ring, MasterList);
                //TODO- add code for all 7 membered aromatic ring possibilities not just 3 bonds
            }
        }

        IMoleculeSet som = molecule.getBuilder().newMoleculeSet();

//		int number=1; // total number of possibilities
//		
//		for (int ii=0;ii<=MasterList.size()-1;ii++) {
//		ArrayList ringlist=(ArrayList)MasterList.get(ii);
//		number*=ringlist.size();
//		}
//		System.out.println("number= "+number);			


        int [] choices = null;

        //if (number> 1000000) return null;

        choices = new int [MasterList.size()];

        if (MasterList.size() > 0) {
            IMolecule iMolecule = loop(System.currentTimeMillis(), molecule, 0, MasterList, choices, som);
            if (iMolecule instanceof IMolecule) return iMolecule;
        }


        int mincount = 99999999;

        int best = -1; // one with minimum number of bad atoms

        // minimize number of potentially bad nitrogens among molecules in the set

        for (int i = 0; i <= som.getAtomContainerCount() - 1; i++) {

            IMolecule mol = som.getMolecule(i);

            ringSet = removeExtraRings(mol);
            int count = getBadCount(mol, ringSet);

            //System.out.println(i + "\t" + count);

            if (count < mincount) {
                mincount = count;
                best = i;
            }
            
        }

        if (som.getAtomContainerCount() > 0) return som.getMolecule(best);
        return null;
    }


    private void applyBonds(IMolecule m, ArrayList al) {

        //System.out.println("");

        for (int i = 0; i <= al.size() - 1; i++) {

            String s = (String) al.get(i);
            String s1 = s.substring(0, s.indexOf("-"));
            String s2 = s.substring(s.indexOf("-") + 1, s.length());

            int i1 = Integer.parseInt(s1);
            int i2 = Integer.parseInt(s2);

            //System.out.println(s1+"\t"+s2);

            IBond b = m.getBond(m.getAtom(i1), m.getAtom(i2));
            b.setOrder(2);


        }

    }

    private void fiveMemberedRingPossibilities(IMolecule m, IRing r, ArrayList MasterList) {
        // 5 possibilities for placing 2 double bonds
        // 5 possibilities for placing 1 double bond

        int [] num = new int [5]; // stores atom numbers based on atom numbers in molecule instead of ring

        for (int j = 0; j <= 4; j++) {
            num[j] = m.getAtomNumber(r.getAtom(j));
            //System.out.println(num[j]);
        }

        java.util.ArrayList al1 = new java.util.ArrayList();
        java.util.ArrayList al2 = new java.util.ArrayList();
        java.util.ArrayList al3 = new java.util.ArrayList();
        java.util.ArrayList al4 = new java.util.ArrayList();
        java.util.ArrayList al5 = new java.util.ArrayList();

        java.util.ArrayList al6 = new java.util.ArrayList();
        java.util.ArrayList al7 = new java.util.ArrayList();
        java.util.ArrayList al8 = new java.util.ArrayList();
        java.util.ArrayList al9 = new java.util.ArrayList();
        java.util.ArrayList al10 = new java.util.ArrayList();

		java.util.ArrayList al11=new java.util.ArrayList(); // no bonds

        al1.add(num[1] + "-" + num[2]);
        al1.add(num[3] + "-" + num[4]);

        al2.add(num[2] + "-" + num[3]);
        al2.add(num[0] + "-" + num[4]);

        al3.add(num[0] + "-" + num[1]);
        al3.add(num[3] + "-" + num[4]);

        al4.add(num[0] + "-" + num[4]);
        al4.add(num[1] + "-" + num[2]);

        al5.add(num[0] + "-" + num[1]);
        al5.add(num[2] + "-" + num[3]);

        al6.add(num[0] + "-" + num[1]);
        al7.add(num[1] + "-" + num[2]);
        al8.add(num[2] + "-" + num[3]);
        al9.add(num[3] + "-" + num[4]);
        al10.add(num[4] + "-" + num[0]);

        ArrayList mal = new ArrayList();

        mal.add(al1);
        mal.add(al2);
        mal.add(al3);
        mal.add(al4);
        mal.add(al5);

        mal.add(al6);
        mal.add(al7);
        mal.add(al8);
        mal.add(al9);
        mal.add(al10);

//		mal.add(al11);

        MasterList.add(mal);

    }

    private void sixMemberedRingPossibilities(IMolecule m, IRing r, ArrayList MasterList) {
        // 2 possibilities for placing 3 double bonds
        // 6 possibilities for placing 2 double bonds
        // 6 possibilities for placing 1 double bonds

        IAtom [] ringatoms = new Atom [6];

        ringatoms[0] = r.getAtom(0);

        int [] num = new int [6];

        for (int j = 0; j <= 5; j++) {
            num[j] = m.getAtomNumber(r.getAtom(j));
        }

        java.util.ArrayList al1 = new java.util.ArrayList();
        java.util.ArrayList al2 = new java.util.ArrayList();

        al1.add(num[0] + "-" + num[1]);
        al1.add(num[2] + "-" + num[3]);
        al1.add(num[4] + "-" + num[5]);

        al2.add(num[1] + "-" + num[2]);
        al2.add(num[3] + "-" + num[4]);
        al2.add(num[5] + "-" + num[0]);

        java.util.ArrayList al3 = new java.util.ArrayList();
        java.util.ArrayList al4 = new java.util.ArrayList();
        java.util.ArrayList al5 = new java.util.ArrayList();
        java.util.ArrayList al6 = new java.util.ArrayList();
        java.util.ArrayList al7 = new java.util.ArrayList();
        java.util.ArrayList al8 = new java.util.ArrayList();
        java.util.ArrayList al9 = new java.util.ArrayList();
        java.util.ArrayList al10 = new java.util.ArrayList();
        java.util.ArrayList al11 = new java.util.ArrayList();

        java.util.ArrayList al12 = new java.util.ArrayList();
        java.util.ArrayList al13 = new java.util.ArrayList();
        java.util.ArrayList al14 = new java.util.ArrayList();
        java.util.ArrayList al15 = new java.util.ArrayList();
        java.util.ArrayList al16 = new java.util.ArrayList();
        java.util.ArrayList al17 = new java.util.ArrayList();

        java.util.ArrayList al18 = new java.util.ArrayList();


        al3.add(num[0] + "-" + num[1]);
        al3.add(num[2] + "-" + num[3]);

        al4.add(num[0] + "-" + num[1]);
        al4.add(num[4] + "-" + num[5]);

        al5.add(num[1] + "-" + num[2]);
        al5.add(num[3] + "-" + num[4]);

        al6.add(num[1] + "-" + num[2]);
        al6.add(num[0] + "-" + num[5]);

        al7.add(num[2] + "-" + num[3]);
        al7.add(num[4] + "-" + num[5]);

        al8.add(num[0] + "-" + num[5]);
        al8.add(num[3] + "-" + num[4]);

        al9.add(num[0] + "-" + num[1]);
        al9.add(num[3] + "-" + num[4]);

        al10.add(num[1] + "-" + num[2]);
        al10.add(num[4] + "-" + num[5]);

        al11.add(num[2] + "-" + num[3]);
        al11.add(num[0] + "-" + num[5]);

        al12.add(num[0] + "-" + num[1]);
        al13.add(num[1] + "-" + num[2]);
        al14.add(num[2] + "-" + num[3]);
        al15.add(num[3] + "-" + num[4]);
        al16.add(num[4] + "-" + num[5]);
        al17.add(num[5] + "-" + num[0]);

        ArrayList mal = new ArrayList();

        mal.add(al1);
        mal.add(al2);

        mal.add(al3);
        mal.add(al4);
        mal.add(al5);
        mal.add(al6);
        mal.add(al7);
        mal.add(al8);
        mal.add(al9);
        mal.add(al10);
        mal.add(al11);

        mal.add(al12);
        mal.add(al13);
        mal.add(al14);
        mal.add(al15);
        mal.add(al16);
        mal.add(al17);
        mal.add(al18);

        MasterList.add(mal);


    }

    private void sevenMemberedRingPossibilities(IMolecule m, IRing r, ArrayList MasterList) {
        // for now only consider case where have 3 double bonds

        IAtom[] ringatoms = new Atom[7];

        ringatoms[0] = r.getAtom(0);

        int[] num = new int[7];

        for (int j = 0; j <= 6; j++) {
            num[j] = m.getAtomNumber(r.getAtom(j));
        }

        java.util.ArrayList al1 = new java.util.ArrayList();
        java.util.ArrayList al2 = new java.util.ArrayList();
        java.util.ArrayList al3 = new java.util.ArrayList();
        java.util.ArrayList al4 = new java.util.ArrayList();
        java.util.ArrayList al5 = new java.util.ArrayList();

        al1.add(num[0] + "-" + num[1]);
        al1.add(num[2] + "-" + num[3]);
        al1.add(num[4] + "-" + num[5]);

        al2.add(num[0] + "-" + num[1]);
        al2.add(num[2] + "-" + num[3]);
        al2.add(num[5] + "-" + num[6]);

        al3.add(num[1] + "-" + num[2]);
        al3.add(num[3] + "-" + num[4]);
        al3.add(num[5] + "-" + num[6]);

        al4.add(num[1] + "-" + num[2]);
        al4.add(num[3] + "-" + num[4]);
        al4.add(num[6] + "-" + num[0]);

        al5.add(num[2] + "-" + num[3]);
        al5.add(num[4] + "-" + num[5]);
        al5.add(num[6] + "-" + num[0]);

        ArrayList mal = new ArrayList();

        mal.add(al1);
        mal.add(al2);
        mal.add(al3);
        mal.add(al4);
        mal.add(al5);

        MasterList.add(mal);


    }


    public int getBadCount(IMolecule molecule, IRingSet ringSet) {
        // finds count of nitrogens in the rings that have 4 bonds
        // to non hydrogen atoms and one to hydrogen
        // or nitrogens with 2 double bonds to atoms in the ringset
        // or have S atom with more than 2 bonds
        // these arent necessarily bad- just unlikely


        int count = 0;

        for (int j = 0; j <= molecule.getAtomCount() - 1; j++) {
            IAtom atom = molecule.getAtom(j);

            //System.out.println(mol.getBondOrderSum(a));

            if (inRingSet(atom, ringSet)) {
                //System.out.println("in ring set");

                if (atom.getSymbol().equals("N")) {
                    if (atom.getFormalCharge() == 0) {
//						System.out.println(mol.getBondOrderSum(a));
                        if (molecule.getBondOrderSum(atom) == 4) {
                            count++; //
                        } else if (molecule.getBondOrderSum(atom) == 5) {
                            // check if have 2 double bonds to atom in ring
                            int doublebondcount = 0;
                            IAtom [] ca = molecule.getConnectedAtoms(atom);

                            for (int k = 0; k <= ca.length - 1; k++) {
                                if (molecule.getBond(atom, ca[k]).getOrder() == 2) {
                                    if (inRingSet(ca[k], ringSet)) {
                                        doublebondcount++;
                                    }
                                }
                            }

                            if (doublebondcount == 2) {
                                count++;
                            }


                        }
                    } else if (atom.getFormalCharge() == 1) {
                        if (molecule.getBondOrderSum(atom) == 5) {
                            count++;
                        }
                    }
                } else if (atom.getSymbol().equals("S")) {
                    if (molecule.getBondOrderSum(atom) > 2) {
                        count++;
                    }
                }
            }
        }
        //System.out.println("here bad count = " + count);

        return count;
    }


    public boolean inRingSet(IAtom atom, IRingSet ringSet) {
        for (int i = 0; i < ringSet.getAtomContainerCount(); i++) {
            Ring ring = (Ring) ringSet.getAtomContainer(i);
            if (ring.contains(atom)) return true;
        }
        return false;
    }

    private IMolecule loop(long starttime, IMolecule molecule, int index, ArrayList MasterList, int [] choices, IMoleculeSet som) {

        //System.out.println(System.currentTimeMillis());

        long time = System.currentTimeMillis();

        long diff = time - starttime;

        if (diff > 100000) return null; //time out after 100 seconds

        ArrayList ringlist = (ArrayList) MasterList.get(index);

        IMolecule mnew2 = null;

        for (int i = 0; i <= ringlist.size() - 1; i++) {

            choices[index] = i;

            if (index == MasterList.size() - 1) {
                //System.out.println(choices[0]+"\t"+choices[1]);

                IMolecule mnew = null;
                try {
                    mnew = (Molecule) molecule.clone();
                } catch (Exception e) {
                    logger.error("Failed to clone molecule: ", e.getMessage());
                    logger.debug(e);
                }

                for (int j = 0; j <= MasterList.size() - 1; j++) {
                    ArrayList ringlist2 = (ArrayList) MasterList.get(j);
                    ArrayList bondlist = (ArrayList) ringlist2.get(choices[j]);
//					System.out.println(j+"\t"+choices[j]);
                    applyBonds(mnew, bondlist);
                }
//				System.out.println("");
                counter++;

                try {
                    HydrogenAdder hydrogenAdder = null;
                    hydrogenAdder = new HydrogenAdder("org.openscience.cdk.tools.ValencyChecker");
                    hydrogenAdder.addImplicitHydrogensToSatisfyValency(mnew);
                } catch (Exception e) {
                    logger.error("Failed to add hydrogens: ", e.getMessage());
                    logger.debug(e);
                }


                if (isStructureOK(mnew)) {

                    IRingSet rs = this.removeExtraRings(mnew); // need to redo this since created new molecule (mnew)

                    int count = this.getBadCount(mnew, rs);
                    //System.out.println("bad count="+count);

                    if (count == 0) {
//						System.out.println("found match after "+counter+" iterations");
                        return mnew; // dont worry about adding to set just finish
                    } else {
                        som.addMolecule(mnew);
                    }
                }

            }

            if (index + 1 <= MasterList.size() - 1) {
                //System.out.println("here3="+counter);
                mnew2 = loop(starttime, molecule, index + 1, MasterList, choices, som); //recursive def
            }


            if (mnew2 instanceof IMolecule) {
                return mnew2;
            }
        }
        return null;

    }

    public boolean isStructureOK(IMolecule molecule) {
        for (int i = 0; i <= molecule.getAtomCount() - 1; i++) {
            //System.out.println(mj.getBondOrderSum(mj.getAtomAt(i)));
            try {
                // Note: valencyHybridChecker.couldMatchAtomType shouldnt check Hybridization to get it to work for non carbon atoms
                valencyChecker.isSaturated(molecule.getAtom(i), molecule);
                //valencyChecker.allSaturated didnt seem to work so did it this way
            } catch (Exception e) {
                logger.debug(i + "\t" + "atom " + (i + 1) + " is not saturated");
                e.printStackTrace();
                return false;
            }
        }

        try {
            AllRingsFinder arf = new AllRingsFinder();
            IRingSet ringSet = arf.findAllRings(molecule);

//			IRingSet rs=this.RemoveExtraRings(m);


            for (int i = 0; i <= molecule.getAtomCount() - 1; i++) {
                molecule.getAtom(i).setFlag(CDKConstants.ISAROMATIC, false);
            }

            for (int i = 0; i <= ringSet.getAtomContainerCount() - 1; i++) {
                Ring r = (Ring) ringSet.getAtomContainer(i);
                r.setFlag(CDKConstants.ISAROMATIC, false);
            }

            //System.out.println("Rs size= "+rs.size());

            // do it multiple times to catch all the aromatic rings
            // this problem is that the aromaticity detector relies on
            // the aromaticity of the individual atoms in the ring
            // these wont be picked up until you do it multiple times
            // for example pyrene 129-00-0

            for (int i = 0; i <= ringSet.getAtomContainerCount() - 1; i++) {
                HueckelAromaticityDetector.detectAromaticity(molecule, ringSet, false);
            }

//			Figure out which rings we want to make sure are aromatic:
            boolean [] Check = this.findRingsToCheck(molecule, ringSet);

//			for (int i=0;i<=Check.length-1;i++) {
//			System.out.println(i+"\t"+rs.getAtomContainer(i).getAtomCount()+"\t"+Check[i]);
//			}

            for (int i = 0; i <= ringSet.getAtomContainerCount() - 1; i++) {
                Ring ring = (Ring) ringSet.getAtomContainer(i);

                //System.out.println(k+"\t"+r.getAtomCount()+"\t"+r.getFlag(CDKConstants.ISAROMATIC));
                if (Check[i]) {
                    if (!ring.getFlag(CDKConstants.ISAROMATIC)) {
//						System.out.println(counter+"\t"+"ring not aromatic"+"\t"+r.getAtomCount());
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    /**
     * Remove rings.
     * <p/>
     * Removes rings which do not have all sp2 aromatic atoms and also gets rid of rings that have more than
     * 7 atoms in them.
     *
     * @param m The molecule from which we want to remove rings
     * @return The set of reduced rings
     */
    IRingSet removeExtraRings(IMolecule m) {

        try {
            AllRingsFinder arf = new AllRingsFinder();
            IRingSet rs = arf.findAllRings(m);

            //remove rings which dont have all aromatic atoms (according to hybridization set by lower case symbols in smiles):

            //System.out.println("numrings="+rs.size());

            iloop:
            for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {

            	boolean AllAromatic = true;
            	
                IRing r = (Ring) rs.getAtomContainer(i);


                if (r.getAtomCount() > 7) {
                    rs.removeAtomContainer(i);
                    i--; // go back to first one
                    continue iloop;
                }

                int NonSP2Count = 0;

                for (int j = 0; j <= r.getAtomCount() - 1; j++) {

                    //System.out.println(j+"\t"+r.getAtomAt(j).getSymbol()+"\t"+r.getAtomAt(j).getHybridization());

                    if (r.getAtom(j).getHybridization() != 2) {
                        NonSP2Count++;
                        if (r.getAtom(j).getSymbol().equals("C")) {
                            rs.removeAtomContainer(i);
                            i--; // go back
                            continue iloop;
                        }
                    }
                }

                if (NonSP2Count > 1) {
                    rs.removeAtomContainer(i);
                    i--; // go back
                    continue iloop;
                }

            }
            return rs;

        } catch (Exception e) {
            return null;
        }
    }

    private boolean[] findRingsToCheck(IMolecule m, IRingSet rs) {

        boolean[] Check = new boolean[rs.getAtomContainerCount()];

        for (int i = 0; i <= Check.length - 1; i++) {
            Check[i] = true;
        }

        iloop:

        for (int i = 0; i <= rs.getAtomContainerCount() - 1; i++) {

        	boolean AllAromatic = true;
        	
            IRing r = (Ring) rs.getAtomContainer(i);

            if (r.getAtomCount() > 7) {
                Check[i] = false;
                continue iloop;
            }

            int NonSP2Count = 0;

            for (int j = 0; j <= r.getAtomCount() - 1; j++) {

                // System.out.println(j+"\t"+r.getAtomAt(j).getSymbol()+"\t"+r.getAtomAt(j).getHybridization());

                if (r.getAtom(j).getHybridization() != 2) {
                    NonSP2Count++;
                    if (r.getAtom(j).getSymbol().equals("C")) {
                        Check[i] = false;
                        continue iloop;
                    }
                }
            }

            if (NonSP2Count > 1) {
            	Check[i] = false;
            	continue iloop;
            }

        }

        return Check;
    }

}





