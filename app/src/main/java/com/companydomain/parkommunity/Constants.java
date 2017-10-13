package com.companydomain.parkommunity;

/**
 * Created by ori on 26/08/16.
 */

public class Constants {



    public static final String ANONYMOUS = "anonymous";
    public static final int REQUEST_INVITE = 1;

    //shared preffrences:
    public static final String SP_MY_REFS_TITLES = "SPMYREFSTITLES";


    public static final String SP_IS_USER_INIT_IN_DB = "SPISUSERINITINDB";
    public static final String DB_USERS = "USERS";
    public static final String DB_GROUPS = "GROUPS";
    public static final String DB_INFO = "INFO";
    public static final String DB_PS = "PS";
    public static final String DB_ASK = "ASK";


    public static int STATE_NO_STATE=0;
    public static int STATE_UPDATING=1;
    public static int STATE_FINISHED_UPDATE=2;


    // Location
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = String.valueOf(R.string.app_package);
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";


    public static final int RADIOS_PARAMETER = 1;

}
