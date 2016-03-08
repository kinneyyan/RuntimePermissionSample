package com.kingars.runtimepermissionsample;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE_WRITE_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertDummyContactWrapper();
            }
        });
    }

    //不论用户同意还是拒绝，activity的onRequestPermissionsResult会被回调来通知结果（通过第三个参数）
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE_CONTACTS:
                // Permission Granted
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    insertDummyContact();
                }
                // Permission Denied
                else {
                    //若用户在拒绝权限时勾选了"不再显示",显示对话框提示用户
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
                        showConfirmDialog("WRITE_CONTACTS permission denied, please enable it in Settings-Apps.", null);
                        return;
                    }
                    Toast.makeText(MainActivity.this, "WRITE_CONTACTS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void insertDummyContactWrapper() {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            //该方法只有在用户在上一次已经拒绝过你的这个权限申请返回true;勾选了"不再显示"时返回false
            //你需要给用户一个解释，为什么要授权
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
                showConfirmDialog("please accept WRITE_CONTACTS permission request.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS},
                                        REQUEST_CODE_WRITE_CONTACTS);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CODE_WRITE_CONTACTS);
            return;
        }
        insertDummyContact();
    }

    private void insertDummyContact() {
        Toast.makeText(MainActivity.this, "Got WRITE_CONTACTS permission!", Toast.LENGTH_SHORT).show();
//        // Two operations are needed to insert a new contact.
//        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(2);
//
//        // First, set up a new raw contact.
//        ContentProviderOperation.Builder op =
//                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
//        operations.add(op.build());
//
//        // Next, set the name for the contact.
//        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                .withValue(ContactsContract.Data.MIMETYPE,
//                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
//                        "__DUMMY CONTACT from runtime permissions sample");
//        operations.add(op.build());
//
//        // Apply the operations.
//        ContentResolver resolver = getContentResolver();
//        try {
//            resolver.applyBatch(ContactsContract.AUTHORITY, operations);
//            Toast.makeText(MainActivity.this, "WRITE_CONTACTS success", Toast.LENGTH_SHORT).show();
//        } catch (RemoteException | OperationApplicationException e) {
//            Toast.makeText(MainActivity.this, "WRITE_CONTACTS failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
    }

    private void showConfirmDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
}
