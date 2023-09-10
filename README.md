# 前言

本教程提供源码，可以对照着源码一步一步的往下看，代码的演示效果以及文件结构都在下方会展示，本教程主要是修改四个文件，分别是`AndroidManifest.xml`、`accessibility_service_config.xml`、`MyAccessibilityService.kt`、   `MainActivity.kt`，所以只要这四个文件懂了，基本无障碍就没有问题了，里面有个string.xml的文件不要忘了就可以了，我可能没在下方标注出来，不过代码会提示，建议跟着源码来进行，源码地址：，有什么问题可以在下方评论区留言，或者加q群讨论（711762040）

## 演示效果

<img src="https://fastly.jsdelivr.net/gh/iiheng/TuChuang@main/1694324002477207FF27641418F61D42726363C6C1B00.gif" alt="" style="zoom:20%;" />

## 代码结构

<img src="https://fastly.jsdelivr.net/gh/iiheng/TuChuang@main/1694323885571filestructure.png" alt="" style="zoom:80%;" />

## AndroidManifest.xml

首先我们需要在AndroidManifest.xml声明我们需要的权限，无障碍的权限以及我们自己的服务

> 在安卓中，服务是可以运行在后台的，就是说应用被切换到后台，服务仍然可以继续工作，服务一般都是用来执行不需要用户界面的任务，而我们现在无障碍服务就属于服务类型中绑定服务，不需要用户界面，可以用来监视或者控制用户界面交互的服务

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
          xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE"
                     tools:ignore="ProtectedPermissions" /> //需要添加这个

            android:allowBackup="true"
            android:dataExtractionRules="@xml/data_extraction_rules"
            android:fullBackupContent="@xml/backup_rules"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/Theme.Accessibilityservicetest"
            tools:targetApi="31">
        <activity
                android:name=".MainActivity"
                android:exported="true"
                android:label="@string/app_name"
                android:theme="@style/Theme.Accessibilityservicetest">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>

                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        //定义我们所需要的无障碍服务，下一步是MyAccessibilityService和accessibility_service_config的编写
        <service android:name=".MyAccessibilityService"
                 android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
                 android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                    android:name="android.accessibilityservice"
                    //指定了一个 XML 资源文件，该文件包含了无障碍服务的配置信息。这个文件描述了服务的功能和行为，例如它可以响应哪些类型的无障碍事件。
                    android:resource="@xml/accessibility_service_config" />
        </service>
    </application>
</manifest>
```

## accessibility_service_config.xml

下面是用定义无障碍服务配置的代码，对于自动化操作，我们主要关注下面两个配置

> 1. 首先是accessibilityEventTypes，这个定义了无障碍服务希望接收的无障碍事件类型。typeAllMask 表示服务希望接收所有类型的事件。详细的类型参考官方链接：[https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo#eventTypes](https://)
> 2. 第二个是canPerformGestures="true"，用来表示无障碍服务可以执行手势。这对于模拟用户的触摸输入，如滑动或点击，非常有用

```
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
                       android:accessibilityEventTypes="typeAllMask"
                       android:accessibilityFeedbackType="feedbackAllMask"
                       android:accessibilityFlags="flagDefault"
                       android:canPerformGestures="true"
                       android:description="@string/service_description"
                       android:notificationTimeout="100"
                       android:packageNames="com.example.accessibilitydemo"
                       android:settingsActivity="com.example.accessibilitydemo.SettingsActivity" />

```

1. strings.xml
   
   这个文件是用来说明我们这个无障碍服务是用来干什么的，在无障碍界面会有说明
   ```kotlin
   <resources>
       <string name="app_name">accessibilityservicetest</string>
       <string name="service_description">无障碍实现自动上划demo</string>
   </resources>
   ```

## MyAccessibilityService.kt

1. 下面是我们来自定义自己需要的无障碍服务，首先我们需要继承AccessibilityService并且重构我们需要的方法，我们先看最简化的无障碍是怎么样的：
   ```kotlin
   class MyAccessibilityService : AccessibilityService() {
     override fun onServiceConnected() {} //无障碍服务连接时触发
     override fun onAccessibilityEvent(event: AccessibilityEvent) {} //检测到与无障碍服务指定的事件过滤参数匹配的AccessibilityEvent才会回调,与accessibility_service_config.xml配置文件中的accessibilityEventTypes相匹配
     override fun onInterrupt() {} //当系统要中断服务正在提供的反馈（通常是为了响应将焦点移到其他控件等用户操作）时，会调用此方法。此方法可能会在服务的整个生命周期内被调用多次。
     override fun onUnbind(){} //当系统将要关闭无障碍服务时，会调用此方法
   }
   ```
2. 根据上面触发的条件，我们目前在于自动上划的这个功能上，可能使用不大，但是在于后面我们获取屏幕上信息上时，onAccessibilityEvent的使用就比较大了
   > **在下面的代码中，最重要有两个，一个是按钮点击广播的触发，还有一个就是自动化点击如何实现的问题**
3. 首先我们先来看如何点击按钮来触发服务，然后触发以后进行上划自动化
   > 说明一下为什么不按钮直接点击触发而要通过广播绕一圈去触发:
   > 
   > dispatchGesture方法是AccessibilityService的一部分，你不能直接从应用的主活动或其他非无障碍服务的部分调用它。这就是为什么示例中使用广播来触发无障碍服务执行手势的原因
   
   在主界面定义一个按钮，按下触发swipe的广播，`MainActivity.kt`
   ```kotlin
   class MainActivity : ComponentActivity() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           setContent {
               AccessibilityservicetestTheme{
                   AccessibilityApp()
               }
           }
       }
   }
   
   
   @Composable
   fun AccessibilityApp() {
       Surface(
           modifier = Modifier.fillMaxSize(),
           color = MaterialTheme.colorScheme.background
       ) {
           Column(
               modifier = Modifier
                   .fillMaxSize()
                   .padding(16.dp),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
           ) {// Adds space between buttons
               SwipeButton()  // New button to initiate the swipe
           }
       }
   }
   
   @Composable
   fun SwipeButton() {
       val context = LocalContext.current // 获取当前的Context
   
       Button(
           onClick = {
               // 检查Accessibility Service是否启动
               val isEnabled = isAccessibilityServiceEnabled(context, MyAccessibilityService::class.java.name)
   
               if (isEnabled) {
                   // 如果已启动，发送广播来执行滑动
                   Log.d("SwipeButton", "Button clicked")
                   val intent = Intent(MyAccessibilityService.SWIPE_ACTION)
   
                   context.sendBroadcast(intent)
               } else {
                   // 如果没有启动，引导用户启动无障碍服务
                   val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                   context.startActivity(intent)
               }
           },
           modifier = Modifier.padding(16.dp)
       ) {
           Text(text = "开始上划")
       }
   }
   
   // 一个简单的函数来检查是否启用了无障碍服务
   fun isAccessibilityServiceEnabled(context: Context, accessibilityService: String): Boolean {
       val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
       val enabledServices = Settings.Secure.getString(
           context.contentResolver,
           Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
       )
       return enabledServices?.contains(accessibilityService) == true
   }
   ```
4. 接收广播，并进行滑动,我们来看一下如何实现滑动：
   > path ：首先我们会定义一个路径，相当于我们如何滑动的手势，比如直线上划，横过来划一样，path有很多选择。例如quadTo，arcTo等等
   > 
   > gestureDescriptio ：有了上面的path，我们来构建一个手势描述。这个描述会告诉无障碍服务如何执行手势，path是我们执行的路径，第二个 参数是用来描述我们等待多少时间去执行（单位毫秒），最后一个参数就是用过来描述我执行了画了多久
   > 
   > dispatchGesture(gestureDescription, null, null)：使用dispatchGesture方法来执行手势。这个方法需要一个手势描述，以及两个可选的回调（在这里都设置为null）：一个是手势成功时的回调，另一个是手势失败时的回调。
   > 
   > 连续的手势可以参看这里[https://developer.android.google.cn/guide/topics/ui/accessibility/service?hl=zh-cn#continued-gestures](https://)
   ```
   @RequiresApi(Build.VERSION_CODES.O)
       private fun performSwipeAction() {
           val path = Path().apply {
               moveTo(500f, 1500f) // Starting point (x, y)
               lineTo(500f, 500f)  // Ending point (x, y)
           }
           val gestureDescription = GestureDescription.Builder()
               .addStroke(GestureDescription.StrokeDescription(path, 100L, 1000L))
               .build()
   
           dispatchGesture(gestureDescription, null, null)
       }
   ```
   ```kotlin
   class MyAccessibilityService : AccessibilityService() {
   
       override fun onServiceConnected() {
           super.onServiceConnected()
           Log.d("connect","service已经连接")
           val filter = IntentFilter(SWIPE_ACTION)
           registerReceiver(swipeReceiver, filter)
       }
   
       override fun onAccessibilityEvent(event: AccessibilityEvent) {}
   
       override fun onInterrupt() {}
       private val swipeReceiver = object : BroadcastReceiver() {
           @RequiresApi(Build.VERSION_CODES.O)
           override fun onReceive(context: Context?, intent: Intent?) {
               if (intent?.action == SWIPE_ACTION) {
                   Log.d("SwipeButton", "接收到广播了");
                   performSwipeAction()
               }
           }
       }
       companion object {
           const val SWIPE_ACTION = "com.example.kitlintest2.SWIPE_ACTION"
       }
   
       override fun onDestroy() {
           super.onDestroy()
           unregisterReceiver(swipeReceiver)
       }
       // Simulates an L-shaped drag path: 200 pixels right, then 200 pixels down.
       @RequiresApi(Build.VERSION_CODES.O)
       private fun performSwipeAction() {
           val path = Path().apply {
               moveTo(500f, 1500f) // Starting point (x, y)
               lineTo(500f, 500f)  // Ending point (x, y)
           }
   
           val gestureDescription = GestureDescription.Builder()
               .addStroke(GestureDescription.StrokeDescription(path, 100L, 1000L))
               .build()
   
           dispatchGesture(gestureDescription, null, null)
       }
   }
   ```
