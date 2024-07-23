package com.laurens.assesmennews.ui

import android.annotation.SuppressLint
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.SimpleCursorAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.laurens.assesmennews.databinding.FragmentNewsBinding


class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private val newsViewModel: NewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter
    private var isSearching = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupSpinner()
        setupSearchView()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchView.query.isNotEmpty()) {
                    binding.searchView.setQuery("", false)
                    binding.searchView.clearFocus()
                    isSearching = false
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = newsAdapter
        }
    }

    private fun observeViewModel() {
        newsViewModel.newsList.observe(viewLifecycleOwner, Observer { newsList ->
            newsAdapter.submitList(newsList)
            binding.progressBar.visibility = View.GONE // Hide ProgressBar when data is loaded
        })

        newsViewModel.newsSites.observe(viewLifecycleOwner, Observer { newsSites ->
            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("All") + newsSites
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerNewsSite.adapter = spinnerAdapter
        })

        newsViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        newsViewModel.recentSearches.observe(viewLifecycleOwner, Observer { recentSearches ->
            val recentSearchAdapter = createCursorAdapter(recentSearches.map { it.query })
            binding.searchView.apply {
                suggestionsAdapter = recentSearchAdapter
                setOnSuggestionListener(object : SearchView.OnSuggestionListener,
                    android.widget.SearchView.OnSuggestionListener {
                    override fun onSuggestionSelect(position: Int): Boolean {
                        return false
                    }
                    @SuppressLint("Range")
                    override fun onSuggestionClick(position: Int): Boolean {
                        val cursor = recentSearchAdapter.cursor
                        cursor.moveToPosition(position)
                        val query = cursor.getString(cursor.getColumnIndex("suggestion"))
                        setQuery(query, true)
                        return true
                    }
                })
            }
        })
    }

    private fun createCursorAdapter(suggestions: List<String>): SimpleCursorAdapter {
        val columns = arrayOf(BaseColumns._ID, "suggestion")
        val cursor = MatrixCursor(columns)
        for ((index, suggestion) in suggestions.withIndex()) {
            cursor.addRow(arrayOf(index, suggestion))
        }

        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)
        return SimpleCursorAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            cursor,
            from,
            to,
            0
        )
    }

    private fun setupSpinner() {
        binding.spinnerNewsSite.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedNewsSite = parent.getItemAtPosition(position).toString()
                val newsSite = if (selectedNewsSite == "All") null else selectedNewsSite
                binding.progressBar.visibility = View.VISIBLE // Show ProgressBar when fetching news data
                newsViewModel.fetchNews(newsSite)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    newsViewModel.searchNews(query)
                    isSearching = true
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    newsViewModel.clearSearch()
                } else {
                    newsViewModel.recentSearches.observe(viewLifecycleOwner, Observer { recentSearches ->
                        val recentSearchAdapter = createCursorAdapter(recentSearches.map { it.query })
                        binding.searchView.suggestionsAdapter = recentSearchAdapter
                    })
                }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            newsViewModel.clearSearch()
            isSearching = false
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}