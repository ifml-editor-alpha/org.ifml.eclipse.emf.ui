package org.ifml.eclipse.emf.ui.properties;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.edit.EMFEditProperties;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.ifml.eclipse.emf.ui.editparts.EditPartEmfSelections;
import org.ifml.eclipse.ui.properties.FormPropertyPart;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * An abstract base class for EMF-based property sections.
 * <p>
 * Note that this property section embeds also the {@link IFilter} logic.
 * 
 * @param <T>
 *            the EMF model type.
 */
public abstract class EmfPropertySection<T extends EObject> extends AbstractPropertySection implements IFilter {

    private final EClass eClass;

    private final Class<T> instanceClass;

    private final List<Binding> bindings = Lists.newArrayList();

    private final List<IObservableValue> modelObservables = Lists.newArrayList();

    private final List<PropertyItem> items = Lists.newArrayList();

    private final LoadingCache<Widget, ISWTObservableValue> widgetTextObservables = CacheBuilder.newBuilder().concurrencyLevel(1)
            .build(new CacheLoader<Widget, ISWTObservableValue>() {
                @Override
                public ISWTObservableValue load(Widget widget) {
                    return WidgetProperties.text(SWT.Modify).observe(widget);
                }
            });

    private final LoadingCache<Widget, ISWTObservableValue> widgetSelectionObservables = CacheBuilder.newBuilder().concurrencyLevel(1)
            .build(new CacheLoader<Widget, ISWTObservableValue>() {
                @Override
                public ISWTObservableValue load(Widget widget) {
                    return WidgetProperties.selection().observe(widget);
                }
            });

    private final LoadingCache<Viewer, IViewerObservableValue> viewerSelectionObservables = CacheBuilder.newBuilder()
            .concurrencyLevel(1).build(new CacheLoader<Viewer, IViewerObservableValue>() {
                @Override
                public IViewerObservableValue load(Viewer viewer) {
                    return ViewerProperties.singleSelection().observe(viewer);
                }
            });

    private DataBindingContext dbc;

    private final EmfPropertyConfigurationSet configSet;

    /**
     * Constructs a new property section.
     * 
     * @param instanceClass
     *            the instance class.
     * @param eClass
     *            the EMF class.
     * @param configSet
     *            the property configuration set.
     */
    protected EmfPropertySection(Class<T> instanceClass, EClass eClass, EmfPropertyConfigurationSet configSet) {
        super();
        Preconditions.checkArgument(instanceClass == eClass.getInstanceClass(),
                String.format("Instance classes don't match: %s vs %s", instanceClass, eClass.getInstanceClass()));
        this.eClass = eClass;
        this.instanceClass = instanceClass;
        this.configSet = configSet;
    }

    /**
     * Returns the instance class.
     * 
     * @return the instance class.
     */
    protected final Class<T> getInstanceClass() {
        return instanceClass;
    }

    /**
     * Returns the EMF class.
     * 
     * @return the EMF class.
     */
    protected final EClass getEClass() {
        return eClass;
    }

    @Override
    public boolean select(Object toTest) {
        if (toTest instanceof ISelection) {
            EObject eObj = EditPartEmfSelections.getSingleModelObject((ISelection) toTest, EObject.class);
            return (eObj != null) && (eObj.eClass() == eClass);
        } else {
            EObject eObj = EditPartEmfSelections.getSingleModelObject(new StructuredSelection(toTest), EObject.class);
            return (eObj != null) && (eObj.eClass() == eClass);
        }
    }

    @Override
    public final void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        this.dbc = new DataBindingContext();
        FormPropertyPart formPropertyPanel = new FormPropertyPart(parent, aTabbedPropertySheetPage);
        for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
            if (configSet.isFeatureToIgnore(feature, eClass)) {
                continue;
            }
            if (feature instanceof EAttribute) {
                if (feature.getEType() == EcorePackage.Literals.EBOOLEAN) {
                    Button button = formPropertyPanel.addCheckBox(configSet.getFeatureLabelProvider().getText(feature));
                    items.add(new SelectionPropertyItem(feature, button));
                } else if (feature.getEType() == EcorePackage.Literals.ESTRING) {
                    Text text = formPropertyPanel.addText(configSet.getFeatureLabelProvider().getText(feature));
                    items.add(new TextPropertyItem(feature, text, null));
                } else {
                    throw new UnsupportedOperationException("Feature not handled: " + feature);
                }
            } else if (feature instanceof EReference) {
                EReference ref = (EReference) feature;
                if (!ref.isContainment()) {
                    ComboViewer comboViewer = formPropertyPanel.addCCombo(configSet.getFeatureLabelProvider().getText(feature));
                    configSet.configureComboViewer(comboViewer, feature, eClass);
                    items.add(new ComboPropertyItem(feature, comboViewer));
                }
            } else {
                throw new UnsupportedOperationException("Feature not handled: " + feature);
            }
        }
    }

    /**
     * Returns the set of features to be ignored by this property section.
     * <p>
     * The default implementation returns an empty set; sub-classes can override.
     * 
     * @return the set of ignored features.
     */
    protected Set<EStructuralFeature> getFeaturesToIgnore() {
        return ImmutableSet.of();
    }

    @Override
    public final void refresh() {
        disposeBindings();
        disposeModelObservables();
        Optional<T> elem = getSingleSelection();
        if (elem.isPresent()) {
            for (PropertyItem item : items) {
                item.refresh(elem.get());
            }
        }
    }

    /**
     * Returns the single selected model element.
     * 
     * @return the single selected model element.
     */
    protected abstract Optional<T> getSingleSelection();

    /**
     * Creates a binding between the structural feature of an element and the text property of a widget.
     * 
     * @param elem
     *            the model element to observe.
     * @param feature
     *            the structural feature to bind.
     * @param widget
     *            the widget whose text property requires binding.
     * @param modelToTextStrategy
     *            the strategy to emply when the model is the source of the change and the widget is the target of the change.
     * @return the binding.
     */
    protected final Binding bindText(T elem, EStructuralFeature feature, Widget widget, UpdateValueStrategy modelToTextStrategy) {
        IObservableValue modelObservable = createModelObservable(elem, feature);
        ISWTObservableValue widgetObservable = widgetTextObservables.getUnchecked(widget);
        Binding binding = dbc.bindValue(widgetObservable, modelObservable, null, modelToTextStrategy);
        bindings.add(binding);
        return binding;
    }

    /**
     * Creates a binding between the structural feature of an element and the selection status of a widget.
     * 
     * @param elem
     *            the model element to observe.
     * @param feature
     *            the structural feature to bind.
     * @param widget
     *            the widget whose selection status requires binding.
     */
    protected final void bindSelection(T elem, EStructuralFeature feature, Widget widget) {
        IObservableValue modelObservable = createModelObservable(elem, feature);
        ISWTObservableValue widgetObservable = widgetSelectionObservables.getUnchecked(widget);
        Binding binding = dbc.bindValue(widgetObservable, modelObservable);
        bindings.add(binding);
    }

    /**
     * Creates a binding between the structural feature of an element and the selection property of a combo.
     * 
     * @param elem
     *            the model element to observe.
     * @param feature
     *            the structural feature to bind.
     * @param comboViewer
     *            the combo viewer.
     */
    protected final void bindCombo(T elem, EStructuralFeature feature, ComboViewer comboViewer) {
        IObservableValue modelObservable = createModelObservable(elem, feature);
        IViewerObservableValue viewerObservable = viewerSelectionObservables.getUnchecked(comboViewer);
        Binding binding = dbc.bindValue(viewerObservable, modelObservable);
        bindings.add(binding);
    }

    private IObservableValue createModelObservable(T elem, EStructuralFeature feature) {
        IObservableValue modelObservable = EMFEditProperties.value(getEditingDomain(), feature).observe(elem);
        modelObservables.add(modelObservable);
        handleModelObservable(modelObservable);
        return modelObservable;
    }

    /**
     * Returns the editing domain.
     * 
     * @return the EMF editing domain.
     */
    protected abstract TransactionalEditingDomain getEditingDomain();

    /**
     * Invoked when a new model observable has been created.
     * 
     * @param modelObservable
     *            the newly created model observable.
     */
    protected abstract void handleModelObservable(IObservableValue modelObservable);

    @Override
    public void dispose() {
        disposeBindings();
        disposeModelObservables();
        disposeWidgetObservables();
        items.clear();
        super.dispose();
    }

    private void disposeBindings() {
        for (Binding binding : bindings) {
            binding.dispose();
        }
        bindings.clear();
    }

    private void disposeModelObservables() {
        for (IObservableValue observable : modelObservables) {
            observable.dispose();
        }
        modelObservables.clear();
    }

    private void disposeWidgetObservables() {
        disposeWidgetObservables(widgetTextObservables);
        disposeWidgetObservables(widgetSelectionObservables);
        disposeViewerObservables(viewerSelectionObservables);
    }

    private void disposeWidgetObservables(LoadingCache<Widget, ISWTObservableValue> observables) {
        for (Map.Entry<Widget, ISWTObservableValue> entry : observables.asMap().entrySet()) {
            ISWTObservableValue observable = entry.getValue();
            if (observable != null) {
                observable.dispose();
            }
        }
        observables.invalidateAll();
    }

    private void disposeViewerObservables(LoadingCache<Viewer, IViewerObservableValue> observables) {
        for (Map.Entry<Viewer, IViewerObservableValue> entry : observables.asMap().entrySet()) {
            IViewerObservableValue observable = entry.getValue();
            if (observable != null) {
                observable.dispose();
            }
        }
        observables.invalidateAll();
    }

    private abstract class PropertyItem {

        private final EStructuralFeature feature;

        private final Widget widget;

        PropertyItem(EStructuralFeature feature, Widget widget) {
            this.feature = feature;
            this.widget = widget;
        }

        protected final EStructuralFeature getFeature() {
            return feature;
        }

        protected final Widget getWidget() {
            return widget;
        }

        protected abstract void refresh(T elem);

        @Override
        public String toString() {
            return String.format("%s %s", getClass().getSimpleName(), getFeature().getName());
        }

    }

    private final class SelectionPropertyItem extends PropertyItem {

        SelectionPropertyItem(EStructuralFeature feature, Widget widget) {
            super(feature, widget);
        }

        @Override
        protected void refresh(T elem) {
            bindSelection(elem, getFeature(), getWidget());
        }

    }

    private final class TextPropertyItem extends PropertyItem {

        private final UpdateValueStrategy modelToTextStrategy;

        TextPropertyItem(EStructuralFeature feature, Widget widget, UpdateValueStrategy modelToTextStrategy) {
            super(feature, widget);
            this.modelToTextStrategy = modelToTextStrategy;
        }

        @Override
        protected void refresh(T elem) {
            bindText(elem, getFeature(), getWidget(), modelToTextStrategy);
        }

    }

    private final class ComboPropertyItem extends PropertyItem {

        private final ComboViewer viewer;

        public ComboPropertyItem(EStructuralFeature feature, ComboViewer viewer) {
            super(feature, viewer.getControl());
            this.viewer = viewer;
        }

        @Override
        protected void refresh(T elem) {
            if (viewer.getContentProvider() != null) {
                viewer.setInput(configSet.getComboViewerInput(elem, getFeature(), eClass));
            }
            bindCombo(elem, getFeature(), viewer);
        }

    }

}
