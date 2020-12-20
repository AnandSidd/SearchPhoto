package com.example.searchphoto

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.searchphoto.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val staggeredRecyclerViewAdapter = StaggeredRecyclerViewAdapter(arrayListOf())
    private var NUM_COLUMNS = 2
    private var issearched: Boolean = false
    private lateinit var manager: StaggeredGridLayoutManager
    private var isScrolling = false
    private var REQ_CODE:Int = 100
    private var PAGE_SIZE:Int = 20
    private var PAGE_NO:Int = 1
    lateinit var flickrResponse:FlickrResponse
    lateinit var flickrPhotos: FlickrPhotos
    lateinit var listFlickrPhotos: List<FlickrPhoto>
    lateinit var searchhistory: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manager = StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayout.VERTICAL)
        binding.recyclerView.apply {
            layoutManager = manager
            adapter = staggeredRecyclerViewAdapter
        }

//        binding.recyclerView.itemAnimator = null
//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                    isScrolling = true
//                }
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val totalItems = manager.itemCount
//                val currentItems = manager.childCount
//                val scrollOutItems = manager.findFirstVisibleItemPositions(null)
//
//                if (isScrolling && (scrollOutItems[0] + currentItems >= totalItems)) {
//
//                    Log.i("TAG", "$totalItems $currentItems (${scrollOutItems[0]}, ${scrollOutItems[1]})")
//                    loadNextPage()
//                }
//            }
//        })
        binding.scrollview.viewTreeObserver.addOnScrollChangedListener(object : ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                if (binding.scrollview.getChildAt(0).bottom <= (binding.scrollview.height + binding.scrollview.scrollY)) {
                    isScrolling = false
                    issearched = false
                    loadNextPage()
                } else {
                    isScrolling = true
                }
            }
        })


        binding.searchET.setOnFocusChangeListener { view, hasFocus ->
            binding.SearchimageView.isVisible=!hasFocus
            binding.VoiceimageView.isVisible = !hasFocus
            binding.menuimageView.isVisible = !hasFocus
            binding.collapseimgview.isVisible = hasFocus
            binding.crossimgview.isVisible = hasFocus
        }
        binding.searchET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.searchET.clearFocus()
                if(binding.searchET.text.isNotEmpty()) {
                    fetchFlickrResponse(binding.searchET.text.toString(), PAGE_NO)
                    searchhistory.add(binding.searchET.text.toString())
                }
                else
                    Toast.makeText(this, "Type something to search", Toast.LENGTH_SHORT).show()
                binding.searchET.hideKeyboard()
                issearched = true
            }
            false
        }
        binding.VoiceimageView.setOnClickListener {
            val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Needf to Speak")
            try {
                startActivityForResult(intent, REQ_CODE)
            }catch (e: ActivityNotFoundException){
                Toast.makeText(applicationContext, "Voice Search not supported", Toast.LENGTH_SHORT).show()
            }
        }
        binding.collapseimgview.setOnClickListener {
            binding.searchET.text = null
            binding.searchET.clearFocus()
            it.hideKeyboard()

        }
        binding.crossimgview.setOnClickListener {
            binding.searchET.text = null
        }

        binding.menuimageView.setOnClickListener {
            val popup:PopupMenu = PopupMenu(this, binding.menuimageView)
            popup.menuInflater.inflate(R.menu.grid_menu, popup.menu)

            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.two -> {
                        manager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
                        binding.recyclerView.apply {
                            layoutManager = manager
                            adapter = staggeredRecyclerViewAdapter
                        }
                    }

                    R.id.three -> {
                        manager = StaggeredGridLayoutManager(3, LinearLayout.VERTICAL)
                        binding.recyclerView.apply {
                            layoutManager = manager
                            adapter = staggeredRecyclerViewAdapter
                        }
                    }

                    R.id.four -> {
                        manager = StaggeredGridLayoutManager(4, LinearLayout.VERTICAL)
                        binding.recyclerView.apply {
                            layoutManager = manager
                            adapter = staggeredRecyclerViewAdapter
                        }
                    }
                }
                true
            }
            popup.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==REQ_CODE && resultCode == RESULT_OK && data!=null){
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            binding.searchET.setText(result?.get(0))
            result?.get(0)?.let { fetchFlickrResponse(it, PAGE_NO) }
            searchhistory.add(binding.searchET.text.toString())
            issearched=true

        }
    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun fetchFlickrResponse(query: String, page: Int) {

        binding.progressBar.visibility = View.VISIBLE
        Log.i("TAG", "Page No: $page")

        val flickrPhotoService = FlickrPhotoServiceBuilder.buildService(FlickrPhotoService::class.java)

        val response = flickrPhotoService.getPhotos(query, page, PAGE_SIZE, "6f102c62f41998d151e5a1b48713cf13")

        response.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                if (response.isSuccessful) {
                    binding.progressBar.visibility = View.INVISIBLE
                    flickrResponse = response.body()!!
                    flickrPhotos = flickrResponse.photos!!
                    listFlickrPhotos = flickrPhotos.photo!!
                    staggeredRecyclerViewAdapter.updatePhoto(listFlickrPhotos, PAGE_NO, issearched)
                } else {
                }
            }
        })
    }

    fun loadNextPage(){
        PAGE_NO = PAGE_NO + 1
        fetchFlickrResponse(binding.searchET.text.toString(), PAGE_NO)
    }
}