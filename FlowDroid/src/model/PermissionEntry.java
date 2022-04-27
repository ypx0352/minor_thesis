package model;

import java.util.Objects;

public class PermissionEntry {
    String appName;
    String permissionName;
    String parentEdgeName;
    String checkedCaller;
    String requestCaller;
    String shouldShowRationalCaller;
    boolean isChecked;
    boolean isRequested;
    boolean isHasShouldShowRational;
    boolean isCheckBeforeRequest;

    public PermissionEntry(String appName, String parentEdgeName, String permissionName) {
        if(parentEdgeName.lastIndexOf('$')>0){
            parentEdgeName = parentEdgeName.substring(0,parentEdgeName.lastIndexOf("$"));
        }
        this.appName = appName;
        this.parentEdgeName = parentEdgeName;
        this.permissionName = permissionName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isRequested() {
        return isRequested;
    }

    public void setRequested(boolean requested) {
        isRequested = requested;
    }

    public boolean isHasShouldShowRational() {
        return isHasShouldShowRational;
    }

    public void setHasShouldShowRational(boolean hasShouldShowRational) {
        isHasShouldShowRational = hasShouldShowRational;
    }

    public boolean isChceckBeforeRequest() {
        return isCheckBeforeRequest;
    }

    public String getParentEdgeName() {
        return parentEdgeName;
    }

    public void setParentEdgeName(String parentEdgeName) {
        this.parentEdgeName = parentEdgeName;
    }

    public String getCheckedCaller() {
        return checkedCaller;
    }

    public void setCheckedCaller(String checkedCaller) {
        this.checkedCaller = checkedCaller;
    }

    public String getRequestCaller() {
        return requestCaller;
    }

    public void setRequestCaller(String requestCaller) {
        this.requestCaller = requestCaller;
    }

    public String getShouldShowRationalCaller() {
        return shouldShowRationalCaller;
    }

    public void setShouldShowRationalCaller(String shouldShowRationalCaller) {
        this.shouldShowRationalCaller = shouldShowRationalCaller;
    }

    public boolean isCheckBeforeRequest() {
        return isCheckBeforeRequest;
    }

    public void setCheckBeforeRequest(boolean checkBeforeRequest) {
        isCheckBeforeRequest = checkBeforeRequest;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionEntry that = (PermissionEntry) o;
        return appName.equals(that.appName) && permissionName.equals(that.permissionName) && parentEdgeName.equals(that.parentEdgeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, permissionName, parentEdgeName);
    }

    public String toCSVString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", appName, permissionName,parentEdgeName, isChecked, checkedCaller, isRequested, requestCaller, isHasShouldShowRational, shouldShowRationalCaller, isCheckBeforeRequest);
    }
}