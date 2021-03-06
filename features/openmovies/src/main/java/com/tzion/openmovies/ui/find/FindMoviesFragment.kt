package com.tzion.openmovies.ui.find

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tzion.android.MoviesApp
import com.tzion.mvi.MviUi
import com.tzion.openmovies.R
import com.tzion.openmovies.databinding.FragmentMoviesFindBinding
import com.tzion.openmovies.presentation.FindMoviesViewModel
import com.tzion.openmovies.presentation.model.UiMovie
import com.tzion.openmovies.presentation.uistate.FindMoviesUiState
import com.tzion.openmovies.presentation.userintent.FindMoviesUserIntent
import com.tzion.openmovies.presentation.userintent.FindMoviesUserIntent.*
import com.tzion.openmovies.ui.di.DaggerOpenMoviesComponent
import com.tzion.util.DefaultValues
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class FindMoviesFragment: Fragment(), MviUi<FindMoviesUserIntent, FindMoviesUiState> {

    private val searchFilterIntentPublisher = PublishSubject.create<SearchFilterUserIntent>()
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val findMoviesViewModel: FindMoviesViewModel? by lazy {
        ViewModelProviders
            .of(this, viewModelFactory)
            .get(FindMoviesViewModel::class.java)
    }
    @Inject lateinit var findMoviesAdapter: FindMoviesAdapter
    private lateinit var binding: FragmentMoviesFindBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupInjection()
    }

    private fun setupInjection() {
        val appComponent = (activity?.applicationContext as MoviesApp).appComponent
        DaggerOpenMoviesComponent.factory().create(appComponent).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUiStatesAndProcessUserIntents()
    }

    private fun subscribeUiStatesAndProcessUserIntents() {
        findMoviesViewModel?.processUserIntents(userIntents())
        observeUiStates()
    }

    override fun userIntents(): Observable<FindMoviesUserIntent> {
        return searchFilterIntent().cast(FindMoviesUserIntent::class.java)
    }

    private fun searchFilterIntent(): Observable<SearchFilterUserIntent> = searchFilterIntentPublisher

    private fun observeUiStates() {
        findMoviesViewModel
            ?.liveData()
            ?.observe(this, Observer { uiState ->
                uiState?.let { renderUiStates(it) }
            })
    }

    override fun renderUiStates(uiState: FindMoviesUiState) {
        setScreenForLoading(uiState.isLoading)
        setScreenForInstructions(uiState.withSearchInstructions)
        setScreenForError(uiState.withError, uiState.errorMessage)
        setScreenForDisplayMovies(uiState.movies, uiState.thereAreNotMoviesMatches)
    }

    private fun setScreenForLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                pbDisplayMovies.visibility = View.VISIBLE
            } else {
                pbDisplayMovies.visibility = View.GONE
            }
        }
    }

    private fun setScreenForInstructions(withSearchInstructions: Boolean) {
        binding.apply {
            if (withSearchInstructions) {
                acivSearchDisplayMovies.visibility = View.VISIBLE
                tvInstructions.visibility = View.VISIBLE
            } else {
                acivSearchDisplayMovies.visibility = View.GONE
                tvInstructions.visibility = View.GONE
            }
        }
    }

    private fun setScreenForError(thereIsAnError: Boolean, errorMessage: String) {
        binding.apply {
            if (thereIsAnError) {
                ivError.visibility = View.VISIBLE
                tvError.text = getString(R.string.something_went_wrong, errorMessage)
                tvError.visibility = View.VISIBLE
            } else {
                ivError.visibility = View.GONE
                tvError.visibility = View.GONE
            }
        }
    }

    private fun setScreenForDisplayMovies(movies: List<UiMovie>,
                                          thereAreNotMoviesMatches: Boolean) {
        binding.apply {
            if (thereAreNotMoviesMatches) {
                ivEmptyList.visibility = View.VISIBLE
                tvEmptyList.visibility = View.VISIBLE
                rvDisplayMovies.visibility = View.GONE
            } else {
                ivEmptyList.visibility = View.GONE
                tvEmptyList.visibility = View.GONE
                setAdapterData(movies)
                rvDisplayMovies.visibility = View.VISIBLE
            }
        }
    }

    private fun setAdapterData(movies: List<UiMovie>) {
        findMoviesAdapter.setData(movies)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMoviesFindBinding.inflate(inflater, container, false)
        setUpRecyclerView()
        setupMenuListeners()
        return binding.root
    }

    private fun setUpRecyclerView() {
        try {
            binding.rvDisplayMovies.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            binding.rvDisplayMovies.itemAnimator = DefaultItemAnimator()
            binding.rvDisplayMovies.adapter = findMoviesAdapter
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setupMenuListeners() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_search -> {
                    val searchView = menuItem.actionView as SearchView
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextChange(newText: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextSubmit(query: String?): Boolean {
                            searchFilterIntentPublisher.onNext(
                                SearchFilterUserIntent(query ?: DefaultValues.emptyString())
                            )
                            return false
                        }
                    })
                    true
                }
                else -> false
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.find_movies_menu, menu)
//        val searchView = menu.findItem(R.id.menu_search)?.actionView as SearchView
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                searchFilterIntentPublisher.onNext(
//                    SearchFilterUserIntent(query ?: DefaultValues.emptyString())
//                )
//                return false
//            }
//        })
//    }

}