package com.cineworm.item;

public class FilterType {
    private String filterId;
    private String filterName;
    private String filterType;
    private boolean isSelected = false;

    public static final String TY_LANGUAGE = "language";
    public static final String TY_GENRE = "genre";
    public static final String TY_CATEGORY_SP = "category_sp";
    public static final String TY_CATEGORY_TV = "category_tv";
    public static final String TY_ORDER_TYPE = "order_type";

    public static final int MOVIE = 0;
    public static final int SHOW = 1;
    public static final int TV = 2;
    public static final int SPORT = 3;

    public static java.util.ArrayList<FilterType> getFilterTypeListBySec(int filterTypeSec) {
        java.util.ArrayList<FilterType> list = new java.util.ArrayList<>();
        if (filterTypeSec == MOVIE) {
            // Movies
            list.add(new FilterType("1", "Language", TY_LANGUAGE, false));
            list.add(new FilterType("2", "Genre", TY_GENRE, false));
            list.add(new FilterType("3", "Sort", TY_ORDER_TYPE, false));
        } else if (filterTypeSec == SHOW) {
            // TV Series
            list.add(new FilterType("1", "Language", TY_LANGUAGE, false));
            list.add(new FilterType("2", "Genre", TY_GENRE, false));
            list.add(new FilterType("3", "Sort", TY_ORDER_TYPE, false));
        } else if (filterTypeSec == TV) {
            // Live TV
            list.add(new FilterType("1", "Category", TY_CATEGORY_TV, false));
            list.add(new FilterType("2", "Sort", TY_ORDER_TYPE, false));
        } else if (filterTypeSec == SPORT) {
            // Sports
            list.add(new FilterType("1", "Category", TY_CATEGORY_SP, false));
            list.add(new FilterType("2", "Sort", TY_ORDER_TYPE, false));
        }
        return list;
    }

    public String getFilterTypeParameterName() {
        switch (filterType) {
            case TY_LANGUAGE:
                return "lang_id";
            case TY_GENRE:
                return "genre_id";
            case TY_CATEGORY_SP:
            case TY_CATEGORY_TV:
                return "cat_id";
            case TY_ORDER_TYPE:
                return "filter";
        }
        return "";
    }

    public FilterType() {

    }

    public FilterType(String filterId, String filterName, String filterType, boolean isSelected) {
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
