package org.openscience.cdk.io.cdx;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.io.formats.IResourceFormat;

public abstract class CDXReader extends DefaultChemObjectReader {

	@Override
	public <T extends IChemObject> T read(T object) throws CDKException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReader(Reader reader) throws CDKException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setReader(InputStream reader) throws CDKException {
		// TODO Auto-generated method stub

	}

	@Override
	public IResourceFormat getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean accepts(Class<? extends IChemObject> classObject) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
