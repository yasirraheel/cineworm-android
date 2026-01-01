package com.cineworm.item;

public class Filter {
    private String filterId;
    private String filterName;
    private String filterType;
    private boolean isSelected = false;

    public Filter() {

    }

    public Filter(String filterId, String filterName, String filterType, boolean isSelected) {
        this.filterId = filterId;
        this.filterName = filterName;
        this.filterType = filterType;
        this.isSelected = isSelected;
    }

    public String getFilterId() {
        return filterId;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getFilterType() {
        return filterType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
