package com.cineworm.util;

import androidx.annotation.NonNull;

import com.cineworm.item.FilterType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class FilterUtil {
    public static ArrayList<FilterType> jsonToList(String json) {
        return new Gson().fromJson(json, new TypeToken<ArrayList<FilterType>>() {
        }.getType());
    }

    public static String listToJson(ArrayList<FilterType> list) {
        return new Gson().toJson(list);
    }

    public static ArrayList<FilterType> jsonToSelectList(String json) {
        ArrayList<FilterType> list = jsonToList(json);
        ArrayList<FilterType> listSelect = new ArrayList<>();
        for (FilterType filterType : list) {
            if (filterType.isSelected()) {
                listSelect.add(filterType);
            }
        }
        return listSelect;
    }

    public static void jsonForParameter(ArrayList<FilterType> filterTypeSelectedList, JsonObject jsObj, int secId) {
        ArrayList<FilterType> listType = FilterType.getFilterTypeListBySec(secId);
        for (FilterType filterType : listType) {
            jsObj.addProperty(filterType.getFilterTypeParameterName(), getCommaSepIds(filterTypeSelectedList, filterType.getFilterType()));
        }
    }

    @NonNull
    private static String getCommaSepIds(ArrayList<FilterType> filterTypeSelectedList, String type) {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (FilterType filterType : filterTypeSelectedList) {
            if (filterType.getFilterType().equals(type)) {
                stringBuilder.append(prefix);
                prefix = ",";
                stringBuilder.append(filterType.getFilterId());
            }
        }
        return stringBuilder.toString();
    }

}
