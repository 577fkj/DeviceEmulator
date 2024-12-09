package cn.fkj233.deviceemulator.app

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cn.fkj233.deviceemulator.R
import cn.fkj233.deviceemulator.aidl.IDeviceEmulatorInterface
import cn.fkj233.deviceemulator.databinding.ActivityMainBinding
import cn.fkj233.deviceemulator.service.manager.DeviceEmulatorManager
import cn.fkj233.xservicemanager.XServiceManager
import com.amap.api.maps.MapsInitializer
import com.github.kyuubiran.ezxhelper.utils.showToast
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"

        var service: IDeviceEmulatorInterface? = null
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val PM_MULTIPLE = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    //申请多个权限
    fun applyForMultiplePermissions() {
        Log.i(TAG, "applyForMultiplePermissions")
        try {
            //如果操作系统SDK级别在23之上（android6.0），就进行动态权限申请
            val pmList = ArrayList<String>()
            //获取当前未授权的权限列表
            for (permission in PM_MULTIPLE) {
                val nRet = ContextCompat.checkSelfPermission(this, permission)
                Log.i(TAG, "checkSelfPermission nRet=$nRet")
                if (nRet != PackageManager.PERMISSION_GRANTED) {
                    pmList.add(permission)
                }
            }

            if (pmList.size > 0) {
                Log.i(TAG, "进行权限申请...")
                val sList = pmList.toTypedArray<String>()
                ActivityCompat.requestPermissions(this, sList, 10000)
            } else {
                showToast("全部权限都已授权")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runCatching {
            Log.d("DeviceEmulator", "Try to get service")
            service = XServiceManager.getServiceInterface(DeviceEmulatorManager.SERVICE_NAME)
        }.onFailure {
            it.printStackTrace()
        }

        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).setAnchorView(R.id.fab).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main) // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        applyForMultiplePermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            val requestList = ArrayList<String>() //允许询问列表
            val banList = ArrayList<String>() //禁止列表
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "【" + permissions[i] + "】权限授权成功")
                } else {
                    //判断是否允许重新申请该权限
                    val nRet = ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permissions[i]
                    )
                    Log.i(TAG, "shouldShowRequestPermissionRationale nRet=$nRet")
                    if (nRet) { //允许重新申请
                        requestList.add(permissions[i])
                    } else { //禁止申请
                        banList.add(permissions[i])
                    }
                }
            }

            //优先对禁止列表进行判断
            if (banList.size > 0) { //告知该权限作用，要求手动授予权限
                showFinishedDialog()
            } else if (requestList.size > 0) { //告知权限的作用，并重新申请
                showTipDialog(requestList)
            } else {
                showToast("权限授权成功")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            showToast("权限申请回调中发生异常")
        }
    }

    fun showFinishedDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("警告")
            .setMessage("请前往设置中打开相关权限，否则功能无法正常运行！")
            .setPositiveButton(
                "确定"
            ) { dialog, which -> // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                finish()
            }
            .create()
        dialog.show()
    }

    fun showTipDialog(pmList: ArrayList<String>) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("【$pmList】权限为应用必要权限，请授权")
            .setPositiveButton("确定") { dialog, which ->
                val sList = pmList.toTypedArray<String>()
                //重新申请该权限
                ActivityCompat.requestPermissions(this@MainActivity, sList, 10000)
            }
            .create()
        dialog.show()
    }
}