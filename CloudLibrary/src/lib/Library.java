package lib;

public class Library {

    public static final String DELIMITER = "±";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";

    public static final String COPY_FILE_TO_SERVER = "/copy_file_to_server";
    public static final String FILE_READY = "/file_ready";

    public static String getCopyFileToServer(String fileName){
        return COPY_FILE_TO_SERVER + DELIMITER + fileName;
    }


    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }



    public static String getAuthDenied() {
        return AUTH_DENIED;
    }



}
