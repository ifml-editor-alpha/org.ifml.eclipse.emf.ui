package org.ifml.eclipse.emf.ui.viewers;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ifml.base.Objects2;

/**
 * Provides utility methods for EMF-based content providers.
 * 
 */
public final class EmfContentProviders {

    private EmfContentProviders() {
    }

    /**
     * Creates a structured content provider able to return the child elements of an element based on a specific containment feature.
     * 
     * @param containmentFeature
     *            the containment feature.
     * @return the structured content provider.
     */
    public static final IStructuredContentProvider newChildrenContentProvider(EStructuralFeature containmentFeature) {
        return new ChildrenContentProvider(containmentFeature);
    }

    private static class ChildrenContentProvider implements IStructuredContentProvider {

        private EObject parent;

        private final EStructuralFeature containmentFeature;

        public ChildrenContentProvider(EStructuralFeature containmentFeature) {
            this.containmentFeature = containmentFeature;
        }

        @Override
        public void dispose() {
            this.parent = null;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.parent = Objects2.as(newInput, EObject.class);
        }

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement == parent) {
                Object result = parent.eGet(containmentFeature, true);
                if (result instanceof EList<?>) {
                    return ((EList<?>) result).toArray();
                } else if (result != null) {
                    return new Object[] { result };
                }
            }
            return new Object[0];
        }
    }

}
