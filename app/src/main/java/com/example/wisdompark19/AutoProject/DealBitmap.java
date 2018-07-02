package com.example.wisdompark19.AutoProject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 最美人间四月天 on 2018/4/10.
 * 图片转换
 */

public class DealBitmap {
    /**
     * 压缩图片（质量压缩）
     * @param
     */
    public static String compressImage(Blob blob, String s) throws SQLException {
        InputStream inputStream = blob.getBinaryStream();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 500) {  //循环判断如果压缩后图片是否大于500kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            long length = baos.toByteArray().length;
        }
        // 获取 SD 卡根目录
        String saveDir = Environment.getExternalStorageDirectory() + "/com.example.wisdom.park/IMG/";
        // 新建目录
        File dir = new File(saveDir);
        if (! dir.exists()) {
            dir.mkdirs();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String filename = format.format(date) + s;
        File file = new File(saveDir,filename+".png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
      //          BAFLogger.e(TAG,e.getMessage());
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
     //       BAFLogger.e(TAG,e.getMessage());
            e.printStackTrace();
        }
        recycleBitmap(bitmap);
        System.out.println("保存图片"+file.getPath());
        return file.getPath();
    }

    public static void recycleBitmap(Bitmap... bitmaps) {
        if (bitmaps==null) {
            return;
        }
        for (Bitmap bm : bitmaps) {
            if (null != bm && !bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    public static Bitmap InputToBitmap(InputStream inputStream){
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    public static Bitmap UriToBitmap(String uri){
        Bitmap bitmap = BitmapFactory.decodeFile(uri);
        return bitmap;
    }

    public static Bitmap byteToBit(byte[] bytes){
        Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return b;
    }
    public static Bitmap BitmapYasuo(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 500) {  //循环判断如果压缩后图片是否大于500kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            long length = baos.toByteArray().length;
        }
        return bitmap;
    }
    /**
     * 把Blob类型转换为byte数组类型
     *
     * @param blob
     * @return
     */
    public static byte[] blobToBytes(Blob blob) {
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(blob.getBinaryStream());
            byte[] bytes = new byte[(int) blob.length()];
            int len = bytes.length;
            int offset = 0;
            int read = 0;
            while (offset < len
                    && (read = is.read(bytes, offset, len - offset)) >= 0) {
                offset += read;
            }
            return bytes;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static String BitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50,baos);
        String imageBase64 = new String (Base64.encode(baos.toByteArray(), 0));
        return imageBase64;
    }
    public static Bitmap StringToBitmap(String string){
        byte[] byte64 = Base64.decode(string, 0);
        ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
        Bitmap bitmap = BitmapFactory.decodeStream(bais);
        return bitmap;
    }
    public static Bitmap createBitmapThumbnail(Bitmap bitMap) {
        int width = bitMap.getWidth();
        int height = bitMap.getHeight();
        // 设置想要的大小
        int newWidth = 99;
        int newHeight = 99;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height,
                matrix, true);
        return newBitMap;
         }

    /**

     * @param bitmap      原图
     * @return  缩放截取正中部分后的位图。
     */
    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap){
        int edgeLength = 100;
        if(null == bitmap || edgeLength <= 0)
        {
            return  null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        if(widthOrg > edgeLength && heightOrg > edgeLength)
        {
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = (int)(edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;
            try{
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            }
            catch(Exception e){
                return null;
            }
            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;
            try{
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            }
            catch(Exception e){
                return null;
            }
        }
        return result;
    }

    /**
     * 按正方形裁切图片
     */
    public static Bitmap ImageCrop(Bitmap bitmap, boolean isRecycled)
    {

        if (bitmap == null)
        {
            return null;
        }

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
                false);
        if (isRecycled && bitmap != null && !bitmap.equals(bmp)
                && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }

        // 下面这句是关键
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null,
        // false);
    }

    /**
     * 按长方形裁切图片
     *
     * @param bitmap
     * @return
     */
    public static Bitmap ImageCropWithRect(Bitmap bitmap)
    {
        if (bitmap == null)
        {
            return null;
        }

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int nw = 100, nh = 130, retX, retY;
        if (w > h)
        {
//            nw = h / 2;
//            nh = h;
            retX = (w - nw) / 2;
            retY = 0;
        } else
        {
//            nw = w / 2;
//            nh = w;
            retX = w / 4;
            retY = (h - w) / 2;
        }

        // 下面这句是关键
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }

//    public static String getBitmap(ContentResolver cr,Intent data){
//        Bitmap bitmap_camera = null;
//        Uri uri_camera = data.getData();
//        Bundle extras = null;
//        try {
//            if(data.getData() != null)
//                //这个方法是根据Uri获取Bitmap图片的静态方法
//                bitmap_camera = MediaStore.Images.Media.getBitmap(cr, uri_camera);
//                //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
//            else
//                extras = data.getExtras();
//            bitmap_camera = extras.getParcelable("data");
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        FileOutputStream fileOutputStream = null;
//        File file = null;
//        try {
//            // 获取 SD 卡根目录
//            String saveDir = Environment.getExternalStorageDirectory() + "/com.example.wisdom.park/IMG";
//            // 新建目录
//            File dir = new File(saveDir);
//            if (! dir.exists()) dir.mkdirs();
//            // 生成文件名
//            SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS");
//            String filename = "IMG_" + (t.format(new Date())) + ".jpg";
//            // 新建文件
//            file = new File(saveDir, filename);
//            // 打开文件输出流
//            fileOutputStream = new FileOutputStream(file);
//            // 生成图片文件
//            bitmap_camera.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//            // 相片的完整路径
//            System.out.println("FFFF"+file.getPath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (fileOutputStream != null) {
//                try {
//                    fileOutputStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return file.getPath();
//    }

    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


}
