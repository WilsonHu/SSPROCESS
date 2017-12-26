package com.example.nan.ssprocess.activity;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.AbnormalRecordDetailsData;
import com.example.nan.ssprocess.bean.basic.TaskMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;

/**
 * @author nan  2017/12/18
 */
public class DetailToInstallActivity extends AppCompatActivity implements BGASortableNinePhotoLayout.Delegate {

    private static final String TAG="nlgDetailToInstall";
    private RadioButton installNormalRb;
    private RadioButton installAbnormalRb;
    private Spinner failReasonSpinner;
    private EditText installAbnormalDetailEt;

    private TaskMachineListData mTaskMachineListData=new TaskMachineListData();
    private ArrayList<AbnormalRecordDetailsData> mAbnormalRecordList = new ArrayList<>();
    private AbnormalRecordDetailsData mAbnormalRecordDetailsData=new AbnormalRecordDetailsData();
    private FetchInstallRecordDataHandler mFetchInstallRecordDataHandler = new FetchInstallRecordDataHandler();
    private UpdateProcessDetailDataHandler mUpdateProcessDetailDataHandler=new UpdateProcessDetailDataHandler();

    private static final int SCAN_QRCODE_END = 0;
    private static final int RC_INSTALL_CHOOSE_PHOTO = 3;
    private static final int RC_INSTALL_PHOTO_PREVIEW = 4;
	private static final int NORMAL = 3;
    private static final int ABNORMAL = 4;

    private BGASortableNinePhotoLayout mInstallAbnormalPhotosSnpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_to_install);

        EditText locationEt = findViewById(R.id.location_et);
        TextView orderNumberTv=findViewById(R.id.order_number_tv);
        TextView machineNumberTv=findViewById(R.id.machine_number_tv);
        TextView needleCountTv=findViewById(R.id.needle_count_tv);
        TextView typeTv=findViewById(R.id.type_tv);
        TextView intallListTv=findViewById(R.id.intall_list_tv);

        installNormalRb=findViewById(R.id.normal_rb);
        installAbnormalRb=findViewById(R.id.abnormal_rb);
        failReasonSpinner=findViewById(R.id.fail_reason_spinner);
        installAbnormalDetailEt=findViewById(R.id.abnormal_detail_et);

        //获取传递过来的信息
        Intent intent = getIntent();
        mTaskMachineListData = (TaskMachineListData) intent.getSerializableExtra("mTaskMachineListData");
        Log.d(TAG, "onCreate: position :"+mTaskMachineListData.getMachineData().getLocation());

        //把数据填入相应位置
        orderNumberTv.setText(""+mTaskMachineListData.getMachineData().getOrderId());
        needleCountTv.setText(""+mTaskMachineListData.getMachineOrderData().getHeadNum());
        machineNumberTv.setText(mTaskMachineListData.getMachineData().getMachineId());
        typeTv.setText(""+mTaskMachineListData.getMachineOrderData().getMachineType());
        locationEt.setText(mTaskMachineListData.getMachineData().getLocation());

        fetchInstallRecordData();
        //点击返回
        ImageView previousIv = findViewById(R.id.close_machine_detail);
        previousIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //点击上传安装结果
        Button installInfoUpdateButton = findViewById(R.id.install_info_update_button);
        installInfoUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DetailToInstallActivity.this,ScanQrcodeActivity.class);
                startActivityForResult(intent,SCAN_QRCODE_END);
            }
        });

        //九宫格拍照
        mInstallAbnormalPhotosSnpl = findViewById(R.id.install_abnormal_add_photos);
        mInstallAbnormalPhotosSnpl.setMaxItemCount(9);
        mInstallAbnormalPhotosSnpl.setPlusEnable(true);
        mInstallAbnormalPhotosSnpl.setDelegate(this);
    }

    private void fetchInstallRecordData() {
        final String account = SinSimApp.getApp().getAccount();
        final String ip = SinSimApp.getApp().getServerIP();
        LinkedHashMap<String, Integer> mPostValue = new LinkedHashMap<>();
        mPostValue.put("taskRecordId", mTaskMachineListData.getId());
        String fetchProcessRecordUrl = URL.HTTP_HEAD + ip + URL.FATCH_INSTALL_ABNORMAL_RECORD_DETAIL;
        Network.Instance(SinSimApp.getApp()).fetchProcessInstallRecordData(fetchProcessRecordUrl, mPostValue, mFetchInstallRecordDataHandler);
    }

    private class FetchInstallRecordDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.what == Network.OK) {
                //获取质检结果
                mAbnormalRecordList=(ArrayList<AbnormalRecordDetailsData>)msg.obj;
                int updateTime=0;
                //对比mQualityRecordList.get(update).getCreateTime()取值
                for(int update=0;update<mAbnormalRecordList.size();update++){
                    if (mAbnormalRecordList.get(update+1) != null) {
                        if (mAbnormalRecordList.get(update).getCreateTime() < mAbnormalRecordList.get(update + 1).getCreateTime()) {
                            Log.d(TAG, "handleMessage: "+mAbnormalRecordList.get(update).getCreateTime()+" : "+mAbnormalRecordList.get(update+1).getCreateTime());
                            updateTime = update+1;
                        }
                        Log.d(TAG, "handleMessage: updateTime1:"+updateTime);
                    }
                    Log.d(TAG, "handleMessage: updateTime2:"+updateTime);
                }
                mAbnormalRecordDetailsData = mAbnormalRecordList.get(updateTime);
                //TODO:对应数值填入框内

                updateInstallRecordData();
            } else {
                String errorMsg = (String)msg.obj;
                Toast.makeText(DetailToInstallActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateInstallRecordData() {
        final String ip = SinSimApp.getApp().getServerIP();
        //读取和更新输入信息
        if(installNormalRb.isChecked()){
            mAbnormalRecordDetailsData.getTaskRecord().setStatus(NORMAL);
        }else if(installAbnormalRb.isChecked()){
            mAbnormalRecordDetailsData.getTaskRecord().setStatus(ABNORMAL);
            mAbnormalRecordDetailsData.setAbnormalType((int) failReasonSpinner.getSelectedItemId());
            if(installAbnormalDetailEt.getText()!=null){
                mAbnormalRecordDetailsData.setComment(installAbnormalDetailEt.getText().toString());
            }
        }

        Gson gson=new Gson();
        String mAbnormalRecordDetailsDataToJson = gson.toJson(mAbnormalRecordDetailsData);
        Log.d(TAG, "updateQARecordData: gson :"+ mAbnormalRecordDetailsDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("strTaskQualityRecordDetail", mAbnormalRecordDetailsDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + ip + URL.UPDATE_INSTALL_ABNORMAL_RECORD_DETAIL;
        Log.d(TAG, "updateQARecordData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, mUpdateProcessDetailDataHandler);
    }

    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                Toast.makeText(DetailToInstallActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(DetailToInstallActivity.this, "更新失败！"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onClickAddNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, ArrayList<String> models) {
        choicePhotoWrapper();
    }

    @Override
    public void onClickDeleteNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        mInstallAbnormalPhotosSnpl.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        Intent photoPickerPreviewIntent = new BGAPhotoPickerPreviewActivity.IntentBuilder(DetailToInstallActivity.this)
                .previewPhotos(models) // 当前预览的图片路径集合
                .selectedPhotos(models) // 当前已选中的图片路径集合
                .maxChooseCount(mInstallAbnormalPhotosSnpl.getMaxItemCount()) // 图片选择张数的最大值
                .currentPosition(position) // 当前预览图片的索引
                .isFromTakePhoto(false) // 是否是拍完照后跳转过来
                .build();
        startActivityForResult(photoPickerPreviewIntent, RC_INSTALL_PHOTO_PREVIEW);
    }

    @Override
    public void onNinePhotoItemExchanged(BGASortableNinePhotoLayout sortableNinePhotoLayout, int fromPosition, int toPosition, ArrayList<String> models) {

    }

    private void choicePhotoWrapper() {
        // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。如果不传递该参数的话就没有拍照功能
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "BGAPhotoPickerTakePhoto");
        Intent photoPickerIntent = new BGAPhotoPickerActivity.IntentBuilder(DetailToInstallActivity.this)
                .cameraFileDir(takePhotoDir) // 拍照后照片的存放目录，改成你自己拍照后要存放照片的目录。
                .maxChooseCount(mInstallAbnormalPhotosSnpl.getMaxItemCount() - mInstallAbnormalPhotosSnpl.getItemCount()) // 图片选择张数的最大值
                .selectedPhotos(null) // 当前已选中的图片路径集合
                .pauseOnScroll(false) // 滚动列表时是否暂停加载图片
                .build();
        startActivityForResult(photoPickerIntent, RC_INSTALL_CHOOSE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SCAN_QRCODE_END:
                if(resultCode == RESULT_OK) {
                    // 检验二维码信息是否对应
                    TaskMachineListData taskMachineListDataId=new TaskMachineListData();
                    taskMachineListDataId = (TaskMachineListData) data.getSerializableExtra("mTaskMachineListData");
                    if(taskMachineListDataId.getId()==mTaskMachineListData.getId()){
                        Log.d(TAG, "onActivityResult: id 对应");
                        //update info
                    } else {
                        Log.d(TAG, "onActivityResult: 二维码信息不对应");
                        Toast.makeText(this, "二维码信息不对应！", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "onActivityResult: scan QRcode fail");
                }

                break;
            case RC_INSTALL_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    mInstallAbnormalPhotosSnpl.addMoreData(BGAPhotoPickerActivity.getSelectedPhotos(data));
                } else {
                    Log.d(TAG, "onActivityResult: choose  nothing");
                }
                break;
            case RC_INSTALL_PHOTO_PREVIEW:
                mInstallAbnormalPhotosSnpl.setData(BGAPhotoPickerPreviewActivity.getSelectedPhotos(data));
            default:
                break;
        }
    }


}