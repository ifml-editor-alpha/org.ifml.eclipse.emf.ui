package org.ifml.eclipse.emf.ui.properties;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * A chached collection of configuration elements.
 */
public final class EmfPropertyConfigurationSet {

    private final ILabelProvider featureLabelProvider;

    private final Map<EClass, EmfPropertyConfiguration<? extends EObject>> configs;

    private EmfPropertyConfigurationSet(Builder builder) {
        this.featureLabelProvider = builder.featureLabelProvider;
        this.configs = ImmutableMap.copyOf(builder.configs);
    }

    @SuppressWarnings("unchecked")
    private <T extends EObject> EmfPropertyConfiguration<T> getConfiguration(EClass eClass) {
        return (EmfPropertyConfiguration<T>) configs.get(eClass);
    }

    /**
     * Returns the label provider for structural features.
     * 
     * @return the label provider for displaying the name of a structural feature.
     */
    public ILabelProvider getFeatureLabelProvider() {
        return featureLabelProvider;
    }

    /**
     * Checks whether a feature must be ignored as a property.
     * 
     * @param feature
     *            the feature to test.
     * @param eClass
     *            the model class.
     * @return {@code true} if the feature must be ignored thus not displaying in the property UI.
     */
    public boolean isFeatureToIgnore(EStructuralFeature feature, EClass eClass) {
        for (EClass eClass2 : Iterables.concat(ImmutableList.of(eClass), eClass.getEAllSuperTypes())) {
            EmfPropertyConfiguration<? extends EObject> config = getConfiguration(eClass2);
            if ((config != null) && config.getFeaturesToIgnore().contains(feature)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Configures a combo viewer displaying a feature of a class.
     * 
     * @param comboViewer
     *            the combo viewer.
     * @param feature
     *            the feature.
     * @param eClass
     *            the EMF class.
     */
    public void configureComboViewer(ComboViewer comboViewer, EStructuralFeature feature, EClass eClass) {
        for (EClass eClass2 : Iterables.concat(ImmutableList.of(eClass), eClass.getEAllSuperTypes())) {
            if (configureComboViewer(comboViewer, feature, getConfiguration(eClass2))) {
                return;
            }
        }
    }

    private static <T extends EObject> boolean configureComboViewer(ComboViewer comboViewer, EStructuralFeature feature,
            EmfPropertyConfiguration<T> config) {
        if (config != null) {
            Optional<IEmfPropertyComboHandler<T>> comboHandler = config.getComboHandler(feature);
            if (comboHandler.isPresent()) {
                comboHandler.get().configureComboViewer(comboViewer, feature);
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the combo viewer input.
     * 
     * @param elem
     *            the current element.
     * @param feature
     *            the feature.
     * @param eClass
     *            the EMF class.
     * @return the combo viewer input.
     */
    public <T extends EObject> Object getComboViewerInput(T elem, EStructuralFeature feature, EClass eClass) {
        for (EClass eClass2 : Iterables.concat(ImmutableList.of(eClass), eClass.getEAllSuperTypes())) {
            Optional<Object> comboViewerInput = getComboViewerInput(elem, feature, getConfiguration(eClass2));
            if (comboViewerInput.isPresent()) {
                return comboViewerInput.get();
            }
        }
        return null;
    }

    private static <T extends EObject> Optional<Object> getComboViewerInput(T elem, EStructuralFeature feature,
            EmfPropertyConfiguration<T> config) {
        if (config != null) {
            Optional<IEmfPropertyComboHandler<T>> comboHandler = config.getComboHandler(feature);
            if (comboHandler.isPresent()) {
                return Optional.fromNullable(comboHandler.get().getComboViewerInput(elem, feature));
            }
        }
        return Optional.absent();
    }

    /**
     * A builder for {@link EmfPropertyConfigurationSet}.
     */
    public static final class Builder {

        private ILabelProvider featureLabelProvider;

        private final Map<EClass, EmfPropertyConfiguration<? extends EObject>> configs = Maps.newHashMap();

        /**
         * Adds a property configuration for a specific model class.
         * 
         * @param config
         *            the configuration.
         * @return this builder.
         */
        public Builder configuration(EmfPropertyConfiguration<? extends EObject> config) {
            if (configs.put(config.getEClass(), config) != null) {
                throw new IllegalArgumentException(String.format("Attempting to register twice a property configuration for %s",
                        config.getEClass().getName()));
            }
            return this;
        }

        /**
         * Sets the feature label provider.
         * 
         * @param featureLabelProvider
         *            the feature label provider.
         * @return this builder.
         */
        public Builder featureLabelProvider(ILabelProvider featureLabelProvider) {
            this.featureLabelProvider = featureLabelProvider;
            return this;
        }

        /**
         * Builds the configuration set.
         * 
         * @return the configuration set.
         */
        public EmfPropertyConfigurationSet build() {
            Preconditions.checkNotNull(featureLabelProvider);
            return new EmfPropertyConfigurationSet(this);
        }

    }

}
