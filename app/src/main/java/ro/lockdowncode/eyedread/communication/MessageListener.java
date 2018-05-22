package ro.lockdowncode.eyedread.communication;

/**
 * Created by Adi Neag on 05.05.2018.
 */

public interface MessageListener {
    void messageReceived(String message, String ip);

    void hostUnavailable(String host);

}
