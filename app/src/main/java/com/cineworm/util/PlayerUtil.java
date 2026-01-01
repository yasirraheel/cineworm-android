package com.cineworm.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerUtil {
    public static boolean isYoutubeUrl(String youTubeURl) {
        boolean success;
        String pattern = "^(http(s)?://)?((w){3}.)?youtu(be|.be)?(\\.com)?/.+";
        success = !youTubeURl.isEmpty() && youTubeURl.matches(pattern);
        return success;
    }

    public static boolean isVimeoUrl(String vimeoUrl) {
        boolean success;
        String pattern = "^(http(s)?://)?((w){3}.)?vimeo?(\\.com)?/.+";
        success = !vimeoUrl.isEmpty() && vimeoUrl.matches(pattern);
        return success;
    }

    public static String getVideoIdFromVimeoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().length() <= 0)
            return "";
        return videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
    }

    public static String getVideoIdFromYoutubeUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().length() <= 0)
            return "";
        String reg = "(?:youtube(?:-nocookie)?\\.com/(?:[^/\\n\\s]+/\\S+/|(?:v|e(?:mbed)?)/|\\S*?[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})";

        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);

        if (matcher.find())
            return matcher.group(1);
        return "";
    }
}
