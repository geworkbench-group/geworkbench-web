package org.geworkbenchweb.dataset;

import java.io.File;

import org.geworkbench.util.AnnotationInformationManager.AnnotationType;
import org.vaadin.appfoundation.authentication.data.User;

public interface LoaderUsingAnnotation extends Loader {

	public abstract void parseAnnotation(File annotFile,
			AnnotationType annotType, User annotOwner, Long datasetId)
			throws GeWorkbenchLoaderException;

}
