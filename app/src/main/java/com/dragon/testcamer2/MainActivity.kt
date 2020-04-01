package com.dragon.testcamer2

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var cameraHolder: CameraHolder? = null
    var cameraId = CameraHolder.CAMERA_FRONT
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraHolder = CameraHolder(this)
        cameraHolder?.let {
            it.selectCamera(cameraId)
            it.open()
        }
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                Log.d("TextureView", "onSurfaceTextureSizeChanged ")
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                Log.d("TextureView", "onSurfaceTextureDestroyed $surface")
                cameraHolder?.let {
                    it.stopPreview().invalidate()
                }
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                Log.d("TextureView", "onSurfaceTextureAvailable $surface")
                cameraHolder?.let {
                    val size = it.getPreviewSize()?.get(0)
                    size?.let {
                        size ->
                        surface?.setDefaultBufferSize(size.width,size.height)
                        updatePreview(size.width,size.height)
                        it.setSurface(Surface(surface)).startPreview().invalidate()
                    }
                }
            }
        }
        buttonSwitch.setOnClickListener {
            cameraId = (cameraId.toInt() + 1).rem(2).toString()
            cameraHolder?.selectCamera(cameraId)?.invalidate()
        }

        buttonPreviewSize.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            cameraHolder?.let {
                val sizes = it.getPreviewSize()
                val sizesString: Array<String> = Array(sizes?.size ?: 0) { "" }
                sizes?.forEachIndexed { index, item ->
                    sizesString[index] = item.width.toString() + "*" + item.height.toString()
                }
                builder.setItems(sizesString) { d, index ->
                    val size = sizesString[index].split("*")
                    val width = size[0].toInt()
                    val height = size[1].toInt()
                    textureView.surfaceTexture.setDefaultBufferSize(width, height)
                    updatePreview(width,height)
                    it.setSurface(Surface(textureView.surfaceTexture)).invalidate()

                }
            }
            builder.create().show()
        }
    }

    private fun updatePreview(previewWidth:Int, previewHeight:Int){
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val ratioView = viewWidth * 1.0f/viewHeight
        val rotation = cameraHolder?.getRotation()?:0
        var rotatedPreviewWidth = previewWidth
        var rotatedPreviewHeight = previewHeight
        if(rotation % 180 != 0){
            rotatedPreviewWidth = previewHeight
            rotatedPreviewHeight = previewWidth
        }
        val ratioPreview = rotatedPreviewWidth * 1.0f / rotatedPreviewHeight
        var scaleX = 1.0f
        var scaleY = 1.0f
        if(ratioPreview > ratioView){
            val scaledHeight = viewHeight
            val scaledWidth = scaledHeight * ratioPreview
            scaleX = scaledWidth *1.0f/viewWidth
        }else{
            val scaledWidth = viewWidth
            val scaledHeight = scaledWidth * 1.0f / ratioPreview
            scaleY = scaledHeight * 1.0f/viewHeight
        }
        val matrix = Matrix()
        matrix.setScale(scaleX,scaleY,viewWidth / 2.0f,viewHeight/2.0f)
        textureView.setTransform(matrix)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraHolder?.release()?.invalidate()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraHolder?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
