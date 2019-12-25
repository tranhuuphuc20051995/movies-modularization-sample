package com.tzion.android.movies.presentation.procesor

import com.tzion.android.movies.domain.FindMoviesByTextUseCase
import com.tzion.android.movies.presentation.action.FindMoviesAction
import com.tzion.android.movies.presentation.action.FindMoviesAction.*
import com.tzion.android.movies.presentation.result.FindMoviesResult
import com.tzion.android.movies.presentation.result.FindMoviesResult.*
import com.tzion.corepresentation.execution.ExecutionThread
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class FindMoviesActionProcessor @Inject constructor(
    private val findMoviesByTextUseCase: FindMoviesByTextUseCase,
    private val executionThread: ExecutionThread) {

    var actionProcessor: ObservableTransformer<FindMoviesAction, FindMoviesResult>
        private set

    init {
        actionProcessor = ObservableTransformer { observableAction ->
            observableAction.publish { action ->
                action.ofType(FindMoviesByTextAction::class.java)
                    .compose(findMoviesByTextProcessor)
                    .cast(FindMoviesResult::class.java)
            }
        }
    }

    private val findMoviesByTextProcessor =
        ObservableTransformer<FindMoviesByTextAction, FindMoviesByTextResult> { actions ->
            actions.switchMap { action ->
                findMoviesByTextUseCase
                    .execute(action.queryText)
                    .toObservable()
                    .map { movies ->
                        FindMoviesByTextResult.Success(movies)
                    }
                    .cast(FindMoviesByTextResult::class.java)
                    .onErrorReturn(FindMoviesByTextResult::Error)
                    .startWith(FindMoviesByTextResult.InProcess)
                    .subscribeOn(executionThread.schedulerForSubscribing())
                    .observeOn(executionThread.schedulerForObserving())
            }
        }

}