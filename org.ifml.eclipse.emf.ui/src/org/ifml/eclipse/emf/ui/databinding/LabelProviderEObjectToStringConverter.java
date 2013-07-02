package org.ifml.eclipse.emf.ui.databinding;

import java.util.List;

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.ILabelProvider;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

final class LabelProviderEObjectToStringConverter extends Converter {

    private final ILabelProvider labelProvider;

    public LabelProviderEObjectToStringConverter(ILabelProvider labelProvider) {
        super(EReference.class, String.class);
        this.labelProvider = labelProvider;
    }

    @Override
    public Object convert(Object fromObject) {
        if (fromObject instanceof List<?>) {
            List<?> fromList = (List<?>) fromObject;
            List<String> toList = Lists.newArrayList();
            for (Object fromObj : fromList) {
                toList.add(labelProvider.getText(fromObj));
            }
            return Joiner.on(' ').join(toList);
        } else {
            return labelProvider.getText(fromObject);
        }
    }

}
