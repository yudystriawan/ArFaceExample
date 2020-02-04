package com.example.arfaceexample


import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class PreviewVideoFragment : Fragment(), View.OnClickListener {

    private lateinit var videoView: VideoView

    companion object {
        val EXTRA_DIR = "extra_dir"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        videoView = view.findViewById(R.id.videoView)

        val btnExit: Button = view.findViewById(R.id.btn_exit)
        val btnSave: Button = view.findViewById(R.id.btn_save)

        btnExit.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview_video, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //FloatingActionButton floatingActionButton = ((MainActivity) getActivity()).getFloatingActionButton();
//        val fabFilter = (activity as AugmentedFaceActivity?)?.getFabFilterButton()
//        fabFilter?.hide()

        initVideo()

    }

    private fun initVideo() {
        if (arguments != null) {
            val mDir = arguments?.getString(EXTRA_DIR)

            val uri = Uri.parse(mDir)

            videoView.setOnPreparedListener {
                it.isLooping = true
            }

            videoView.apply {
                setVideoURI(uri)
                requestFocus()
                start()
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_exit -> {
                deleteFile()
                startActivity(activity?.intent)

            }

            R.id.btn_save -> {
                Toast.makeText(activity, "Video Saved", Toast.LENGTH_SHORT).show()
                activity?.onBackPressed()
            }
        }

    }

    private fun deleteFile() {
        val mDir = arguments?.getString(EXTRA_DIR)
        val uri = Uri.parse(mDir)
        val file = File(uri.path!!)

        if (file.exists()) {
            file.delete()
        }
    }

}
