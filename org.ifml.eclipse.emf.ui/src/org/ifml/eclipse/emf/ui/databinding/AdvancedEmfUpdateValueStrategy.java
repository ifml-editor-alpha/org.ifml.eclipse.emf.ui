package org.ifml.eclipse.emf.ui.databinding;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.emf.databinding.EMFUpdateValueStrategy;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * A sub-class of {@link EMFUpdateValueStrategy} which is able to apply custom EMF converters.
 * 
 */
public final class AdvancedEmfUpdateValueStrategy extends EMFUpdateValueStrategy {

    private final ILabelProvider labelProvider;

    /**
     * Constructs a new strategy.
     * 
     * @param labelProvider
     *            the label provider.
     */
    public AdvancedEmfUpdateValueStrategy(ILabelProvider labelProvider) {
        this.labelProvider = labelProvider;
    }

    @Override
    protected IConverter createConverter(Object fromType, Object toType) {
        if ((fromType instanceof EReference) && (String.class == toType)) {
            return new LabelProviderEObjectToStringConverter(labelProvider);
        }
        return super.createConverter(fromType, toType);
    }

}
