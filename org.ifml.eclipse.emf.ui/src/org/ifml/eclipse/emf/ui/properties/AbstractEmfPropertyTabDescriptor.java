package org.ifml.eclipse.emf.ui.properties;

import java.util.List;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;

import com.google.common.collect.Lists;

/**
 * An abstract base class for EMF-based property tab descriptors.
 */
public abstract class AbstractEmfPropertyTabDescriptor extends AbstractTabDescriptor {

    private final String category;

    private final String label;

    /**
     * Constructs a new descriptor.
     * 
     * @param category
     *            the category the tab belongs to.
     * @param label
     *            the text label for the tab.
     */
    public AbstractEmfPropertyTabDescriptor(String category, String label) {
        this.category = category;
        this.label = label;
        setSectionDescriptors(createSectionDescriptors());
    }

    @Override
    public final String getCategory() {
        return category;
    }

    @Override
    public final String getId() {
        return String.format("%s.%s.%s", getClass().getName(), getCategory(), getLabel());
    }

    @Override
    public final String getLabel() {
        return label;
    }

    private final List<SectionDescriptor> createSectionDescriptors() {
        List<SectionDescriptor> descriptors = Lists.newArrayList();
        for (EmfPropertySection<?> section : createPropertySections()) {
            descriptors.add(new SectionDescriptor(getId(), section));
        }
        return descriptors;
    }

    /**
     * Creates the list of property sections.
     * 
     * @return the list of property sections.
     */
    protected abstract List<EmfPropertySection<?>> createPropertySections();

    private static final class SectionDescriptor extends AbstractSectionDescriptor {

        private final String tabId;

        private final EmfPropertySection<?> propertySection;

        private SectionDescriptor(String tabId, EmfPropertySection<?> propertySection) {
            this.tabId = tabId;
            this.propertySection = propertySection;
        }

        @Override
        public String getId() {
            return String.format("%s.%s", tabId, propertySection.getInstanceClass().getName());
        }

        @Override
        public String getTargetTab() {
            return tabId;
        }

        @Override
        public ISection getSectionClass() {
            return propertySection;
        }

        @Override
        public IFilter getFilter() {
            return propertySection;
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof SectionDescriptor)) {
                return false;
            }
            SectionDescriptor other = (SectionDescriptor) obj;
            return getId().equals(other.getId());
        }

    }

}
