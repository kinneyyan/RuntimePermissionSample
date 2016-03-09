# RuntimePermissionSample
Android M runtime permission sample

以下为Android 6.0 运行时权限相关要点

## 1.新的权限机制
6.0开始将权限分为两类。
	
一类是Normal Permissions，这类权限一般不涉及用户隐私，是不需要用户进行授权的，比如手机震动、访问网络等。此类权限在应用安装时就被授予（同6.0之前）。
	
另一类是Dangerous Permission，一般是涉及到用户隐私的，在app使用时需要用户进行授权，比如读取sdcard、访问通讯录等。此类权限是分组的：
	
<!-- more -->
	
![image](http://inthecheesefactory.com/uploads/source/blog/mpermission/permgroup.png)
	
同一组的任何一个权限被授权了，其他权限也自动被授权。

## 2.关于targetSdkVersion需要注意的
- 若targetSdkVersion低于23，将使用旧有规则：用户在安装的时候不得不接受所有权限，安装后app就有了那些权限。不过用户依然可以在设置中取消已经同意的授权。
- 若targetSdkVersion高于23，如果app在使用一些敏感权限的时候没有做运行时权限的代码处理，app会直接crash。
	
## 3.相关API以及使用步骤
	
假设我们的app有添加联系人的功能：
	
1. 在AndroidManifest文件中添加需要的权限android.permission.WRITE_CONTACTS
2. 在添加联系人的代码之前，检查权限

	```java
	int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
	if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
		//申请授权
		...
		return;
	}
	//添加联系人
	insertDummyContact();
    ```
    
    `ContextCompat.checkSelfPermission`：用于检测某个权限是否已经被授予，返回值为`PackageManager.PERMISSION_DENIED`或者`PackageManager.PERMISSION_GRANTED`
    
3. 申请授权

	```java
    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE_WRITE_CONTACTS);
    ```
    
    `ActivityCompat.requestPermissions`：第一个参数是Context；第二个参数是需要申请的权限的字符串数组，很明显这里可以一次申请多个；第三个参数为requestCode，主要用于回调的时候检测。
    
4. 处理申请回调。不论用户同意还是拒绝，activity的onRequestPermissionsResult都会被回调来通知结果。
    
    ```java
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
                    Toast.makeText(MainActivity.this, "WRITE_CONTACTS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    ```
    
    `onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)`：第一个参数requestCode不用说；第二个参数为申请的权限，例如Manifest.permission.WRITE_CONTACTS；第三个参数为申请结果。如果此次回调是一次申请多个权限的情况，那第二个参数和第三个参数为对应关系。
    	
	至此权限申请的步骤走通，不过还有个API需要提下：
	`ActivityCompat.shouldShowRequestPermissionRationale(Activity activity, String permission)`：该方法只有在用户在上一次已经拒绝过你的这个权限申请时返回true；其余情况例如用户勾选了"不再显示"时均返回false。这个API的目的主要用于给用户一个申请权限的解释，我们可以弹个对话框告知用户为什么需要这个权限，点击确定时再去申请权限。加入此方法的申请权限代码如下：
	
	```java
	int hasWriteContactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS);
	if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
		//该方法只有在用户在上一次已经拒绝过你的这个权限申请返回true;勾选了"不再显示"时返回false
		//你需要给用户一个解释，为什么要授权
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
			showConfirmDialog("please accept WRITE_CONTACTS permission request.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            	ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE_WRITE_CONTACTS);
                            }});
        	return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE_WRITE_CONTACTS);
        return;
	}
	insertDummyContact();
	```
	
    如果用户勾选了“不再显示”拒绝后，再次申请权限时，在onRequestPermissionsResult回调方法中走权限拒绝的方法，如果用户又想开启此权限的话，我们可以通过shouldShowRequestPermissionRationale返回值判断是否勾选“不再显示”，是的话在回调方法判断权限拒绝的代码块中弹一个对话框告知用户：在设置－应用－权限管理中去开启。
    
    ```java
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
    ```
    
## 4.第三方开源库

列举几个还不错的。

- [https://github.com/hotchemi/PermissionsDispatcher](https://github.com/hotchemi/PermissionsDispatcher)
	
- [https://github.com/Karumi/Dexter](https://github.com/Karumi/Dexter)
	
- [https://github.com/hongyangAndroid/MPermissions](https://github.com/hongyangAndroid/MPermissions)

## 5.参考博客

> [Android M 新的运行时权限开发者需要知道的一切](http://jijiaxin89.com/2015/08/30/Android-s-Runtime-Permission/)
	
> [Android 6.0 运行时权限处理](http://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=402456158&idx=1&sn=67d952c5fc3fb7876fc14783be6ab50a&scene=0#rd)