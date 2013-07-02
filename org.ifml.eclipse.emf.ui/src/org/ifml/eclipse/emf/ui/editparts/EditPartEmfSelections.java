package org.ifml.eclipse.emf.ui.editparts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.ifml.base.Objects2;
import org.ifml.eclipse.ui.viewers.Selections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Provides utility methods for dealing with UI selections composed either by {@link EObject}s or by {@link EditPart}s wrapping
 * {@link EObject}s.
 */
public final class EditPartEmfSelections {

    private EditPartEmfSelections() {
    }

    /**
     * Transforms a single UI selection into a model element.
     * 
     * @param selection
     *            the selection.
     * @param modelType
     *            the model instance class.
     * @return the single selected model element or {@code null} if the selection is not a singleton or if the single selected element
     *         cannot be adapted to the specified {@code modelType}.
     */
    public static @Nullable
    <T extends EObject> T getSingleModelObject(ISelection selection, Class<T> modelType) {
        IStructuredSelection structuredSelection = Selections.toStructuredSelection(selection);
        if (structuredSelection.size() == 1) {
            return getModelObject(structuredSelection.getFirstElement(), modelType);
        } else {
            return null;
        }
    }

    /**
     * Transforms a UI selection into a list of model elements.
     * 
     * @param selection
     *            the selection.
     * @param modelType
     *            the model instance class.
     * @param forceCheck
     *            if {@code true} each selected object must be effectivily associated with the given {@code modelType}, thus returning
     *            an empty list in case one or more selected object cannot be converted; if {@code false} the returning list is
     *            narrowed in order to discard selected elements not effectively associated with the given {@code modelType}.
     * @return the list of {@link EObject} elements.
     */
    public static <T extends EObject> List<T> getModelObjects(ISelection selection, Class<T> modelType, boolean forceCheck) {
        List<T> result = Lists.newArrayList();
        for (Iterator<?> i = Selections.toStructuredSelection(selection).iterator(); i.hasNext();) {
            Object obj = i.next();
            T modelObj = getModelObject(obj, modelType);
            if (modelObj != null) {
                result.add(modelObj);
            } else {
                if (forceCheck) {
                    return ImmutableList.of();
                }
            }
        }
        return result;
    }

    private static @Nullable
    <T extends EObject> T getModelObject(Object toTest, Class<T> modelType) {
        if (toTest == null) {
            return null;
        }
        if (toTest instanceof EditPart) {
            EditPart editPart = (EditPart) toTest;
            Object model = editPart.getModel();
            return getModelObject(model, modelType);
        } else if (modelType.isInstance(toTest)) {
            return modelType.cast(toTest);
        } else if (toTest instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) toTest;
            Object editPartAdapter = adaptable.getAdapter(EditPart.class);
            if (editPartAdapter instanceof EditPart) {
                return getModelObject(editPartAdapter, modelType);
            }
            Object eObjectAdapter = adaptable.getAdapter(EObject.class);
            if (eObjectAdapter instanceof EObject) {
                return getModelObject(eObjectAdapter, modelType);
            }
        }
        return null;
    }

    /**
     * Transforms a selection of model objects into a selection of edit parts.
     * 
     * @param modelObjects
     *            the model objects.
     * @param viewer
     *            the edit part viewer.
     * @return the selection of edit parts.
     */
    public static ISelection toEditParts(List<?> modelObjects, EditPartViewer viewer) {
        List<EditPart> editParts = Lists.newArrayList();
        Map<?, ?> editPartRegistry = viewer.getEditPartRegistry();
        for (Object modelObj : modelObjects) {
            EditPart editPart = Objects2.as(editPartRegistry.get(modelObj), EditPart.class);
            if (editPart != null) {
                editParts.add(editPart);
            }
        }
        return new StructuredSelection(editParts);
    }

}
