package com.cineworm.util;

public class Events {
    public static class FullScreen {
        private boolean isFullScreen = false;

        public boolean isFullScreen() {
            return isFullScreen;
        }

        public void setFullScreen(boolean fullScreen) {
            isFullScreen = fullScreen;
        }
    }

    public static class RemoteLogout {
        private boolean isLogoutRemote = false;

        public boolean isLogoutRemote() {
            return isLogoutRemote;
        }

        public void setLogoutRemote(boolean isLogout) {
            isLogoutRemote = isLogout;
        }
    }

    public static class WatchList {
        private String watchListId;

        private boolean isAddToWatchList = false;

        public String getWatchListId() {
            return watchListId;
        }

        public void setWatchListId(String watchListId) {
            this.watchListId = watchListId;
        }

        public boolean isAddToWatchList() {
            return isAddToWatchList;
        }

        public void setAddToWatchList(boolean addToWatchList) {
            isAddToWatchList = addToWatchList;
        }
    }

    public static class CoinGateSuccess {

    }

    public static class CoinGateFailed {

    }
}
