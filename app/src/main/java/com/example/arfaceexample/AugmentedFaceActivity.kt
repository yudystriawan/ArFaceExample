package com.example.arfaceexample

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.CamcorderProfile
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arfaceexample.adapter.ListArFace
import com.example.arfaceexample.model.Filter
import com.example.arfaceexample.record.VideoRecorder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode
import kotlinx.android.synthetic.main.activity_augmented_face.*
import java.io.File


class AugmentedFaceActivity : AppCompatActivity() {

    companion object {
        private val TAG = AugmentedFaceActivity::class.java.simpleName
        private const val MIN_OPENGL_VERSION = 3.0

        const val EXTRA_MODEL = "extra_model"
    }

    private lateinit var videoRecorder: VideoRecorder
    private lateinit var arFragment: FaceArFragment
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayout: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var fabFilter: FloatingActionButton

    private var modelRenderable: ModelRenderable? = null
//    private var texture: Texture? = null

    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_augmented_face)

        if (!checkIsSupportedDeviceOrFinish(this))
            return

        arFragment =
            supportFragmentManager.findFragmentById(R.id.face_fragment) as FaceArFragment

        recyclerView = findViewById(R.id.rv_arFaces)
        linearLayout = findViewById(R.id.bottom_sheet)

        bottomSheetBehavior = BottomSheetBehavior.from(linearLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        fabFilter = findViewById(R.id.fab_filter)
        fabFilter.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        val filters = getListFilter()

        intAdapter(filters)

        val model = intent.getIntExtra(EXTRA_MODEL, 0)

        if (model != 0) {
            loadModelRenderable(this, model)
            setupScene()
        }


        //Init VideoRecorder
        videoRecorder = VideoRecorder(this)

        val orientation = resources.configuration.orientation

        videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_2160P, orientation)
        videoRecorder.setSceneView(arFragment.arSceneView)

        record.setOnClickListener(this::toggleRecording)
        record.isEnabled = true
        record.setImageResource(R.drawable.round_video_cam)


    }


    private fun intAdapter(filters: ArrayList<Filter>) {
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val filterAdapter = ListArFace(filters)
        recyclerView.adapter = filterAdapter

        filterAdapter.setOnItemClickCallback(object : ListArFace.OnItemClickCallback {
            override fun onItemClicked(data: Filter) {
                showSelectedFilter(data)
            }
        })
    }

    private fun showSelectedFilter(data: Filter) {
        if (videoRecorder.isRecording()) {
            showAlertDialog(data)
        } else {
            changeFilter(data)
        }

    }

    private fun changeFilter(data: Filter) {
        Toast.makeText(this, "Kamu memilih ${data.name}", Toast.LENGTH_SHORT).show()
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        intent.putExtra(EXTRA_MODEL, data.model)
        startActivity(intent)
    }

    private fun showAlertDialog(data: Filter) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Pilih Filter")
        alertDialogBuilder
            .setMessage("Memilih filter berarti menghentikan proses perekaman, apakah anda ingin melanjutkan?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                changeFilter(data)
            }
            .setNegativeButton("Tidak") { dialogInterface, _ -> dialogInterface.cancel() }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getListFilter(): ArrayList<Filter> {
        val listFilter = ArrayList<Filter>()
        listFilter.add(Filter(null, "No Filter", null))
        listFilter.add(Filter(null, "Fox", R.raw.fox_face))
        listFilter.add(Filter(null, "Mask", R.raw.mask01))
        return listFilter
    }

    private fun loadModelRenderable(context: Context, model: Int) {

        modelRenderable = null

        ModelRenderable.builder()
            .setSource(context, model)
            .build()
            .thenAccept {
                it.apply {
                    isShadowCaster = false
                    isShadowReceiver = false
                }
                modelRenderable = it
            }
    }

    private fun loadTexture(context: Context, texture: Int) {
        Texture.builder()
            .setSource(context, texture)
            .build()
            .thenAccept {
                //                this.texture = it
            }
    }

    private fun setupScene() {
        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        val scene = sceneView.scene

        scene.addOnUpdateListener {

            val faceList = sceneView.session?.getAllTrackables(AugmentedFace::class.java)

            for (face in faceList!!) {

                if (!faceNodeMap.containsKey(face)) {
                    val faceNode = AugmentedFaceNode(face)
                    faceNode.apply {
                        setParent(scene)
                        faceRegionsRenderable = modelRenderable
//                        faceMeshTexture = texture
                    }
                    faceNodeMap[face] = faceNode
                }

            }

            val iterator = faceNodeMap.entries.iterator()

            while (iterator.hasNext()) {
                val entry = iterator.next()
                val face = entry.key

                if (face.trackingState == TrackingState.STOPPED) {
                    val faceNode = entry.value
                    faceNode.setParent(null)
                    iterator.remove()
                }
            }


        }
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (ArCoreApk.getInstance().checkAvailability(activity) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Log.e(TAG, "Augmented Faces requires ArCore.")
            Toast.makeText(activity, "Augmented Faces requires ARCore", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }

        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion

        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL.ES 3.0 later.")
            Toast.makeText(
                activity,
                "Sceneform requires OpenGL ES 3.0 or later",
                Toast.LENGTH_LONG
            )
                .show()
            activity.finish()
            return false
        }

        return true
    }

    override fun onPause() {
        if (videoRecorder.isRecording()) {
            toggleRecording(null)
        }
        super.onPause()
    }

    @SuppressLint("InlinedApi")
    private fun toggleRecording(unusedView: View?) {
        if (!arFragment.hasWritePermission()) {
            Log.e(TAG, "Video recording requires the WRITE_EXTERNAL_STORAGE permission")
            Toast.makeText(
                this, "Video recording requires the WRITE_EXTERNAL_STORAGE permission",
                Toast.LENGTH_LONG
            ).show()

            arFragment.launchPermissionSettings()
            return
        }

        if (!arFragment.hasRecordAudioPermission()) {
            Log.e(TAG, "Video recording requires the RECORD_AUDIO permission")
            Toast.makeText(
                this, "Video recording requires the RECORD_AUDIO permission",
                Toast.LENGTH_LONG
            ).show()

            arFragment.launchPermissionSettings()
            return
        }

        val recording = videoRecorder.onToggleRecord()
        if (recording) {
            record.setImageResource(R.drawable.round_stop)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            
        } else {
            record.setImageResource(R.drawable.round_video_cam)
            // save video to local
            val videoPath = videoRecorder.getVideoPath().absolutePath
            Toast.makeText(this, "Video saved: $videoPath", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Video saved: $videoPath")

            //get preview video
            getPreviewVideo()

            //Send notification of updatedContent
            val values = ContentValues()
            values.put(MediaStore.Video.Media.TITLE, "Sceneform Video")
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            values.put(MediaStore.Video.Media.RELATIVE_PATH, videoPath)

            contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        }
    }

    private fun getPreviewVideo() {
        val previewVideoFragment = PreviewVideoFragment()
        val fragmentManager = supportFragmentManager

        val bundle = Bundle()
        bundle.putString(PreviewVideoFragment.EXTRA_DIR, videoRecorder.getVideoPath().absolutePath)
        previewVideoFragment.arguments = bundle

        fragmentManager
            .beginTransaction()
            .replace(
                R.id.constain_container,
                previewVideoFragment,
                PreviewVideoFragment::class.java.simpleName
            )
            .addToBackStack(null)
            .commit()
//
//        val intent = Intent(this, PreviewVideoActivity::class.java)
//        startActivity(intent)

    }

    fun getFabFilterButton():FloatingActionButton{
        return fabFilter
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

}
