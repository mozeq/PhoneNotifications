package cz.moskovcak.pcsms;

public interface NotificationServer {
    String notifySMS(String phoneNumber, String contactName, String message);
    String notifyPhoneCall(String phoneNumber, String contactName, String state);
    void save();
    void load();
    void connect();
}
