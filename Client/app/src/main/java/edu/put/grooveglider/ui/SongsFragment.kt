package edu.put.grooveglider.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.put.grooveglider.R
import edu.put.grooveglider.databinding.FragmentSongsBinding
import edu.put.grooveglider.utils.Adapter
import edu.put.grooveglider.music.Song
import edu.put.grooveglider.utils.Utils

class SongsFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var _binding: FragmentSongsBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: Adapter

    private lateinit var listView: ListView

    private var list: MutableList<Song>? = null // Temporarily mutable, consider changing this to ordinary list

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)

        listView = binding.root.findViewById(R.id.item_list)

        // Adjust adapter if there are new json data available
        sharedViewModel.jsonDataQueue.observe(viewLifecycleOwner) { json ->
            list = Utils.songListFromJsonMessage(json)
            adapter = list?.let { Adapter(requireContext(), R.layout.list_row_songs, it, lifecycleScope) }!!
            listView.adapter = adapter
        }

        sharedViewModel.currentSong.observe(viewLifecycleOwner) { number ->
            adapter.showCurrent(number)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}