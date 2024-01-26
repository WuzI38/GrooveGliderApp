package edu.put.grooveglider.ui

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import edu.put.grooveglider.databinding.FragmentUploadBinding
import edu.put.grooveglider.internet.TCPHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null

    private var audioUri: Uri? = null

    private val binding get() = _binding!!

    // Choose a local mp3 file to send
    private val selectMp3File = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        audioUri = uri
        val cursor = uri?.let { context?.contentResolver?.query(it, null, null, null, null) }
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        var fileName = "Choose file"
        if (cursor != null && cursor.moveToFirst() && nameIndex != null) {
            fileName = cursor.getString(nameIndex)
        }
        cursor?.close()
        binding.buttonChooseFile.text = fileName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)

        binding.buttonChooseFile.setOnClickListener {
            selectMp3File.launch("audio/mpeg")
        }

        // Show animation and handle file sending
        binding.buttonUpload.setOnClickListener {
            val title = binding.title.text.toString().trim()
            val artist = binding.artist.text.toString().trim()

            if (title.isNotEmpty() && artist.isNotEmpty() && audioUri != null) {
                lifecycleScope.launch {
                    binding.logoSetup.visibility = View.GONE
                    binding.logoLoader.visibility = View.VISIBLE
                    TCPHandler.send("AS;$title;$artist")
                    TCPHandler.sendFile(requireContext(), audioUri!!)
                    delay(500) // DELAY
                    TCPHandler.send("Finish")
                    binding.logoSetup.visibility = View.VISIBLE
                    binding.logoLoader.visibility = View.GONE
                }
            } else {
                binding.buttonUpload.text = String.format("Upload failed")
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}