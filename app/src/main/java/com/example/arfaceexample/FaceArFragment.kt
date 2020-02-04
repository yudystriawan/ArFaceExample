package com.example.arfaceexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.util.*
import kotlin.collections.ArrayList

class FaceArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        return config
    }

    override fun getSessionFeatures(): MutableSet<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val frameLayout =
            super.onCreateView(inflater, container, savedInstanceState) as FrameLayout

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)

        return frameLayout
    }

    override fun getAdditionalPermissions(): Array<String?> {

        val permissions = arrayOfNulls<String>(2)
        permissions[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE
        permissions[1] = Manifest.permission.RECORD_AUDIO

        return permissions
    }

    fun hasWritePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this.requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasRecordAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this.requireActivity(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun launchPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
        requireActivity().startActivity(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        
    }


}