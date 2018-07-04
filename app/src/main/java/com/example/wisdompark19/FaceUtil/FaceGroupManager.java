package com.example.wisdompark19.FaceUtil;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wisdompark19.R;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 声纹密码和人脸识别相关组操作
 * 支持组创建/添加成员/查询组成员/删除成员/删除组/
 * @see this#createGroup(Activity,String,String)
 * @see this#joinGroup(Activity,String,String)
 * @see this#deleteGroup(Activity,String,String) ,第二个参数用于标示删除组成员or组
 *
 * @author hjyu
 * @date 2017/9/28.
 * @see <a href="http://www.xfyun.cn">讯飞开放平台</a>
 */
public class FaceGroupManager {

    private final static String TAG = FaceGroupManager.class.getSimpleName();
    //Activity，群组名称，业主ID
    public static String createGroup(final Activity activity, String groupName, final String authId){

        final String[] groupId = {null};
        // 身份验证对象
        IdentityVerifier mIdVerifier = IdentityVerifier.createVerifier(activity, null);
        // sst=add，scope=group，group_name=famil;
        // 设置人脸模型操作参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);

        // 设置模型参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();
        params.append("auth_id=" + authId);
        params.append(",scope=group");
        params.append(",group_name=" + groupName);
        // 执行模型操作
        mIdVerifier.execute("ipt", "add", params.toString(), new IdentityListener() {
            @Override
            public void onResult(IdentityResult identityResult, boolean b) {
                Log.d(TAG, identityResult.getResultString());
                try {
                    JSONObject resObj = new JSONObject(identityResult.getResultString());
                    groupId[0] = resObj.getString("group_id");
                    // 创建成功后将自己加入到组里
                    // joinGroup(activity,groupId,authId); 暂不需要
                    Toast.makeText(activity,"组创建成功",Toast.LENGTH_LONG).show();
                    System.out.println("组ID" + groupId[0]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
             //   System.out.println("组创建成功");

            }

            @Override
            public void onError(SpeechError speechError) {
              //  System.out.println("组创建失败" + speechError.getPlainDescription(true));
                Toast.makeText(activity,"组创建失败"+speechError.getPlainDescription(true),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        });

        return groupId[0];
    }

    public static void joinGroup(final Activity activity, String groupIdCreate, String authId){
        IdentityVerifier mIdVerifier = IdentityVerifier.createVerifier(activity, null);
        if(!TextUtils.isEmpty(groupIdCreate)){
            // sst=add，auth_id=eqhe，group_id=123456，scope=person
            mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
            // 设置会话场景
            mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
            // 用户id
            mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);
            // 设置模型参数，若无可以传空字符传
            StringBuffer params2 = new StringBuffer();
            params2.append("auth_id=" + authId);
            params2.append(",scope=person");
            params2.append(",group_id=" + groupIdCreate);
            // 执行模型操作
            mIdVerifier.execute("ipt", "add", params2.toString(), new IdentityListener() {
                @Override
                public void onResult(IdentityResult identityResult, boolean b) {
                    try {
                        JSONObject resObj = new JSONObject(identityResult.getResultString());
                        // 保存到用户信息中，用来显示用户加人的组
                        System.out.println("group_name"+resObj.getString("group_name")+"\n"+
                                "group_id"+resObj.getString("group_id"));
                        Toast.makeText(activity,"组加入成功",Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(SpeechError speechError) {
                    Toast.makeText(activity,"组加入失败"+speechError.getPlainDescription(true),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onEvent(int i, int i1, int i2, Bundle bundle) {

                }
            });
        }else {
            Toast.makeText(activity,"组加入失败",Toast.LENGTH_LONG).show();
        }
    }

    public static void deleteGroup(final Activity activity, String groupIdCreate, String authId){
        IdentityVerifier mIdVerifier = IdentityVerifier.createVerifier(activity, null);
        if(!TextUtils.isEmpty(groupIdCreate)){
            // sst=add，auth_id=eqhe，group_id=123456，scope=person
            mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
            // 设置会话场景
            mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ipt");
            // 用户id
            mIdVerifier.setParameter(SpeechConstant.AUTH_ID, authId);

            // 设置模型参数，若无可以传空字符传
            StringBuffer params2 = new StringBuffer();
            //删除组
           // params2.append("scope=group");
            // 删除组中指定auth_id用户
            params2.append("scope=person");
            params2.append(",auth_id=" + authId);

            params2.append(",group_id=" + groupIdCreate);
            // 执行模型操作
            mIdVerifier.execute("ipt", "delete", params2.toString(), new IdentityListener() {
                @Override
                public void onResult(IdentityResult identityResult, boolean b) {
                    Log.d(TAG, identityResult.getResultString());
                    try {
                        JSONObject resObj = new JSONObject(identityResult.getResultString());
                        Log.d(TAG,"resObj == "+resObj.toString());
                        int ret = resObj.getInt("ret");
                        if(0 != ret) {
                            onError(new SpeechError(ret));
                            return;
                        } else {
                            if(identityResult.getResultString().contains("user")) {
                                String user = resObj.getString("user");
                                System.out.println("删除组成员"+user+"成功");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(SpeechError speechError) {
                    Toast.makeText(activity,"组员删除失败"+speechError.getPlainDescription(true),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onEvent(int i, int i1, int i2, Bundle bundle) {

                }
            });
        }else {
            Toast.makeText(activity,"组员删除失败",Toast.LENGTH_LONG).show();
        }
    }


}
