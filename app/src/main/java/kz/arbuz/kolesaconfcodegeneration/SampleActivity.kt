package kz.arbuz.kolesaconfcodegeneration

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kz.arbuz.permrequester.annotation.OnPermissionResult
import kz.arbuz.permrequester.annotation.PermissionRequired

@PermissionRequired
class SampleActivity: AppCompatActivity(R.layout.sample_layout) {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<AppCompatButton>(R.id.clickMe).setOnClickListener {
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        SampleActivityGenerated.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @OnPermissionResult(requestCode = CAMERA_PERMISSION_REQUEST_CODE)
    fun takePicture() {
        Toast.makeText(this, "Permission Requested!", Toast.LENGTH_LONG).show()
    }
}