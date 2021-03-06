package com.example.nan.ssprocess.app;

/**
 * Created by nan on 12/15/2016.
 */
public class URL {

    public static final String HTTP_HEAD = "http://";
    public static final String USER_LOGIN = "/user/requestLogin";
    public static final String FETCH_TASK_RECORD_DETAIL = "/task/record/selectTaskRecordDetail";
    public static final String FETCH_TASK_RECORD_TO_ADMIN = "/task/record/selectAllTaskRecordDetail";
    public static final String FETCH_TASK_RECORD_TO_QA = "/task/record/selectAllQaTaskRecordDetailByUserAccount";
    public static final String FETCH_TASK_RECORD_TO_INSTALL = "/task/record/selectAllInstallTaskRecordDetailByUserAccount";
    public static final String UPDATE_MACHINE_LOCATION = "/machine/update";
    public static final String FATCH_INSTALL_ABNORMAL_RECORD_DETAIL = "/abnormal/record/selectAbnormalRecordDetails";
    public static final String UPDATE_INSTALL_ABNORMAL_RECORD_DETAIL = "/abnormal/record/updateAbnormalRecordDetail";
    public static final String UPLOAD_INSTALL_ABNORMAL_IMAGE = "/abnormal/image/add";
    public static final String FATCH_TASK_QUALITY_RECORD_DETAIL = "/task/quality/record/selectTaskQualityRecordDetails";
    public static final String UPDATE_TASK_QUALITY_RECORD_DETAIL = "/task/quality/record/updateTaskQualityRecordDetail";
    public static final String UPLOAD_QUALITY_RECORD_IMAGE = "/quality/record/image/add";

}
