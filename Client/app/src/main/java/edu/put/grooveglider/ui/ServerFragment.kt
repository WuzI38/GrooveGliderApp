package edu.put.grooveglider.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.put.grooveglider.R
import edu.put.grooveglider.databinding.FragmentServerBinding
import edu.put.grooveglider.utils.Adapter
import edu.put.grooveglider.music.Song
import edu.put.grooveglider.utils.Utils

class ServerFragment : Fragment() {
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var _binding: FragmentServerBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: Adapter

    private lateinit var listView: ListView
    private lateinit var searchEditText: EditText

    private var list: MutableList<Song>? = null // Temporarily mutable, consider changing this to ordinary list

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServerBinding.inflate(inflater, container, false)

        // Find layout elements
        listView = binding.root.findViewById(R.id.item_list)
        searchEditText = binding.root.findViewById(R.id.search)

        // Adjust adapter if there are new json data available
        sharedViewModel.jsonDataServer.observe(viewLifecycleOwner) { json ->
            list = Utils.songListFromJsonMessage(json)
            adapter = list?.let { Adapter(requireContext(), R.layout.list_row_server, it, lifecycleScope) }!!
            listView.adapter = adapter

            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // Filter the list based on the input
                    adapter.filter.filter(s.toString())
                }

                override fun afterTextChanged(s: Editable) { }
            })
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}