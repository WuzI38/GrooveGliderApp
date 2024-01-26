package edu.put.grooveglider.utils
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import edu.put.grooveglider.R
import edu.put.grooveglider.internet.TCPHandler
import edu.put.grooveglider.music.Song
import kotlinx.coroutines.launch
import java.util.Locale

class Adapter(
    private val ctx: Context,
    private val resources: Int,
    private var items: List<Song>,
    private val lifecycleScope: LifecycleCoroutineScope
) : ArrayAdapter<Song>(ctx, resources, items) {

    private var originalItems: List<Song> = items.toList()

    private var currentSongIndex: Int? = null

    // Fills the listView with list_row elements
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        val view: View = convertView ?: layoutInflater.inflate(resources, null)

        val titleView: TextView = view.findViewById(R.id.row_title)
        val artistView: TextView = view.findViewById(R.id.row_artist)

        val item: Song = items[position]

        if(resources == R.layout.list_row_server) {
            val addQueue: ImageButton = view.findViewById(R.id.button_add_server)

            addQueue.setOnClickListener {
                lifecycleScope.launch {
                    TCPHandler.send("AQ;${item.filename};${item.title};${item.artist}")
                }
            }
        }
        if(resources == R.layout.list_row_songs) {
            val removeQueue: ImageButton = view.findViewById(R.id.button_remove_queue)

            removeQueue.setOnClickListener {
                lifecycleScope.launch {
                    TCPHandler.send("DQ;${position}")
                }
            }
        }

        titleView.setTextColor(ContextCompat.getColor(context, R.color.light_gray))
        if (position == currentSongIndex) {
            titleView.setTextColor(ContextCompat.getColor(context, R.color.icon_orange))
        }
        titleView.text = item.title
        artistView.text = item.artist

        return view
    }

    // Filters the list by a given pattern (lowercase + trimmed)
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: List<Song> = if (constraint.isNullOrBlank()) {
                    originalItems
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.ROOT).trim()
                    originalItems.filter { it.title.lowercase(Locale.ROOT).contains(filterPattern) }
                }

                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                items = results?.values as List<Song>
                notifyDataSetChanged()
            }
        }
    }

    // Replaces the list of songs with a new list given as an argument
    fun showCurrent(songNumber: Int) {
        currentSongIndex = songNumber
        notifyDataSetChanged()
    }

    // Prevents an error that occurs when trying to access nonexistent elements (filtered list)
    override fun getCount(): Int {
        return items.size
    }
}