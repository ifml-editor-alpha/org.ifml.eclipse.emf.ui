package org.ifml.eclipse.emf.ui.properties;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.ComboViewer;

/**
 * The interface of objects able to handle combo viewers associated with a structural feature.
 * 
 * @param <T>
 *            the model type.
 */
public interface IEmfPropertyComboHandler<T> {

    /**
     * Configures a combo viewer associated with a structural feature.
     * 
     * @param comboViewer
     *            the combo viewer.
     * @param feature
     *            the structural feature.
     */
    void configureComboViewer(ComboViewer comboViewer, EStructuralFeature feature);

    /**
     * Returns the input for a combo viewer associated with a structural feature.
     * 
     * @param elem
     *            the selected model element.
     * @param feature
     *            the structural feature.
     * @return the input for the combo viewer.
     */
    Object getComboViewerInput(T elem, EStructuralFeature feature);

}