package org.ifml.eclipse.emf.ui.properties;

import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * An abstract base class for property configurations based on EMF models.
 * 
 * @param <T>
 *            the model type.
 */
public abstract class EmfPropertyConfiguration<T extends EObject> {

    private final EClass eClass;

    /**
     * Constructs a new property configuration.
     * 
     * @param instanceClass
     *            the instance class.
     * @param eClass
     *            the EMF class.
     */
    public EmfPropertyConfiguration(Class<T> instanceClass, EClass eClass) {
        Preconditions.checkArgument(instanceClass == eClass.getInstanceClass(),
                String.format("Instance classes don't match: %s vs %s", instanceClass, eClass.getInstanceClass()));
        this.eClass = eClass;
    }

    EClass getEClass() {
        return eClass;
    }

    /**
     * Returns the set of features to be ignored by this property section.
     * <p>
     * The default implementation returns an empty set; sub-classes can override.
     * 
     * @return the set of ignored features.
     */
    public Set<EStructuralFeature> getFeaturesToIgnore() {
        return ImmutableSet.of();
    }

    /**
     * Returns the combo handler for a specific feature.
     * <p>
     * The default implementation returns an absent {@link Optional}; sub-classes can override.
     * 
     * @param feature
     *            the feature.
     * @return the optional combo handler for a specific feature.
     */
    public Optional<IEmfPropertyComboHandler<T>> getComboHandler(EStructuralFeature feature) {
        return Optional.absent();
    }

}