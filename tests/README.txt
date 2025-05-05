The files in this directory are based on 

https://github.com/WahlOya/Tautobase/blob/master/Tautobase_SMIRKS.txt

This file has 1680 tab-delimited entries. The first column is a SMIRKS 
expression of the form SMILES1>>SMILES2. 

In each case, I have used InchiAPI to get the InChI for each of the 
two tautomers. The processs was to create a CDK molecule 
(IAtomContainer) from a SMILES, and to generate the InChI from 
that molecule. 

If the two InChIs are the same, it means that inchi-C considers 
them to tautomers. There were 711 such cases.

A problem arose that inchi-C fails to generate an InChI when the 
CDK molecule was created from a SMILES that involves unresolved 
aromatic bonding (c,n,o,s,p). There is probably a simpler way 
around this, but I just used the CDK to generate a canonical 
(Kekule) SMILES from the molecule in such cases and then 
generated another molecule from that canonical SMILES.

iminol vs. amide

For each of these cases, I used CDK to generate a CDK molecule and its 
PNG image. I also generated the InChIKey from the InChI using 
InchiAPI.getInChIKeyFromInChI(inchi).

In addition, I checked to see which of these cases differed when 
I added the InchiAPI output flag FIXAMIDE. This flagged 284 structures, 
which I have given a grey background to. They are all amides that 
CDK (using JNA-InChI or using InchiAPI without the FIXAMIDE flag) 
would leave as inchi-C's default output structure, which is in 
the form of an iminol (N=C-OH) rather than an amide (HN-C=O).

My conclusion is that changing iminols to amides is pretty much all 
we need to do. There might still be an odd example or two that might 
cause a chemist to wince. You tell me.

All I need for our IUPAC FAIRSpec project is to get a "chemically 
reasonable" tautomer out of an InChI. We will use InchiAPI with the
FIXAMIDE flag.

Thanking John Mayfield for pointing me to the Tautobase database. 

Bob Hanson
2025.05.04 
