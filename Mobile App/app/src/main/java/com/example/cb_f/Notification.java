package com.example.cb_f;

public class Notification {
    private String icon_uri, notification_title, notification_body;

    public Notification() {
    }

    public Notification(String icon_uri, String notification_title, String notification_body) {
        this.icon_uri = icon_uri;
        this.notification_title = notification_title;
        this.notification_body = notification_body;
    }

    public Notification(String notification_title, String notification_body) {
        this.notification_title = notification_title;
        this.notification_body = notification_body;
    }

    public String getIcon_uri() {
        return icon_uri;
    }

    public void setIcon_uri(String icon_uri) {
        this.icon_uri = icon_uri;
    }

    public String getNotification_title() {
        return notification_title;
    }

    public void setNotification_title(String notification_title) {
        this.notification_title = notification_title;
    }

    public String getNotification_body() {
        return notification_body;
    }

    public void setNotification_body(String notification_body) {
        this.notification_body = notification_body;
    }
}
