package jobs;

/**
 * Created by IntelliJ IDEA.
 * User: nicolasleroux
 * Date: 1/12/11
 * Time: 9:15 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Notification {

    public void says(String nick, String text);

    public void connect() throws Exception;


}
